package com.dlr.assessment.application;

import com.dlr.assessment.domain.ScoreCalculator;
import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.execution.application.ExecutionResultRepository;
import com.dlr.execution.domain.ExecutionStatus;
import com.dlr.learning.application.AttemptRepository;
import com.dlr.learning.application.AttemptService;
import com.dlr.learning.domain.Attempt;
import com.dlr.learning.domain.AttemptStatus;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private static final String SCORE_VERSION = "V1";

    private final AttemptService attemptService;
    private final LabCatalog labCatalog;
    private final AssessmentRepository assessmentRepository;
    private final ExecutionResultRepository executionRepository;
    private final ScoreCalculator scoreCalculator;
    private final Clock clock;

    @Autowired
    public AssessmentService(
            AttemptService attemptService,
            LabCatalog labCatalog,
            AssessmentRepository assessmentRepository,
            ExecutionResultRepository executionRepository,
            ScoreCalculator scoreCalculator
    ) {
        this(attemptService, labCatalog, assessmentRepository, executionRepository, scoreCalculator, Clock.systemUTC());
    }

    AssessmentService(
            AttemptService attemptService,
            LabCatalog labCatalog,
            AssessmentRepository assessmentRepository,
            ExecutionResultRepository executionRepository,
            ScoreCalculator scoreCalculator,
            Clock clock
    ) {
        this.attemptService = attemptService;
        this.labCatalog = labCatalog;
        this.assessmentRepository = assessmentRepository;
        this.executionRepository = executionRepository;
        this.scoreCalculator = scoreCalculator;
        this.clock = clock;
    }

    @Transactional
    public AssessmentRepository.QuizAnswer answer(
            UUID attemptId,
            String questionId,
            Integer selectedChoice,
            String answerText
    ) {
        Attempt attempt = requireInProgress(attemptId);
        LabContent lab = requireLab(attempt.labCode());
        LabContent.QuizQuestion question = lab.quiz().stream()
                .filter(candidate -> candidate.code().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "QUIZ_QUESTION_NOT_FOUND", "Question introuvable : " + questionId));

        BigDecimal score;
        String feedback;
        if ("SINGLE_CHOICE".equals(question.type())) {
            if (selectedChoice == null || selectedChoice < 0 || selectedChoice >= question.choices().size()) {
                throw new IllegalArgumentException("Un choix valide est obligatoire.");
            }
            boolean correct = selectedChoice.equals(question.correctChoice());
            score = correct ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
            feedback = correct ? "Bonne réponse." : "Réponse incorrecte. Relis le cours avant de réessayer.";
        } else if ("FREE_TEXT".equals(question.type())) {
            if (answerText == null || answerText.isBlank()) {
                throw new IllegalArgumentException("Une réponse rédigée est obligatoire.");
            }
            long matched = question.expectedKeywords().stream()
                    .map(this::normalize)
                    .filter(normalize(answerText)::contains)
                    .count();
            score = question.expectedKeywords().isEmpty()
                    ? BigDecimal.valueOf(100)
                    : BigDecimal.valueOf(matched)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(question.expectedKeywords().size()), 2, RoundingMode.HALF_UP);
            feedback = matched == question.expectedKeywords().size()
                    ? "Les idées essentielles sont présentes."
                    : "Réponse enregistrée. Certaines idées essentielles restent à préciser.";
        } else {
            throw new IllegalStateException("Type de question non pris en charge : " + question.type());
        }

        return assessmentRepository.saveAnswer(new AssessmentRepository.QuizAnswer(
                UUID.randomUUID(), attemptId, questionId, selectedChoice, answerText, score, feedback, Instant.now(clock)));
    }

    @Transactional
    public AssessmentRepository.Checklist saveChecklist(UUID attemptId, List<Boolean> completed) {
        Attempt attempt = requireInProgress(attemptId);
        LabContent lab = requireLab(attempt.labCode());
        if (completed == null || completed.size() != lab.checklist().size() || completed.isEmpty()) {
            throw new IllegalArgumentException("La checklist doit contenir exactement " + lab.checklist().size() + " réponses.");
        }
        int completedItems = (int) completed.stream().filter(Boolean.TRUE::equals).count();
        return assessmentRepository.saveChecklist(new AssessmentRepository.Checklist(
                attemptId, completedItems, completed.size(), List.copyOf(completed), Instant.now(clock)));
    }

    public AssessmentState state(UUID attemptId) {
        attemptService.get(attemptId);
        return new AssessmentState(
                assessmentRepository.findAnswers(attemptId),
                assessmentRepository.findChecklist(attemptId).map(AssessmentRepository.Checklist::completed).orElse(List.of()));
    }

    @Transactional
    public CompletionResult complete(UUID attemptId) {
        Attempt attempt = requireInProgress(attemptId);
        LabContent lab = requireLab(attempt.labCode());
        Map<String, AssessmentRepository.QuizAnswer> answers = assessmentRepository.findAnswers(attemptId).stream()
                .collect(Collectors.toMap(AssessmentRepository.QuizAnswer::questionId, Function.identity()));
        List<String> missing = lab.quiz().stream()
                .map(LabContent.QuizQuestion::code)
                .filter(code -> !answers.containsKey(code))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Réponds à toutes les questions avant de terminer : " + String.join(", ", missing));
        }

        var execution = executionRepository.findLatestByAttemptId(attemptId)
                .orElseThrow(() -> new IllegalStateException("Exécute le code au moins une fois avant de terminer."));
        BigDecimal executionScore = execution.status() == ExecutionStatus.SUCCESS
                ? BigDecimal.valueOf(100)
                : BigDecimal.ZERO;
        BigDecimal quizScore = average(lab.quiz(), answers, "SINGLE_CHOICE");
        BigDecimal connectionScore = average(lab.quiz(), answers, "FREE_TEXT");
        BigDecimal selfAssessmentScore = assessmentRepository.findChecklist(attemptId)
                .orElseThrow(() -> new IllegalStateException("Complète la checklist avant de terminer."))
                .score();

        ScoreCalculator.ScoreResult calculated = scoreCalculator.calculate(new ScoreCalculator.ScoreInput(
                executionScore,
                quizScore,
                executionScore,
                connectionScore,
                selfAssessmentScore,
                lab.threshold()));
        AttemptRepository.ScoreBreakdown breakdown = new AttemptRepository.ScoreBreakdown(
                executionScore, quizScore, executionScore, connectionScore, selfAssessmentScore, SCORE_VERSION);
        Attempt completed = attemptService.complete(attemptId, calculated.score(), breakdown, lab.threshold());

        Instant now = Instant.now(clock);
        String reviewReason = completed.status() == AttemptStatus.COMPLETED_BELOW_THRESHOLD
                ? "Score " + calculated.score() + " % sous le seuil recommandé de " + lab.threshold() + " %."
                : "Consolider les concepts du laboratoire avec la répétition espacée.";
        assessmentRepository.createReview(
                attemptId,
                lab.code(),
                now.plus(1, ChronoUnit.DAYS),
                reviewReason,
                now);
        return new CompletionResult(completed, breakdown, lab.threshold(), true);
    }

    private Attempt requireInProgress(UUID attemptId) {
        Attempt attempt = attemptService.get(attemptId);
        if (attempt.status() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cette tentative est déjà terminée.");
        }
        return attempt;
    }

    private LabContent requireLab(String labCode) {
        return labCatalog.findByCode(labCode)
                .orElseThrow(() -> new ResourceNotFoundException("LAB_NOT_FOUND", "Laboratoire introuvable : " + labCode));
    }

    private BigDecimal average(
            List<LabContent.QuizQuestion> questions,
            Map<String, AssessmentRepository.QuizAnswer> answers,
            String type
    ) {
        List<BigDecimal> scores = questions.stream()
                .filter(question -> type.equals(question.type()))
                .map(question -> answers.get(question.code()).score())
                .toList();
        if (scores.isEmpty()) {
            return BigDecimal.valueOf(100);
        }
        return scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public record CompletionResult(
            Attempt attempt,
            AttemptRepository.ScoreBreakdown breakdown,
            int threshold,
            boolean reviewScheduled
    ) {
    }

    public record AssessmentState(List<AssessmentRepository.QuizAnswer> answers, List<Boolean> checklist) {
    }
}

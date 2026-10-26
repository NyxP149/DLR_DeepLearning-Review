package com.dlr.assessment.application;

import com.dlr.assessment.domain.QuizGrader;
import com.dlr.assessment.domain.ScoreCalculator;
import com.dlr.catalog.application.LabCatalog;
import com.dlr.mastery.application.ReviewService;
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
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    /** Version des nouvelles tentatives ; une tentative déjà notée garde sa version lors d'un recalcul. */
    private static final String CURRENT_SCORE_VERSION = "V2";
    private static final String LEGACY_SCORE_VERSION = "V1";
    /** Part de la pratique conservée selon le plus haut indice demandé (0 à 3). */
    private static final int[] PRACTICE_AFTER_HINT_LEVEL = {100, 85, 65, 40};

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
    public AnswerResult answer(
            UUID attemptId,
            String questionId,
            Integer selectedChoice,
            String answerText,
            Integer confidence
    ) {
        if (confidence != null && (confidence < 1 || confidence > 3)) {
            throw new IllegalArgumentException("La confiance doit être comprise entre 1 et 3.");
        }
        Attempt attempt = requireAttempt(attemptId);
        LabContent lab = requireLab(attempt.labCode());
        LabContent.QuizQuestion question = lab.quiz().stream()
                .filter(candidate -> candidate.code().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "QUIZ_QUESTION_NOT_FOUND", "Question introuvable : " + questionId));

        QuizGrader.Grade grade = QuizGrader.grade(question, selectedChoice, answerText);
        AssessmentRepository.QuizAnswer saved = new AssessmentRepository.QuizAnswer(
                UUID.randomUUID(), attemptId, questionId, selectedChoice, answerText, grade.score(), grade.feedback(),
                Instant.now(clock), confidence);
        assessmentRepository.logAnswer(saved);
        assessmentRepository.saveAnswer(saved);
        return AnswerResult.of(saved, question, grade.missingIdeas());
    }

    /** Enregistre le niveau d'indice demandé : il réduit la part « sans aide » de la pratique du score V2. */
    @Transactional
    public void recordHint(UUID attemptId, int level) {
        requireAttempt(attemptId);
        assessmentRepository.recordHint(attemptId, level, Instant.now(clock));
    }

    @Transactional
    public void removeAnswer(UUID attemptId, String questionId) {
        requireAttempt(attemptId);
        assessmentRepository.deleteAnswer(attemptId, questionId);
    }

    @Transactional
    public AssessmentRepository.Checklist saveChecklist(UUID attemptId, List<Boolean> completed) {
        Attempt attempt = requireAttempt(attemptId);
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
        Attempt attempt = requireAttempt(attemptId);
        boolean firstCompletion = attempt.completedAt() == null;
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
        boolean legacy = LEGACY_SCORE_VERSION.equals(attempt.scoreVersion());
        String scoreVersion = legacy ? LEGACY_SCORE_VERSION : CURRENT_SCORE_VERSION;
        // V2 : seule la première réponse de chaque question compte, pas celle donnée après avoir lu la correction.
        Map<String, BigDecimal> firstScores = legacy ? Map.of() : assessmentRepository.firstScores(attemptId);
        BigDecimal quizScore = average(lab.quiz(), answers, firstScores, "SINGLE_CHOICE");
        BigDecimal connectionScore = average(lab.quiz(), answers, firstScores, "FREE_TEXT");
        BigDecimal practiceScore = legacy || execution.status() != ExecutionStatus.SUCCESS
                ? executionScore
                : BigDecimal.valueOf(PRACTICE_AFTER_HINT_LEVEL[assessmentRepository.highestHintLevel(attemptId)]);
        BigDecimal selfAssessmentScore = assessmentRepository.findChecklist(attemptId)
                .orElseThrow(() -> new IllegalStateException("Complète la checklist avant de terminer."))
                .score();

        ScoreCalculator.ScoreResult calculated = scoreCalculator.calculate(new ScoreCalculator.ScoreInput(
                executionScore,
                quizScore,
                practiceScore,
                connectionScore,
                selfAssessmentScore,
                lab.threshold()), legacy ? ScoreCalculator.V1 : ScoreCalculator.V2);
        AttemptRepository.ScoreBreakdown breakdown = new AttemptRepository.ScoreBreakdown(
                executionScore, quizScore, practiceScore, connectionScore, selfAssessmentScore, scoreVersion);
        Attempt completed = attemptService.complete(attemptId, calculated.score(), breakdown, lab.threshold());

        boolean reviewScheduled = firstCompletion && !assessmentRepository.hasPendingReview(lab.code());
        if (reviewScheduled) {
            Instant now = Instant.now(clock);
            assessmentRepository.createReview(
                    attemptId,
                    lab.code(),
                    now.plus(1, ChronoUnit.DAYS),
                    ReviewService.initialReason(calculated.score(), lab.threshold()),
                    now);
        }
        return new CompletionResult(completed, breakdown, lab.threshold(), reviewScheduled);
    }

    private Attempt requireAttempt(UUID attemptId) {
        return attemptService.get(attemptId);
    }

    private LabContent requireLab(String labCode) {
        return labCatalog.findByCode(labCode)
                .orElseThrow(() -> new ResourceNotFoundException("LAB_NOT_FOUND", "Laboratoire introuvable : " + labCode));
    }

    private BigDecimal average(
            List<LabContent.QuizQuestion> questions,
            Map<String, AssessmentRepository.QuizAnswer> answers,
            Map<String, BigDecimal> firstScores,
            String type
    ) {
        List<BigDecimal> scores = questions.stream()
                .filter(question -> type.equals(question.type()))
                .map(question -> firstScores.getOrDefault(question.code(), answers.get(question.code()).score()))
                .toList();
        if (scores.isEmpty()) {
            return BigDecimal.valueOf(100);
        }
        return scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    /** Réponse enregistrée plus ce qui peut être révélé une fois la question traitée. */
    public record AnswerResult(
            AssessmentRepository.QuizAnswer answer,
            boolean correct,
            Integer correctChoice,
            String explanation,
            String modelAnswer,
            List<String> missingIdeas
    ) {
        static AnswerResult of(
                AssessmentRepository.QuizAnswer answer, LabContent.QuizQuestion question, List<String> missingIdeas) {
            boolean correct = answer.score().compareTo(BigDecimal.valueOf(100)) >= 0;
            return new AnswerResult(answer, correct, question.correctChoice(), question.explanation(),
                    question.modelAnswer(), missingIdeas);
        }
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

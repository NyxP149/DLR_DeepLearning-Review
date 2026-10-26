package com.dlr.adaptation.application;

import com.dlr.assessment.application.PretestService;
import com.dlr.catalog.application.LabCatalog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gain du quiz de départ, tous laboratoires confondus : ce que le cours change entre ce que tu savais avant
 * (réponses du quiz de départ) et ce que tu réussis ensuite (première réponse du quiz final, mêmes questions).
 */
@Service
public class PretestGainService {

    static final int MIN_LABS = 3;
    static final int MIN_QUESTIONS = 9;
    static final int KNOWN_AT_START_PERCENT = 80;
    static final int STRONG_GAIN_POINTS = 25;
    static final int MODERATE_GAIN_POINTS = 10;
    static final int MAX_LABS = 12;

    private final JdbcTemplate jdbcTemplate;
    private final PretestService pretestService;
    private final LabCatalog catalog;

    public PretestGainService(JdbcTemplate jdbcTemplate, PretestService pretestService, LabCatalog catalog) {
        this.jdbcTemplate = jdbcTemplate;
        this.pretestService = pretestService;
        this.catalog = catalog;
    }

    public PretestGain gain() {
        List<String> answeredLabs = jdbcTemplate.queryForList(
                "select lab_code from lab_pretest where status = 'ANSWERED' order by lab_code", String.class);
        Integer skipped = jdbcTemplate.queryForObject("select count(*) from lab_pretest where status = 'SKIPPED'", Integer.class);

        List<LabGain> labs = new ArrayList<>();
        int questions = 0;
        int atStart = 0;
        int atEnd = 0;
        int learned = 0;
        int lost = 0;
        int unknown = 0;
        for (String code : answeredLabs) {
            var lab = catalog.findByCode(code);
            if (lab.isEmpty()) continue;
            PretestService.Gain gain = pretestService.get(code).gain();
            if (gain == null) continue;
            questions += gain.total();
            atStart += gain.correctAtStart();
            atEnd += gain.correctAtEnd();
            learned += gain.learned();
            lost += gain.lost();
            unknown += gain.unknownAtStart();
            labs.add(new LabGain(code, lab.get().title(), gain.total(), gain.correctAtStart(), gain.correctAtEnd(),
                    percent(gain.correctAtEnd(), gain.total()) - percent(gain.correctAtStart(), gain.total())));
        }
        int startPercent = percent(atStart, questions);
        int endPercent = percent(atEnd, questions);
        int gainPoints = endPercent - startPercent;
        Verdict verdict = verdict(labs.size(), questions, startPercent, endPercent, gainPoints);
        List<LabGain> shown = labs.stream()
                .sorted(Comparator.comparingInt(LabGain::gainPoints).reversed().thenComparing(LabGain::labCode))
                .limit(MAX_LABS).toList();
        return new PretestGain(answeredLabs.size(), skipped == null ? 0 : skipped, labs.size(), questions,
                startPercent, endPercent, gainPoints, learned, lost, percent(unknown, questions),
                verdict.code(), verdict.message(), shown);
    }

    private Verdict verdict(int labs, int questions, int start, int end, int gain) {
        if (labs < MIN_LABS || questions < MIN_QUESTIONS) {
            return new Verdict("INSUFFICIENT_DATA", "Pas encore assez de laboratoires comparés (" + labs + " sur " + MIN_LABS
                    + ") pour mesurer ce que le cours t'apporte. Réponds au quiz de départ avant les prochains cours, puis termine-les.");
        }
        if (start >= KNOWN_AT_START_PERCENT) {
            return new Verdict("ALREADY_KNOWN", "Tu réussis déjà " + start + " % des questions avant le cours : ces notions te sont "
                    + "plutôt acquises, tu peux avancer plus vite avec les défis autonomes.");
        }
        if (gain >= STRONG_GAIN_POINTS) {
            return new Verdict("STRONG_GAIN", "Le cours te fait vraiment progresser : de " + start + " % à " + end
                    + " % de réussite sur les mêmes questions (+" + gain + " points).");
        }
        if (gain >= MODERATE_GAIN_POINTS) {
            return new Verdict("MODERATE_GAIN", "Le cours t'apporte un gain réel mais modeste : de " + start + " % à " + end
                    + " % (+" + gain + " points). Les révisions consolideront ce qui reste fragile.");
        }
        return new Verdict("LOW_GAIN", "Le cours ne change pas encore assez tes réponses : " + start + " % avant, " + end
                + " % après. Relis les leçons plus lentement ou demande un autre angle au tuteur.");
    }

    private static int percent(long part, long whole) {
        return whole == 0 ? 0 : (int) Math.round(part * 100.0 / whole);
    }

    private record Verdict(String code, String message) {
    }

    public record PretestGain(
            int pretestsAnswered,
            int pretestsSkipped,
            int labsCompared,
            int questions,
            int startCorrectPercent,
            int endCorrectPercent,
            int gainPoints,
            int learned,
            int lost,
            int unknownAtStartPercent,
            String verdict,
            String message,
            List<LabGain> labs
    ) {
    }

    public record LabGain(String labCode, String title, int total, int correctAtStart, int correctAtEnd, int gainPoints) {
    }
}

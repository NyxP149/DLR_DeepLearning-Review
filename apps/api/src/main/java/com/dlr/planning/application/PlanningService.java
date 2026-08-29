package com.dlr.planning.application;

import com.dlr.profile.application.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PlanningService {

    private final JdbcTemplate jdbcTemplate;
    private final ProfileService profileService;
    private final Clock clock;

    @Autowired
    public PlanningService(JdbcTemplate jdbcTemplate, ProfileService profileService) {
        this(jdbcTemplate, profileService, Clock.systemDefaultZone());
    }

    PlanningService(JdbcTemplate jdbcTemplate, ProfileService profileService, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.profileService = profileService;
        this.clock = clock;
    }

    public CalendarView calendar(int days) {
        if (days < 1 || days > 31) {
            throw new IllegalArgumentException("Le calendrier accepte de 1 à 31 jours.");
        }
        LocalDate start = LocalDate.now(clock);
        LocalDate end = start.plusDays(days - 1L);
        Map<LocalDate, StoredSession> stored = load(start, end).stream()
                .collect(Collectors.toMap(StoredSession::date, session -> session));
        ProfileService.Profile profile = profileService.get();
        List<StudySession> sessions = IntStream.range(0, days)
                .mapToObj(start::plusDays)
                .map(date -> merge(date, profile, stored.get(date)))
                .toList();
        int planned = sessions.stream().mapToInt(StudySession::plannedMinutes).sum();
        int actual = sessions.stream().mapToInt(StudySession::actualMinutes).sum();
        return new CalendarView(start, end, planned, actual, sessions);
    }

    @Transactional
    public StudySession record(LocalDate date, SessionRecord request) {
        LocalDate today = LocalDate.now(clock);
        if (date.isBefore(today.minusDays(365)) || date.isAfter(today.plusDays(30))) {
            throw new IllegalArgumentException("La date de séance est hors de la plage autorisée.");
        }
        ProfileService.Profile profile = profileService.get();
        int planned = plannedMinutes(date, profile);
        String reward = request.reward() == null || request.reward().isBlank() ? null : request.reward().trim();
        if (reward != null && reward.length() > 120) {
            throw new IllegalArgumentException("La récompense ne peut pas dépasser 120 caractères.");
        }
        String status = request.actualMinutes() == 0 ? "SKIPPED" : "COMPLETED";
        Instant now = Instant.now(clock);
        jdbcTemplate.update("delete from study_session where session_date = ?", Date.valueOf(date));
        jdbcTemplate.update(
                """
                insert into study_session
                    (id, session_date, planned_minutes, actual_minutes, status, reward, completed_at)
                values (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), Date.valueOf(date), planned, request.actualMinutes(), status, reward,
                Timestamp.from(now));
        return new StudySession(date, planned, request.actualMinutes(), status, reward, now);
    }

    private StudySession merge(LocalDate date, ProfileService.Profile profile, StoredSession stored) {
        int planned = plannedMinutes(date, profile);
        if (stored == null) {
            return new StudySession(date, planned, 0, "PLANNED", null, null);
        }
        return new StudySession(date, stored.plannedMinutes(), stored.actualMinutes(), stored.status(),
                stored.reward(), stored.completedAt());
    }

    private int plannedMinutes(LocalDate date, ProfileService.Profile profile) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
                ? profile.weekendMinutes()
                : profile.weekdayMinutes();
    }

    private List<StoredSession> load(LocalDate start, LocalDate end) {
        return jdbcTemplate.query(
                """
                select session_date, planned_minutes, actual_minutes, status, reward, completed_at
                from study_session where session_date between ? and ? order by session_date
                """,
                (result, row) -> new StoredSession(
                        result.getDate("session_date").toLocalDate(), result.getInt("planned_minutes"),
                        result.getInt("actual_minutes"), result.getString("status"), result.getString("reward"),
                        result.getTimestamp("completed_at").toInstant()),
                Date.valueOf(start), Date.valueOf(end));
    }

    public record SessionRecord(@jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(480) int actualMinutes,
                                String reward) {
    }

    public record CalendarView(LocalDate start, LocalDate end, int plannedMinutes, int actualMinutes,
                               List<StudySession> sessions) {
    }

    public record StudySession(LocalDate date, int plannedMinutes, int actualMinutes, String status,
                               String reward, Instant completedAt) {
    }

    private record StoredSession(LocalDate date, int plannedMinutes, int actualMinutes, String status,
                                 String reward, Instant completedAt) {
    }
}

package com.dlr.profile.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final JdbcTemplate jdbcTemplate;

    public ProfileService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Profile get() {
        return jdbcTemplate.query(
                        "select display_name, target_months, weekday_minutes, weekend_minutes from user_profile order by id limit 1",
                        (result, row) -> new Profile(
                                result.getString("display_name"), result.getInt("target_months"),
                                result.getInt("weekday_minutes"), result.getInt("weekend_minutes")))
                .stream().findFirst().orElseThrow(() -> new IllegalStateException("Le profil local n'est pas initialisé."));
    }

    @Transactional
    public Profile update(Profile request) {
        int updated = jdbcTemplate.update(
                """
                update user_profile set display_name = ?, target_months = ?, weekday_minutes = ?, weekend_minutes = ?
                where id = (select id from user_profile order by id limit 1)
                """,
                request.displayName().trim(), request.targetMonths(), request.weekdayMinutes(), request.weekendMinutes());
        if (updated != 1) {
            throw new IllegalStateException("Le profil local n'est pas initialisé.");
        }
        return get();
    }

    public record Profile(
            @NotBlank @Size(max = 80) String displayName,
            @Min(3) @Max(4) int targetMonths,
            @Min(15) @Max(240) int weekdayMinutes,
            @Min(15) @Max(240) int weekendMinutes
    ) {
    }
}

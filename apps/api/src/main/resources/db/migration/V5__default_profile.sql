insert into user_profile (display_name, target_months, weekday_minutes, weekend_minutes)
select 'Apprenant DLR', 4, 90, 60
where not exists (select 1 from user_profile);

-- La sauvegarde automatique du texte pendant la frappe était journalisée comme une vraie réponse : la « première
-- réponse » d'une question rédigée était alors un brouillon partiel (souvent 0 %).
-- Un envoi réel (« Vérifier ») porte toujours une confiance ; un brouillon n'en porte jamais. On retire donc les lignes
-- sans confiance qui sont suivies d'une autre ligne de la même question dans la même tentative.
delete from quiz_answer_log l
where l.confidence is null
  and exists (
      select 1 from quiz_answer_log later
      where later.attempt_id = l.attempt_id
        and later.question_id = l.question_id
        and (later.answered_at > l.answered_at or (later.answered_at = l.answered_at and later.id > l.id))
  );

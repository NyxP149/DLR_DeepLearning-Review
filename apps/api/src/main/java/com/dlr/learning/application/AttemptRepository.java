package com.dlr.learning.application;

import com.dlr.learning.domain.Attempt;

import java.util.Optional;
import java.util.UUID;

public interface AttemptRepository {

    Attempt save(Attempt attempt);

    Optional<Attempt> findById(UUID id);
}


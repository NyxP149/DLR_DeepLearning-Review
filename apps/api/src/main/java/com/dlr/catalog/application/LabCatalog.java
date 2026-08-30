package com.dlr.catalog.application;

import com.dlr.catalog.domain.LabContent;
import com.dlr.catalog.domain.PathDescriptor;

import java.util.List;
import java.util.Optional;

public interface LabCatalog {

    List<LabContent> findAll();

    Optional<LabContent> findByCode(String code);

    List<PathDescriptor> findPaths();
}

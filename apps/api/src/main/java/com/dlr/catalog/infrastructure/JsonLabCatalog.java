package com.dlr.catalog.infrastructure;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class JsonLabCatalog implements LabCatalog {

    private static final String CONTENT_PATTERN = "classpath*:content/**/*.json";

    private final List<LabContent> labs;

    public JsonLabCatalog(ObjectMapper objectMapper) {
        this.labs = loadLabs(objectMapper);
    }

    @Override
    public List<LabContent> findAll() {
        return labs;
    }

    @Override
    public Optional<LabContent> findByCode(String code) {
        return labs.stream()
                .filter(lab -> lab.code().equalsIgnoreCase(code))
                .findFirst();
    }

    private List<LabContent> loadLabs(ObjectMapper objectMapper) {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(CONTENT_PATTERN);
            if (resources.length == 0) {
                throw new IllegalStateException("Aucun contenu pédagogique trouvé dans " + CONTENT_PATTERN);
            }

            return Arrays.stream(resources)
                    .map(resource -> readLab(objectMapper, resource))
                    .sorted(Comparator.comparing(LabContent::language).thenComparingInt(LabContent::number))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de parcourir le contenu pédagogique", exception);
        }
    }

    private LabContent readLab(ObjectMapper objectMapper, Resource resource) {
        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(input, LabContent.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Contenu pédagogique invalide : " + resource.getDescription(), exception);
        }
    }
}


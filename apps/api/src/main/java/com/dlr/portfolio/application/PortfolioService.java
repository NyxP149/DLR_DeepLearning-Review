package com.dlr.portfolio.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PortfolioService {

    private static final Pattern SECRET = Pattern.compile(
            "(?is)(-----BEGIN [A-Z ]*PRIVATE KEY-----|(?:api[_-]?key|secret|token|password)\\s*[:=]\\s*[^\\s]{8,})");
    private final JdbcTemplate jdbcTemplate;
    private final LabCatalog catalog;

    public PortfolioService(JdbcTemplate jdbcTemplate, LabCatalog catalog) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalog = catalog;
    }

    public List<Project> list() {
        return jdbcTemplate.query("select id, slug, title, summary, source_lab_codes, status, created_at, updated_at from portfolio_project order by updated_at desc",
                (row, index) -> new Project(row.getObject("id", UUID.class), row.getString("slug"), row.getString("title"),
                        row.getString("summary"), List.of(row.getString("source_lab_codes").split(",")), row.getString("status"),
                        decisions(row.getObject("id", UUID.class)), row.getTimestamp("created_at").toInstant(), row.getTimestamp("updated_at").toInstant()));
    }

    @Transactional
    public Project create(String title, String summary, List<String> labCodes, List<String> decisions) {
        checkPublicText(title + "\n" + summary + "\n" + String.join("\n", decisions));
        List<String> normalizedLabs = labCodes.stream().map(String::strip).distinct().toList();
        if (normalizedLabs.isEmpty()) throw new IllegalArgumentException("Sélectionne au moins un laboratoire comme preuve.");
        normalizedLabs.forEach(this::lab);
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        String slug = uniqueSlug(title);
        jdbcTemplate.update("insert into portfolio_project (id, slug, title, summary, source_lab_codes, status, created_at, updated_at) values (?, ?, ?, ?, ?, 'PRIVATE', ?, ?)",
                id, slug, title.strip(), summary.strip(), String.join(",", normalizedLabs), Timestamp.from(now), Timestamp.from(now));
        for (int i = 0; i < decisions.size(); i++) {
            if (!decisions.get(i).isBlank()) jdbcTemplate.update("insert into portfolio_decision (id, project_id, summary, position) values (?, ?, ?, ?)",
                    UUID.randomUUID(), id, decisions.get(i).strip(), i);
        }
        return find(id);
    }

    public Project find(UUID id) {
        return list().stream().filter(project -> project.id().equals(id)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("PORTFOLIO_PROJECT_NOT_FOUND", "Projet portfolio introuvable : " + id));
    }

    public String markdown(UUID id) {
        Project project = find(id);
        StringBuilder output = new StringBuilder("# ").append(project.title()).append("\n\n")
                .append(project.summary()).append("\n\n## Compétences démontrées\n\n");
        for (String code : project.labCodes()) {
            LabContent lab = lab(code);
            output.append("### ").append(lab.title()).append(" (`").append(code).append("`)\n\n");
            for (LabContent.KeyConcept concept : lab.keyConcepts()) {
                output.append("- **").append(concept.name()).append("** — ").append(concept.masteryProof()).append("\n");
            }
            output.append("\n");
        }
        if (!project.decisions().isEmpty()) {
            output.append("## Décisions techniques\n\n");
            project.decisions().forEach(decision -> output.append("- ").append(decision).append("\n"));
            output.append("\n");
        }
        output.append("## Exécution locale\n\nCe dossier présente des preuves sélectionnées depuis DLR. Ajoutez ici les commandes propres au dépôt exporté.\n\n")
                .append("---\nExport DLR privé par défaut. Aucun score, profil personnel ou échange avec l’IA n’est inclus.\n");
        String markdown = output.toString();
        checkPublicText(markdown);
        return markdown;
    }

    @Transactional
    public byte[] zip(UUID id) {
        Project project = find(id);
        String markdown = markdown(id);
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            entry(zip, "README.md", markdown);
            entry(zip, ".gitignore", ".env\n*.key\n*.pem\n.idea/\n.vscode/\ntarget/\ndist/\n");
            entry(zip, "docs/PRIVACY.md", "# Confidentialité\n\nCet export exclut les scores, le profil, les conversations IA et les données de synchronisation.\n");
            zip.finish();
            byte[] result = bytes.toByteArray();
            jdbcTemplate.update("insert into portfolio_export (id, project_id, format, content_hash, included_content, created_at) values (?, ?, 'ZIP', ?, ?, ?)",
                    UUID.randomUUID(), id, hash(result), "README.md,.gitignore,docs/PRIVACY.md", Timestamp.from(Instant.now()));
            return result;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Impossible de produire l'archive portfolio.", exception);
        }
    }

    private List<String> decisions(UUID id) {
        return jdbcTemplate.queryForList("select summary from portfolio_decision where project_id = ? order by position", String.class, id);
    }

    private LabContent lab(String code) {
        return catalog.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("LAB_NOT_FOUND", "Laboratoire introuvable : " + code));
    }

    private String uniqueSlug(String title) {
        String base = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) base = "projet-dlr";
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void checkPublicText(String value) {
        if (SECRET.matcher(value).find()) throw new IllegalArgumentException("Export bloqué : un secret potentiel a été détecté.");
    }

    private void entry(ZipOutputStream zip, String name, String content) throws java.io.IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String hash(byte[] value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); }
        catch (java.security.NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 indisponible", exception); }
    }

    public record Project(UUID id, String slug, String title, String summary, List<String> labCodes,
                          String status, List<String> decisions, Instant createdAt, Instant updatedAt) {}
}

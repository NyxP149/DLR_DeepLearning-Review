package com.dlr.sync.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SyncService {

    private static final int MAX_OPERATIONS = 50;
    private static final int MAX_PAYLOAD_LENGTH = 131_072;
    private static final Set<String> ENTITY_TYPES = Set.of(
            "PROFILE", "ATTEMPT", "SUBMISSION", "QUIZ_ANSWER", "CHECKLIST",
            "STUDY_SESSION", "MASTERY", "REVIEW", "PREFERENCE", "PORTFOLIO");

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final String pairingCode;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public SyncService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper,
                       @Value("${dlr.sync.pairing-code:}") String pairingCode) {
        this(jdbcTemplate, objectMapper, pairingCode, Clock.systemUTC());
    }

    SyncService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, String pairingCode, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.pairingCode = pairingCode == null ? "" : pairingCode;
        this.clock = clock;
    }

    @Transactional
    public PairingResult pair(String deviceName, String suppliedCode, String remoteAddress) {
        requirePairingAuthorization(suppliedCode, remoteAddress);
        String token = token();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now(clock);
        jdbcTemplate.update(
                "insert into sync_device (id, name, token_hash, status, paired_at, last_seen_at) values (?, ?, ?, 'ACTIVE', ?, ?)",
                id, deviceName.strip(), hash(token), Timestamp.from(now), Timestamp.from(now));
        return new PairingResult(id, deviceName.strip(), token, now);
    }

    @Transactional
    public List<DeviceView> devices(String authorization) {
        authenticate(authorization);
        return jdbcTemplate.query(
                "select id, name, status, paired_at, last_seen_at from sync_device order by paired_at",
                (result, row) -> new DeviceView(
                        result.getObject("id", UUID.class), result.getString("name"), result.getString("status"),
                        result.getTimestamp("paired_at").toInstant(), result.getTimestamp("last_seen_at").toInstant()));
    }

    @Transactional
    public void revoke(String authorization, UUID deviceId) {
        authenticate(authorization);
        if (jdbcTemplate.update("update sync_device set status = 'REVOKED' where id = ? and status = 'ACTIVE'", deviceId) == 0) {
            throw new IllegalArgumentException("Appareil actif introuvable : " + deviceId);
        }
    }

    @Transactional
    public synchronized PushResult push(String authorization, List<IncomingChange> changes) {
        Device device = authenticate(authorization);
        if (changes.isEmpty() || changes.size() > MAX_OPERATIONS) {
            throw new IllegalArgumentException("Une synchronisation doit contenir entre 1 et 50 opérations.");
        }
        int accepted = 0;
        int duplicates = 0;
        int conflicts = 0;
        long cursor = currentCursor();
        for (IncomingChange change : changes) {
            validate(change);
            if (exists(change.operationId())) {
                duplicates++;
                continue;
            }
            String payload = json(change.payload());
            String contentHash = hash(payload);
            StoredChange existing = sameVersion(change.entityType(), change.entityId(), change.entityVersion());
            cursor++;
            Instant now = Instant.now(clock);
            jdbcTemplate.update(
                    """
                    insert into sync_change
                        (operation_id, device_id, entity_type, entity_id, entity_version, logical_time,
                         content_hash, payload_json, client_modified_at, created_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    change.operationId(), device.id(), change.entityType(), change.entityId(), change.entityVersion(),
                    cursor, contentHash, payload, Timestamp.from(change.clientModifiedAt()), Timestamp.from(now));
            if (existing != null && !existing.contentHash().equals(contentHash)) {
                jdbcTemplate.update(
                        """
                        insert into sync_conflict
                            (id, entity_type, entity_id, entity_version, existing_operation_id,
                             incoming_operation_id, detected_at, resolved_at)
                        values (?, ?, ?, ?, ?, ?, ?, null)
                        """,
                        UUID.randomUUID(), change.entityType(), change.entityId(), change.entityVersion(),
                        existing.operationId(), change.operationId(), Timestamp.from(now));
                conflicts++;
            }
            accepted++;
        }
        return new PushResult(accepted, duplicates, conflicts, cursor);
    }

    @Transactional
    public ChangePage pull(String authorization, long after, int limit) {
        authenticate(authorization);
        if (after < 0 || limit < 1 || limit > 200) {
            throw new IllegalArgumentException("Le curseur ou la limite de synchronisation est invalide.");
        }
        List<OutgoingChange> changes = jdbcTemplate.query(
                """
                select operation_id, device_id, entity_type, entity_id, entity_version, logical_time,
                       payload_json, client_modified_at
                from sync_change where logical_time > ? order by logical_time limit ?
                """,
                (result, row) -> new OutgoingChange(
                        result.getObject("operation_id", UUID.class), result.getObject("device_id", UUID.class),
                        result.getString("entity_type"), result.getString("entity_id"),
                        result.getLong("entity_version"), result.getLong("logical_time"),
                        parse(result.getString("payload_json")), result.getTimestamp("client_modified_at").toInstant()),
                after, limit);
        long cursor = changes.isEmpty() ? after : changes.getLast().logicalTime();
        return new ChangePage(cursor, changes.size() == limit, changes);
    }

    @Transactional
    public List<ConflictView> conflicts(String authorization) {
        authenticate(authorization);
        return jdbcTemplate.query(
                """
                select id, entity_type, entity_id, entity_version, existing_operation_id,
                       incoming_operation_id, detected_at
                from sync_conflict where resolved_at is null order by detected_at
                """,
                (result, row) -> new ConflictView(
                        result.getObject("id", UUID.class), result.getString("entity_type"),
                        result.getString("entity_id"), result.getLong("entity_version"),
                        result.getObject("existing_operation_id", UUID.class),
                        result.getObject("incoming_operation_id", UUID.class),
                        result.getTimestamp("detected_at").toInstant()));
    }

    private Device authenticate(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() < 20) {
            throw new SyncAuthenticationException("Jeton d'appareil absent ou invalide.");
        }
        String tokenHash = hash(authorization.substring(7).strip());
        List<Device> devices = jdbcTemplate.query(
                "select id, name from sync_device where token_hash = ? and status = 'ACTIVE'",
                (result, row) -> new Device(result.getObject("id", UUID.class), result.getString("name")), tokenHash);
        if (devices.isEmpty()) {
            throw new SyncAuthenticationException("Appareil non appairé ou révoqué.");
        }
        Device device = devices.getFirst();
        jdbcTemplate.update("update sync_device set last_seen_at = ? where id = ?",
                Timestamp.from(Instant.now(clock)), device.id());
        return device;
    }

    private void requirePairingAuthorization(String suppliedCode, String remoteAddress) {
        if (pairingCode.isBlank()) {
            if (!("127.0.0.1".equals(remoteAddress) || "0:0:0:0:0:0:0:1".equals(remoteAddress) || "::1".equals(remoteAddress))) {
                throw new SyncAuthenticationException("L'appairage sans code est limité à cette machine.");
            }
            return;
        }
        byte[] expected = pairingCode.getBytes(StandardCharsets.UTF_8);
        byte[] supplied = (suppliedCode == null ? "" : suppliedCode).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, supplied)) {
            throw new SyncAuthenticationException("Code d'appairage invalide.");
        }
    }

    private void validate(IncomingChange change) {
        if (!ENTITY_TYPES.contains(change.entityType())) {
            throw new IllegalArgumentException("Type synchronisable inconnu : " + change.entityType());
        }
        if (change.entityId().isBlank() || change.entityId().length() > 120 || change.entityVersion() < 1) {
            throw new IllegalArgumentException("Identifiant ou version d'entité invalide.");
        }
        if (change.clientModifiedAt().isAfter(Instant.now(clock).plusSeconds(300))) {
            throw new IllegalArgumentException("La date cliente est trop éloignée dans le futur.");
        }
        if (json(change.payload()).length() > MAX_PAYLOAD_LENGTH) {
            throw new IllegalArgumentException("La charge synchronisée dépasse 128 Kio.");
        }
    }

    private boolean exists(UUID operationId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from sync_change where operation_id = ?", Integer.class, operationId);
        return count != null && count > 0;
    }

    private StoredChange sameVersion(String type, String id, long version) {
        List<StoredChange> changes = jdbcTemplate.query(
                """
                select operation_id, content_hash from sync_change
                where entity_type = ? and entity_id = ? and entity_version = ?
                order by logical_time desc limit 1
                """,
                (result, row) -> new StoredChange(
                        result.getObject("operation_id", UUID.class), result.getString("content_hash")),
                type, id, version);
        return changes.isEmpty() ? null : changes.getFirst();
    }

    private long currentCursor() {
        Long cursor = jdbcTemplate.queryForObject("select coalesce(max(logical_time), 0) from sync_change", Long.class);
        return cursor == null ? 0 : cursor;
    }

    private String token() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponible", exception);
        }
    }

    private String json(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Charge JSON invalide.", exception);
        }
    }

    private JsonNode parse(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Charge synchronisée illisible.", exception);
        }
    }

    public record PairingResult(UUID deviceId, String deviceName, String token, Instant pairedAt) {}
    public record DeviceView(UUID id, String name, String status, Instant pairedAt, Instant lastSeenAt) {}
    public record IncomingChange(UUID operationId, String entityType, String entityId, long entityVersion,
                                 JsonNode payload, Instant clientModifiedAt) {}
    public record PushResult(int accepted, int duplicates, int conflicts, long cursor) {}
    public record OutgoingChange(UUID operationId, UUID deviceId, String entityType, String entityId,
                                 long entityVersion, long logicalTime, JsonNode payload, Instant clientModifiedAt) {}
    public record ChangePage(long cursor, boolean hasMore, List<OutgoingChange> changes) {}
    public record ConflictView(UUID id, String entityType, String entityId, long entityVersion,
                               UUID existingOperationId, UUID incomingOperationId, Instant detectedAt) {}
    private record Device(UUID id, String name) {}
    private record StoredChange(UUID operationId, String contentHash) {}
}

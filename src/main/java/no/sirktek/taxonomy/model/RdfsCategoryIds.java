package no.sirktek.taxonomy.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Stable negative Long IDs for RDF-S taxonomy categories, derived from their URIs.
 *
 * <p>RDF-S categories live outside the database, so they have no auto-generated primary
 * key. To let API consumers refer to them with a numeric id (the same shape as DB-backed
 * org categories, which use positive BIGINTs), we hash the URI into a negative long.
 *
 * <p>The hash is the first 8 bytes of SHA-256 over the UTF-8 URI bytes, with the sign
 * bit forced on. This guarantees:
 * <ul>
 *   <li>Determinism: same URI → same ID across services and JVMs.</li>
 *   <li>Disjoint from DB IDs: DB IDs are positive, RDF-S IDs are always negative.</li>
 *   <li>Low collision risk: 63 bits of entropy (~2^31.5 URIs before 50% collision odds).</li>
 * </ul>
 *
 * <p><b>This is a load-bearing contract across services.</b> Any consumer that persists
 * these IDs (caches, exported data, etc.) will break if the algorithm changes — treat
 * changes as a coordinated migration.
 */
public final class RdfsCategoryIds {

    private RdfsCategoryIds() {}

    public static Long negativeIdFromUri(String uri) {
        if (uri == null) {
            return -1L;
        }
        byte[] digest = sha256(uri.getBytes(StandardCharsets.UTF_8));
        long value = 0L;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (digest[i] & 0xFFL);
        }
        return value | Long.MIN_VALUE;
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

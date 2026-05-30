package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a university topic (subject/course).
 *
 * CSV column "Topic" splits on the FIRST space into:
 *   topicCode  (e.g. "COMP1234")
 *   topicName  (e.g. "Introduction to Computing")
 *
 * A Topic has 1..* {@link Availability} entries.
 */
public final class Topic {

    private final String topicCode;
    private final String topicName;

    private final List<Availability> availabilities;

    public Topic(String topicCode, String topicName) {
        this.topicCode     = Objects.requireNonNull(topicCode, "topicCode must not be null").trim();
        this.topicName     = Objects.requireNonNull(topicName, "topicName must not be null").trim();
        this.availabilities = new ArrayList<>();
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    /**
     * Parses a raw topic string, splitting on the first space.
     * Example: "COMP1234 Introduction to Computing"
     *          → code="COMP1234", name="Introduction to Computing"
     */
    public static Topic parse(String raw) {
        Objects.requireNonNull(raw, "raw topic string must not be null");
        String trimmed = raw.trim();
        int idx = trimmed.indexOf(' ');
        if (idx < 0) {
            // No space — treat entire string as code, name is empty
            return new Topic(trimmed, "");
        }
        String code = trimmed.substring(0, idx).trim();
        String name = trimmed.substring(idx + 1).trim();
        return new Topic(code, name);
    }

    // ── Mutation ──────────────────────────────────────────────────────────────

    /** Adds an availability to this topic (building the 1..* relationship). */
    public void addAvailability(Availability availability) {
        Objects.requireNonNull(availability, "availability must not be null");
        availabilities.add(availability);
    }

    /**
     * Finds an existing availability that matches the given raw string,
     * or creates and registers a new one.
     */
    public Availability findOrCreateAvailability(String rawAvailability) {
        Availability candidate = Availability.parse(rawAvailability);
        for (Availability existing : availabilities) {
            if (existing.equals(candidate)) {
                return existing;
            }
        }
        availabilities.add(candidate);
        return candidate;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String             getTopicCode()      { return topicCode; }
    public String             getTopicName()      { return topicName; }
    public List<Availability> getAvailabilities() { return Collections.unmodifiableList(availabilities); }

    /** Full display string, e.g. "COMP1234 Introduction to Computing". */
    public String getFullName() {
        return topicName.isBlank() ? topicCode : topicCode + " " + topicName;
    }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic t)) return false;
        return topicCode.equalsIgnoreCase(t.topicCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicCode.toLowerCase());
    }

    @Override
    public String toString() {
        return getFullName();
    }
}

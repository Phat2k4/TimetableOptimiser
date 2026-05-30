package model;

import java.util.Objects;

/**
 * Represents a named class offering (e.g. "Lecture", "Tutorial") and
 * a specific instance number within that class (e.g. instance 1, 2 …).
 *
 * A ClassOffering is a named slot; the concrete time/place detail lives
 * in the ClassOption → ClassSession chain.
 */
public final class ClassOffering {

    /** Raw class name as read from CSV (e.g. "Lecture", "Tutorial", "Workshop"). */
    private final String className;

    /** Instance number within this class name (e.g. 1, 2, 3). */
    private final int classInstance;

    public ClassOffering(String className, int classInstance) {
        this.className     = Objects.requireNonNull(className, "className must not be null").trim();
        this.classInstance = classInstance;
    }

    // ── Lecture detection ─────────────────────────────────────────────────────

    /**
     * Returns true when this offering is a lecture.
     * Per spec: className starts with "Lecture" (case-insensitive).
     */
    public boolean isLecture() {
        return className.toLowerCase().startsWith("lecture");
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String getClassName()    { return className; }
    public int    getClassInstance(){ return classInstance; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassOffering c)) return false;
        return classInstance == c.classInstance &&
               className.equalsIgnoreCase(c.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className.toLowerCase(), classInstance);
    }

    @Override
    public String toString() {
        return className + " (Instance " + classInstance + ")";
    }
}

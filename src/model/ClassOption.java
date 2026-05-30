package model;

import java.util.Objects;

/**
 * A selectable option for a particular {@link ClassOffering}.
 *
 * In the CSV data each row maps to one ClassOption: it pairs an offering
 * (class name + instance) with exactly one {@link ClassSession} (the concrete
 * time/place slot).
 *
 * Relationship:  Availability → 1..* ClassOption → 1 ClassSession
 */
public final class ClassOption {

    private final ClassOffering classOffering;
    private final ClassSession  classSession;

    public ClassOption(ClassOffering classOffering, ClassSession classSession) {
        this.classOffering = Objects.requireNonNull(classOffering, "classOffering must not be null");
        this.classSession  = Objects.requireNonNull(classSession,  "classSession must not be null");
    }

    // ── Convenience delegation ────────────────────────────────────────────────

    /** True when the underlying offering is a lecture. */
    public boolean isLecture() {
        return classOffering.isLecture();
    }

    /** The name of the class type (e.g. "Lecture", "Tutorial"). */
    public String getClassName() {
        return classOffering.getClassName();
    }

    /** The instance number of the class. */
    public int getClassInstance() {
        return classOffering.getClassInstance();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public ClassOffering getClassOffering() { return classOffering; }
    public ClassSession  getClassSession()  { return classSession; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassOption c)) return false;
        return classOffering.equals(c.classOffering) &&
               classSession.equals(c.classSession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classOffering, classSession);
    }

    @Override
    public String toString() {
        return classOffering + " → " + classSession;
    }
}

package model;

import java.util.Objects;

/**
 * A soft scheduling preference applied to a {@link Timetable}.
 *
 * Preferences are soft constraints — they influence optimisation but are
 * never allowed to violate hard constraints (clash rules).
 *
 * "Evenly Spread" and "Compact" are mutually exclusive; whichever has a
 * lower (higher-priority) {@code priorityOrder} value wins.
 */
public final class Preference {

    // ── Known preference type constants ───────────────────────────────────────

    public static final String TYPE_NO_MORNINGS     = "No Mornings";
    public static final String TYPE_NO_AFTERNOONS   = "No Afternoons";
    public static final String TYPE_EVENLY_SPREAD   = "Evenly Spread";
    public static final String TYPE_COMPACT         = "Compact";
    public static final String TYPE_NO_FRIDAYS      = "No Fridays";

    // ── Fields ────────────────────────────────────────────────────────────────

    /**
     * A descriptive label for this preference (e.g. "No Mornings", "Compact").
     * Case-insensitive for comparison purposes.
     */
    private final String preferenceType;

    /**
     * Rank of this preference: lower number = higher priority (1 = most important).
     */
    private final int priorityOrder;

    public Preference(String preferenceType, int priorityOrder) {
        this.preferenceType = Objects.requireNonNull(preferenceType, "preferenceType must not be null").trim();
        if (priorityOrder < 1) {
            throw new IllegalArgumentException("priorityOrder must be ≥ 1, got: " + priorityOrder);
        }
        this.priorityOrder = priorityOrder;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** True when this preference is of the given type (case-insensitive). */
    public boolean isType(String type) {
        return preferenceType.equalsIgnoreCase(type);
    }

    public boolean isEvenlySpread() { return isType(TYPE_EVENLY_SPREAD); }
    public boolean isCompact()      { return isType(TYPE_COMPACT); }
    public boolean isNoMornings()   { return isType(TYPE_NO_MORNINGS); }
    public boolean isNoAfternoons() { return isType(TYPE_NO_AFTERNOONS); }
    public boolean isNoFridays()    { return isType(TYPE_NO_FRIDAYS); }

    /**
     * Returns true when this preference has higher priority than {@code other}
     * (i.e. lower priorityOrder value).
     */
    public boolean hasHigherPriorityThan(Preference other) {
        return this.priorityOrder < other.priorityOrder;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String getPreferenceType() { return preferenceType; }
    public int    getPriorityOrder()  { return priorityOrder; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Preference p)) return false;
        return priorityOrder == p.priorityOrder &&
               preferenceType.equalsIgnoreCase(p.preferenceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preferenceType.toLowerCase(), priorityOrder);
    }

    @Override
    public String toString() {
        return preferenceType + " (priority " + priorityOrder + ")";
    }
}

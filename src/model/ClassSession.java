package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A single recurring weekly class session.
 *
 * Clash rules (per spec):
 *  - Two sessions on the same day clash when they overlap by ≥ 1 minute.
 *  - Back-to-back sessions at the SAME campus (building) do NOT clash.
 *  - Sessions at DIFFERENT campuses require ≥ 30 min gap (end of earlier → start of later).
 */
public final class ClassSession {

    private final LocalDate dateOfFirstClass;
    private final LocalDate dateOfLastClass;
    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Location  location;

    public ClassSession(LocalDate dateOfFirstClass,
                        LocalDate dateOfLastClass,
                        DayOfWeek day,
                        LocalTime startTime,
                        LocalTime endTime,
                        Location  location) {
        this.dateOfFirstClass = Objects.requireNonNull(dateOfFirstClass);
        this.dateOfLastClass  = Objects.requireNonNull(dateOfLastClass);
        this.day              = Objects.requireNonNull(day);
        this.startTime        = Objects.requireNonNull(startTime);
        this.endTime          = Objects.requireNonNull(endTime);
        this.location         = Objects.requireNonNull(location);
    }

    // ── Clash detection ───────────────────────────────────────────────────────

    /**
     * Returns true when this session clashes with {@code other}.
     *
     * @param other            another session to compare
     * @param allowLectureOverlap if true, a lecture may overlap another lecture
     *                            (caller must still honour this flag at Timetable level)
     * @param thisIsLecture    whether THIS session is a lecture
     * @param otherIsLecture   whether OTHER session is a lecture
     */
    public boolean clashesWith(ClassSession other,
                                boolean allowLectureOverlap,
                                boolean thisIsLecture,
                                boolean otherIsLecture) {
        if (other == null || !this.day.equals(other.day)) return false;

        // Allow lecture-on-lecture overlap when flag is set
        if (allowLectureOverlap && thisIsLecture && otherIsLecture) return false;

        boolean sameCampus = this.location.isSameCampusAs(other.location);

        if (sameCampus) {
            // Back-to-back is fine; only a genuine overlap (≥ 1 min) is a clash
            return overlapsBy(other, 1);
        } else {
            // Different campus: need ≥ 30-min gap
            long gap = gapMinutes(other);
            return gap < 30;
        }
    }

    /**
     * Convenience overload – no lecture-overlap logic.
     */
    public boolean clashesWith(ClassSession other) {
        return clashesWith(other, false, false, false);
    }

    /** Minutes of overlap between this and other (negative = gap). */
    private boolean overlapsBy(ClassSession other, long threshold) {
        LocalTime latestStart  = startTime.isAfter(other.startTime)  ? startTime  : other.startTime;
        LocalTime earliestEnd  = endTime.isBefore(other.endTime)     ? endTime    : other.endTime;
        long overlapMinutes = java.time.Duration.between(latestStart, earliestEnd).toMinutes();
        return overlapMinutes >= threshold;
    }

    /** Gap in minutes between the end of the earlier session and the start of the later one. */
    private long gapMinutes(ClassSession other) {
        // Determine order
        ClassSession earlier, later;
        if (!this.endTime.isAfter(other.startTime)) {
            earlier = this; later = other;
        } else if (!other.endTime.isAfter(this.startTime)) {
            earlier = other; later = this;
        } else {
            // They overlap — gap is 0 (negative actually, treat as 0)
            return 0;
        }
        return java.time.Duration.between(earlier.endTime, later.startTime).toMinutes();
    }

    // ── Time-of-day helpers ───────────────────────────────────────────────────

    /** "Mornings" = starts before 12:00. */
    public boolean isMorning()   { return startTime.isBefore(LocalTime.NOON); }

    /** "Afternoons" = starts at 12:00 or later. */
    public boolean isAfternoon() { return !isMorning(); }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public LocalDate getDateOfFirstClass() { return dateOfFirstClass; }
    public LocalDate getDateOfLastClass()  { return dateOfLastClass; }
    public DayOfWeek getDay()              { return day; }
    public LocalTime getStartTime()        { return startTime; }
    public LocalTime getEndTime()          { return endTime; }
    public Location  getLocation()         { return location; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassSession s)) return false;
        return day.equals(s.day) &&
               startTime.equals(s.startTime) &&
               endTime.equals(s.endTime) &&
               location.equals(s.location) &&
               dateOfFirstClass.equals(s.dateOfFirstClass) &&
               dateOfLastClass.equals(s.dateOfLastClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startTime, endTime, location, dateOfFirstClass, dateOfLastClass);
    }

    @Override
    public String toString() {
        return String.format("%s %s–%s @ %s (%s to %s)",
                day, startTime, endTime, location, dateOfFirstClass, dateOfLastClass);
    }
}

import model.*;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Stateless utility that encapsulates all clash and commute-gap detection logic.
 *
 * <p>All methods are pure functions — they only inspect their arguments and
 * never modify state.
 *
 * <h3>Rules (per spec):</h3>
 * <ul>
 *   <li><b>Time clash</b> — two sessions on the same day overlap by ≥ 1 minute.</li>
 *   <li><b>Commute gap violation</b> — two sessions at different campuses on the same
 *       day have fewer than 30 minutes from the end of the earlier to the start of the
 *       later.</li>
 *   <li><b>Back-to-back same campus</b> — never a violation (gap = 0 minutes is fine).</li>
 *   <li><b>Lecture exemption</b> — when {@code allowLectureOverlap} is set and both
 *       sessions are lectures, neither a time clash nor a commute violation is raised.</li>
 * </ul>
 */
public final class ClashDetector {

    private ClashDetector() {}   // utility class — no instances

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} when sessions {@code a} and {@code b} are on the same
     * day and their time windows overlap by at least 1 minute.
     *
     * <p>This check is campus-agnostic: it fires regardless of whether the
     * sessions are at the same campus or different campuses.
     */
    public static boolean hasTimeClash(ClassSession a, ClassSession b) {
        if (a == null || b == null) return false;
        if (!a.getDay().equals(b.getDay())) return false;
        return overlapMinutes(a, b) >= 1;
    }

    /**
     * Returns {@code true} when sessions {@code a} and {@code b}:
     * <ol>
     *   <li>are on the same day,</li>
     *   <li>are at <em>different</em> campuses (different buildings), and</li>
     *   <li>have fewer than 30 minutes gap between the end of the earlier session
     *       and the start of the later session.</li>
     * </ol>
     *
     * <p>Back-to-back sessions at the <em>same</em> campus always return
     * {@code false} (no violation).
     *
     * <p>Note: overlapping sessions at different campuses also violate this rule
     * (gap is effectively 0).
     */
    public static boolean violatesCommuteGap(ClassSession a, ClassSession b) {
        if (a == null || b == null) return false;
        if (!a.getDay().equals(b.getDay())) return false;
        if (a.getLocation().isSameCampusAs(b.getLocation())) return false;  // same campus – no commute
        return gapMinutes(a, b) < 30;
    }

    /**
     * Returns {@code true} when both sessions are lectures and the supplied
     * {@link Timetable} has {@code allowLectureOverlap} enabled — meaning any
     * overlap between these two sessions should be ignored during validation.
     *
     * @param a    first session
     * @param b    second session
     * @param aIsLecture  whether session {@code a} is a lecture
     * @param bIsLecture  whether session {@code b} is a lecture
     * @param timetable   the timetable whose flag is inspected
     */
    public static boolean isLectureExempt(ClassSession a, ClassSession b,
                                          boolean aIsLecture, boolean bIsLecture,
                                          Timetable timetable) {
        if (timetable == null) return false;
        return timetable.isAllowLectureOverlap() && aIsLecture && bIsLecture;
    }

    /**
     * Convenience overload that derives the "is lecture" flags from
     * {@link ClassOption} objects.
     */
    public static boolean isLectureExempt(ClassOption a, ClassOption b, Timetable timetable) {
        return isLectureExempt(a.getClassSession(), b.getClassSession(),
                               a.isLecture(), b.isLecture(), timetable);
    }

    /**
     * Full combined violation check used by the generator.
     *
     * <p>Returns {@code true} when adding session {@code candidate} to a
     * timetable that already contains {@code existing} would violate a hard
     * constraint — i.e. there is a clash or commute-gap problem that is
     * NOT exempted by the lecture-overlap flag.
     *
     * @param existing    an already-scheduled class option
     * @param candidate   a class option being considered for inclusion
     * @param timetable   the timetable (for lecture-overlap flag)
     */
    public static boolean violatesHardConstraint(ClassOption existing,
                                                  ClassOption candidate,
                                                  Timetable   timetable) {
        ClassSession a = existing.getClassSession();
        ClassSession b = candidate.getClassSession();

        if (!a.getDay().equals(b.getDay())) return false;

        // Lecture exemption: if both are lectures and overlap is allowed, skip
        if (isLectureExempt(existing, candidate, timetable)) return false;

        boolean sameCampus = a.getLocation().isSameCampusAs(b.getLocation());

        if (sameCampus) {
            // Same campus: only a real time overlap (≥ 1 min) is a hard violation
            return overlapMinutes(a, b) >= 1;
        } else {
            // Different campus: gap < 30 min is a hard violation
            return gapMinutes(a, b) < 30;
        }
    }

    // ── Internal time arithmetic ──────────────────────────────────────────────

    /**
     * Overlap in minutes between two sessions (0 if they merely touch or gap).
     * Negative values are clamped to 0.
     */
    static long overlapMinutes(ClassSession a, ClassSession b) {
        LocalTime latestStart  = a.getStartTime().isAfter(b.getStartTime())
                                 ? a.getStartTime() : b.getStartTime();
        LocalTime earliestEnd  = a.getEndTime().isBefore(b.getEndTime())
                                 ? a.getEndTime() : b.getEndTime();
        long minutes = Duration.between(latestStart, earliestEnd).toMinutes();
        return Math.max(0, minutes);
    }

    /**
     * Gap in minutes from the end of the earlier session to the start of the later.
     * Returns 0 when sessions overlap (i.e. no gap).
     */
    static long gapMinutes(ClassSession a, ClassSession b) {
        ClassSession earlier, later;
        if (!a.getEndTime().isAfter(b.getStartTime())) {
            earlier = a; later = b;
        } else if (!b.getEndTime().isAfter(a.getStartTime())) {
            earlier = b; later = a;
        } else {
            return 0L;  // overlapping — gap is zero
        }
        long gap = Duration.between(earlier.getEndTime(), later.getStartTime()).toMinutes();
        return Math.max(0, gap);
    }
}

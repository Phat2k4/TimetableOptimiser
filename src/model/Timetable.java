package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A named, versioned schedule belonging to a {@link Student}.
 *
 * Naming rules (per spec):
 *  - Names are trimmed and compared case-insensitively.
 *  - Auto-generated names follow the pattern "Timetable_N" (N = 1, 2, …).
 *
 * Constraint flags:
 *  - {@code allowLectureOverlap}: when true, lecture sessions may overlap each
 *    other (a hard-constraint relaxation the student explicitly opts into).
 *
 * Preferences are soft constraints evaluated by the optimiser.
 * "Evenly Spread" and "Compact" are mutually exclusive — the higher-ranked
 * (lower priorityOrder) one wins.
 */
public final class Timetable {

    private String timetableName;       // mutable — user can rename
    private final String timetableId;
    private final String semesterN;
    private boolean allowLectureOverlap;

    private final List<TimetableEntry> entries;
    private final List<Preference>     preferences;

    public Timetable(String timetableName,
                     String timetableId,
                     String semesterN,
                     boolean allowLectureOverlap) {
        this.timetableName      = Objects.requireNonNull(timetableName, "timetableName must not be null").trim();
        this.timetableId        = Objects.requireNonNull(timetableId,   "timetableId must not be null").trim();
        this.semesterN          = Objects.requireNonNull(semesterN,     "semesterN must not be null").trim();
        this.allowLectureOverlap = allowLectureOverlap;
        this.entries            = new ArrayList<>();
        this.preferences        = new ArrayList<>();
    }

    // ── Auto-name factory ─────────────────────────────────────────────────────

    /**
     * Generates an auto-name "Timetable_N" for display.
     * The caller supplies the next available integer.
     */
    public static String generateAutoName(int n) {
        return "Timetable_" + n;
    }

    // ── Entry management ──────────────────────────────────────────────────────

    public void addEntry(TimetableEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        entries.add(entry);
    }

    public boolean removeEntry(TimetableEntry entry) {
        return entries.remove(entry);
    }

    /**
     * Finds an existing entry for the given topic, or returns null.
     */
    public TimetableEntry findEntryForTopic(Topic topic) {
        for (TimetableEntry e : entries) {
            if (e.getTopic().equals(topic)) return e;
        }
        return null;
    }

    // ── Preference management ─────────────────────────────────────────────────

    public void addPreference(Preference preference) {
        Objects.requireNonNull(preference, "preference must not be null");
        preferences.add(preference);
    }

    public boolean removePreference(Preference preference) {
        return preferences.remove(preference);
    }

    /**
     * Resolves the mutually-exclusive "Evenly Spread" vs "Compact" conflict.
     * Returns the winning preference, or null if neither is present.
     */
    public Preference resolveSpreadVsCompact() {
        Preference spread  = null;
        Preference compact = null;
        for (Preference p : preferences) {
            if (p.isEvenlySpread() && (spread  == null || p.hasHigherPriorityThan(spread)))  spread  = p;
            if (p.isCompact()      && (compact == null || p.hasHigherPriorityThan(compact))) compact = p;
        }
        if (spread == null)  return compact;
        if (compact == null) return spread;
        return spread.hasHigherPriorityThan(compact) ? spread : compact;
    }

    /**
     * Returns preferences sorted by priorityOrder (ascending = highest priority first),
     * excluding the lower-ranked of Evenly Spread / Compact.
     */
    public List<Preference> getEffectivePreferences() {
        Preference winner = resolveSpreadVsCompact();
        List<Preference> effective = new ArrayList<>();
        for (Preference p : preferences) {
            if (p.isEvenlySpread() || p.isCompact()) {
                if (p.equals(winner)) effective.add(p);
            } else {
                effective.add(p);
            }
        }
        effective.sort((a, b) -> Integer.compare(a.getPriorityOrder(), b.getPriorityOrder()));
        return Collections.unmodifiableList(effective);
    }

    // ── Clash-checking ────────────────────────────────────────────────────────

    /**
     * Runs clash and commute detection across all entries, updating each
     * entry's warning flags.  Call this after any change to chosen options.
     */
    public void recomputeWarnings() {
        // Flatten all (session, entryIndex, isLecture) tuples
        record SessionRecord(ClassSession session, TimetableEntry entry, boolean isLecture) {}
        List<SessionRecord> records = new ArrayList<>();
        for (TimetableEntry entry : entries) {
            for (ClassOption opt : entry.getChosenOptions()) {
                records.add(new SessionRecord(opt.getClassSession(), entry, opt.isLecture()));
            }
        }

        // Reset all warnings
        for (TimetableEntry entry : entries) {
            entry.setClashWarning(false);
            entry.setCommuteWarning(false);
        }

        // Pairwise comparison
        int n = records.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                SessionRecord a = records.get(i);
                SessionRecord b = records.get(j);

                boolean clashes = a.session().clashesWith(
                        b.session(),
                        allowLectureOverlap,
                        a.isLecture(),
                        b.isLecture());

                if (clashes) {
                    boolean sameCampus = a.session().getLocation()
                                          .isSameCampusAs(b.session().getLocation());
                    if (sameCampus) {
                        // Genuine time overlap
                        a.entry().setClashWarning(true);
                        b.entry().setClashWarning(true);
                    } else {
                        // Different-campus gap < 30 min → commute warning
                        a.entry().setCommuteWarning(true);
                        b.entry().setCommuteWarning(true);
                    }
                }
            }
        }
    }

    // ── Mutators ──────────────────────────────────────────────────────────────

    public void setTimetableName(String timetableName) {
        Objects.requireNonNull(timetableName);
        this.timetableName = timetableName.trim();
    }

    public void setAllowLectureOverlap(boolean allowLectureOverlap) {
        this.allowLectureOverlap = allowLectureOverlap;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String               getTimetableName()      { return timetableName; }
    public String               getTimetableId()        { return timetableId; }
    public String               getSemesterN()          { return semesterN; }
    public boolean              isAllowLectureOverlap() { return allowLectureOverlap; }
    public List<TimetableEntry> getEntries()            { return Collections.unmodifiableList(entries); }
    public List<Preference>     getPreferences()        { return Collections.unmodifiableList(preferences); }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Timetable t)) return false;
        return timetableId.equals(t.timetableId);
    }

    @Override
    public int hashCode() {
        return timetableId.hashCode();
    }

    @Override
    public String toString() {
        return timetableName + " [" + timetableId + "] – " + semesterN +
               " (" + entries.size() + " entries)";
    }
}

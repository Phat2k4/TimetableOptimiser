//TimetableGenerator.java

import model.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a {@link Timetable} from a set of {@link GenerationSettings}.
 *
 * <h3>Hard constraints (never violated):</h3>
 * <ol>
 *   <li>No time clash — sessions on the same day may not overlap by ≥ 1 minute
 *       (unless both are lectures and {@code allowLectureOverlap} is set).</li>
 *   <li>Inter-campus gap — sessions at different campuses on the same day must
 *       have ≥ 30 minutes between the end of the earlier and start of the later.</li>
 *   <li>Exactly one instance of each required class type per topic.</li>
 *   <li>Campus mixing — City campus classes may not be mixed with
 *       Bedford Park / Tonsley classes for the <em>same</em> topic (cross-topic
 *       mixing is allowed).</li>
 * </ol>
 *
 * <h3>Soft constraints (preferences):</h3>
 * Applied in the user's priority order.  A valid timetable is always returned
 * even if some preferences cannot be met; preferences only score candidates,
 * they never filter them out.
 *
 * <h3>Last-used settings:</h3>
 * The most recently used {@link GenerationSettings} are stored in a static
 * field and re-surfaced when the user opens this screen again.
 */
public final class TimetableGenerator {

    // ── ANSI shortcuts ────────────────────────────────────────────────────────
    private static final String R  = AppState.ANSI_RESET;
    private static final String G  = AppState.ANSI_GREEN;
    private static final String RE = AppState.ANSI_RED;
    private static final String Y  = AppState.ANSI_YELLOW;
    private static final String C  = AppState.ANSI_CYAN;

    // ── Campus grouping ───────────────────────────────────────────────────────
    /** Campus names treated as "City" group — cannot mix with BP/Tonsley for one topic. */
    private static final Set<String> CITY_CAMPUSES = Set.of(
            "city", "flinders city campus", "flinders city", "city campus");
    /** Campus names treated as "Non-city" group. */
    private static final Set<String> NON_CITY_CAMPUSES = Set.of(
            "bedford park", "tonsley");

    // ── Last-used settings (persisted within the JVM session) ─────────────────
    private static GenerationSettings lastUsedSettings = null;

    /** Returns the settings used in the most recent generation, or null if none. */
    public static GenerationSettings getLastUsedSettings() {
        return lastUsedSettings;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  GenerationSettings — value object carrying all user inputs
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * All user-configurable inputs for timetable generation.
     * Immutable after construction.
     */
    public static final class GenerationSettings {

        /** Display name.  Blank means auto-generate "Timetable_N". */
        public final String name;

        /** Semester filter: "1", "2", or "both". */
        public final String semester;

        /** Topic codes the student wants to include (at least one required). */
        public final List<String> topicCodes;

        /**
         * Preferred campus.  One of "Bedford Park", "Tonsley",
         * "Flinders City Campus", or blank for no preference.
         */
        public final String preferredCampus;

        /** If true, lecture sessions may overlap each other. */
        public final boolean allowLectureOverlap;

        /**
         * User's ranked preferences, in priority order (index 0 = highest priority).
         * Each entry is one of the {@code Preference.TYPE_*} constants or a
         * campus/day name string.
         */
        public final List<String> preferenceOrder;

        public GenerationSettings(String name,
                                  String semester,
                                  List<String> topicCodes,
                                  String preferredCampus,
                                  boolean allowLectureOverlap,
                                  List<String> preferenceOrder) {
            this.name                = (name == null ? "" : name.trim());
            this.semester            = (semester == null ? "both" : semester.trim());
            this.topicCodes          = List.copyOf(topicCodes == null ? List.of() : topicCodes);
            this.preferredCampus     = (preferredCampus == null ? "" : preferredCampus.trim());
            this.allowLectureOverlap = allowLectureOverlap;
            this.preferenceOrder     = List.copyOf(preferenceOrder == null ? List.of() : preferenceOrder);
        }

        /** Returns a mutable builder pre-populated with this settings object. */
        public Builder toBuilder() {
            return new Builder()
                    .name(name)
                    .semester(semester)
                    .topicCodes(new ArrayList<>(topicCodes))
                    .preferredCampus(preferredCampus)
                    .allowLectureOverlap(allowLectureOverlap)
                    .preferenceOrder(new ArrayList<>(preferenceOrder));
        }

        // ── Builder ───────────────────────────────────────────────────────────
        public static final class Builder {
            private String       name              = "";
            private String       semester          = "both";
            private List<String> topicCodes        = new ArrayList<>();
            private String       preferredCampus   = "";
            private boolean      allowLectureOverlap = false;
            private List<String> preferenceOrder   = new ArrayList<>();

            public Builder name(String v)               { this.name = v; return this; }
            public Builder semester(String v)            { this.semester = v; return this; }
            public Builder topicCodes(List<String> v)    { this.topicCodes = v; return this; }
            public Builder preferredCampus(String v)     { this.preferredCampus = v; return this; }
            public Builder allowLectureOverlap(boolean v){ this.allowLectureOverlap = v; return this; }
            public Builder preferenceOrder(List<String> v){ this.preferenceOrder = v; return this; }

            public GenerationSettings build() {
                return new GenerationSettings(name, semester, topicCodes,
                        preferredCampus, allowLectureOverlap, preferenceOrder);
            }
        }

        @Override
        public String toString() {
            return String.format("GenerationSettings{name='%s', semester='%s', topics=%s, " +
                                 "campus='%s', lectureOverlap=%b, prefs=%s}",
                    name, semester, topicCodes, preferredCampus, allowLectureOverlap, preferenceOrder);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Generation result
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * The outcome of a generation attempt.
     */
    public static final class GenerationResult {
        public final boolean    success;
        public final Timetable  timetable;
        public final String     errorMessage;
        /** Preferences that were requested but could not be satisfied. */
        public final List<String> unmetPreferences;

        private GenerationResult(boolean success, Timetable tt,
                                  String err, List<String> unmet) {
            this.success          = success;
            this.timetable        = tt;
            this.errorMessage     = err;
            this.unmetPreferences = List.copyOf(unmet == null ? List.of() : unmet);
        }

        static GenerationResult ok(Timetable tt, List<String> unmet) {
            return new GenerationResult(true, tt, null, unmet);
        }
        static GenerationResult fail(String msg) {
            return new GenerationResult(false, null, msg, List.of());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TopicCandidates — inner record used during backtracking search
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Pairs a topic with all its valid class-combination options so the
     * backtracking search can reference it across method boundaries.
     */
    private record TopicCandidates(Topic topic, List<List<ClassOption>> combinations) {}

    // ═════════════════════════════════════════════════════════════════════════
    //  Main generate method
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Generates a timetable from the given settings and saves it to
     * {@link AppState} on success.
     *
     * @param settings user's customisation choices
     * @return a {@link GenerationResult} — always non-null
     */
    public GenerationResult generate(GenerationSettings settings) {
        Objects.requireNonNull(settings, "settings must not be null");
        lastUsedSettings = settings;   // remember for next visit

        // ── Validate inputs ───────────────────────────────────────────────────
        if (settings.topicCodes.isEmpty()) {
            return GenerationResult.fail("At least one topic must be selected.");
        }

        AppState state = AppState.getInstance();

        // ── Resolve timetable name ────────────────────────────────────────────
        String ttName = resolveName(settings.name, state);
        if (ttName == null) {
            return GenerationResult.fail("A timetable with that name already exists " +
                    "(names are case-insensitive). Choose a different name.");
        }

        // ── Build Timetable shell ─────────────────────────────────────────────
        String semLabel = settings.semester.equalsIgnoreCase("both")
                          ? "S1+S2" : "S" + settings.semester;
        String ttId = state.nextTimetableId();
        if (ttName.isBlank()) ttName = state.nextAutoName();

        Timetable timetable = new Timetable(ttName, ttId, semLabel,
                                             settings.allowLectureOverlap);

        // Attach user preferences to timetable model (for display / persistence)
        for (int i = 0; i < settings.preferenceOrder.size(); i++) {
            timetable.addPreference(new Preference(settings.preferenceOrder.get(i), i + 1));
        }

        // ── Collect candidates per topic ──────────────────────────────────────
        // For each requested topic, gather all ClassOptions that pass the
        // semester + campus hard-constraint filters.
        List<String> unmetPrefs = new ArrayList<>();

        // We'll do a backtracking combination search: for each topic, we have a
        // list of "candidate selections" (one selection = one ClassOption per
        // required class-type within that topic's availability).
        // We pick the combination with the best preference score.

        // Step 1 — per-topic: enumerate all valid availability × option-combination tuples
        List<TopicCandidates> allTopicCandidates = new ArrayList<>();

        for (String code : settings.topicCodes) {
            Optional<Topic> topicOpt = state.findTopic(code.trim().toUpperCase());
            if (topicOpt.isEmpty()) {
                return GenerationResult.fail("Topic not found: " + code +
                        ". Import its CSV data first.");
            }
            Topic topic = topicOpt.get();

            // Filter availabilities by semester
            List<Availability> semesterMatched = filterBySemester(
                    topic.getAvailabilities(), settings.semester);
            if (semesterMatched.isEmpty()) {
                return GenerationResult.fail("No classes available for topic " +
                        topic.getFullName() + " in semester " + settings.semester + ".");
            }

            // Within each availability, produce combinations: exactly one ClassOption
            // per distinct className (class type).  Enforce campus hard constraints.
            List<List<ClassOption>> topicCombos = new ArrayList<>();
            for (Availability av : semesterMatched) {
                if (!campusAllowed(av, settings.preferredCampus)) continue;
                List<List<ClassOption>> availCombos = buildClassTypeCombinations(av, topic);
                topicCombos.addAll(availCombos);
            }

            if (topicCombos.isEmpty()) {
                return GenerationResult.fail("No valid class combinations found for topic " +
                        topic.getFullName() + " matching campus/semester constraints.");
            }
            allTopicCandidates.add(new TopicCandidates(topic, topicCombos));
        }

        // Step 2 — find the best clash-free cross-topic combination via greedy
        // best-first search (branch and bound would be ideal but dataset sizes
        // are small; greedy is correct for small N topics).
        List<ClassOption> chosen = new ArrayList<>();
        List<TopicCandidates> remaining = new ArrayList<>(allTopicCandidates);
        List<Topic>           chosenTopics = new ArrayList<>();

        boolean ok = backtrack(chosen, chosenTopics, remaining, timetable, settings);

        if (!ok) {
            return GenerationResult.fail(
                    "No clash-free timetable could be generated with the selected " +
                    "topics and constraints. Try relaxing preferences or adjusting " +
                    "campus settings.");
        }

        // Step 3 — attach chosen options to timetable entries
        // Re-associate each ClassOption with its topic via the parallel chosenTopics list
        Map<String, List<ClassOption>> optsByTopic = new LinkedHashMap<>();
        for (int i = 0; i < chosen.size(); i++) {
            String code = chosenTopics.get(i).getTopicCode();
            optsByTopic.computeIfAbsent(code, k -> new ArrayList<>()).add(chosen.get(i));
        }
        for (TopicCandidates tc : allTopicCandidates) {
            String code = tc.topic().getTopicCode();
            TimetableEntry entry = new TimetableEntry(tc.topic());
            List<ClassOption> opts = optsByTopic.getOrDefault(code, List.of());
            for (ClassOption opt : opts) entry.addChosenOption(opt);
            timetable.addEntry(entry);
        }

        // Step 4 — compute warnings and identify unmet prefs
        timetable.recomputeWarnings();
        unmetPrefs.addAll(findUnmetPreferences(chosen, settings));

        // Step 5 — save to AppState
        state.addTimetable(timetable);
        state.save();

        return GenerationResult.ok(timetable, unmetPrefs);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Backtracking search — picks the best valid cross-topic combination
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Recursively assigns one combination per topic.
     * Tries each combination in preference-score order (best first).
     * Prunes branches immediately when a hard constraint is violated.
     *
     * @param chosen      accumulates the chosen ClassOptions (flat list)
     * @param chosenTopics parallel list — topic owner of each chosen option
     * @param remaining   topics yet to assign
     * @param timetable   shell timetable (for lecture-overlap flag)
     * @param settings    user settings (for scoring)
     * @return true if a valid complete assignment was found
     */
    private boolean backtrack(List<ClassOption>    chosen,
                               List<Topic>          chosenTopics,
                               List<TopicCandidates> remaining,
                               Timetable            timetable,
                               GenerationSettings   settings) {
        if (remaining.isEmpty()) return true;

        TopicCandidates current = remaining.get(0);
        List<TopicCandidates> rest = remaining.subList(1, remaining.size());

        // Sort this topic's combinations by preference score (descending = best first)
        List<List<ClassOption>> sorted = current.combinations().stream()
                .sorted(Comparator.comparingInt(
                        (List<ClassOption> combo) -> scoreCombo(combo, settings)).reversed())
                .collect(Collectors.toList());

        for (List<ClassOption> combo : sorted) {
            // Hard-constraint check: does adding this combo violate anything already chosen?
            if (hasHardConflict(chosen, combo, timetable)) continue;

            // Tentatively add
            int addedCount = combo.size();
            chosen.addAll(combo);
            for (int i = 0; i < addedCount; i++) chosenTopics.add(current.topic());

            if (backtrack(chosen, chosenTopics, rest, timetable, settings)) return true;

            // Undo
            for (int i = 0; i < addedCount; i++) {
                chosen.remove(chosen.size() - 1);
                chosenTopics.remove(chosenTopics.size() - 1);
            }
        }
        return false;
    }

    /** True when any option in {@code combo} hard-conflicts with any already-chosen option. */
    private boolean hasHardConflict(List<ClassOption> chosen,
                                     List<ClassOption> combo,
                                     Timetable timetable) {
        for (ClassOption candidate : combo) {
            for (ClassOption existing : chosen) {
                if (ClashDetector.violatesHardConstraint(existing, candidate, timetable)) {
                    return true;
                }
            }
            // Also check within the combo itself
            for (ClassOption other : combo) {
                if (other == candidate) continue;
                if (ClashDetector.violatesHardConstraint(other, candidate, timetable)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Preference scoring
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Scores a combination of ClassOptions against the user's preference list.
     * Higher = better.  Each preference met adds (preferenceCount - rank) points,
     * so higher-priority preferences contribute more when met.
     */
    private int scoreCombo(List<ClassOption> combo, GenerationSettings settings) {
        int score = 0;
        int n = settings.preferenceOrder.size();
        for (int rank = 0; rank < n; rank++) {
            String pref = settings.preferenceOrder.get(rank);
            int weight  = n - rank;   // highest rank = highest weight
            if (prefSatisfied(combo, pref, settings)) {
                score += weight;
            }
        }
        return score;
    }

    /**
     * Returns true if the given preference is satisfied by all sessions in the combo.
     */
    private boolean prefSatisfied(List<ClassOption> combo,
                                   String pref,
                                   GenerationSettings settings) {
        List<ClassSession> sessions = combo.stream()
                .map(ClassOption::getClassSession).collect(Collectors.toList());

        return switch (pref.toLowerCase(Locale.ENGLISH)) {
            case "bedford park"           -> sessions.stream().allMatch(
                    s -> s.getLocation().getBuilding().toLowerCase().contains("bedford"));
            case "tonsley"                -> sessions.stream().allMatch(
                    s -> s.getLocation().getBuilding().toLowerCase().contains("tonsley"));
            case "flinders city campus",
                 "city"                  -> sessions.stream().allMatch(
                    s -> isCityCampus(s.getLocation().getBuilding()));
            case "all at same campus"    -> allSameCampus(sessions);
            case "mornings"              -> sessions.stream().allMatch(ClassSession::isMorning);
            case "afternoons"            -> sessions.stream().allMatch(ClassSession::isAfternoon);
            case "monday"                -> sessions.stream().allMatch(
                    s -> s.getDay() == DayOfWeek.MONDAY);
            case "tuesday"               -> sessions.stream().allMatch(
                    s -> s.getDay() == DayOfWeek.TUESDAY);
            case "wednesday"             -> sessions.stream().allMatch(
                    s -> s.getDay() == DayOfWeek.WEDNESDAY);
            case "thursday"              -> sessions.stream().allMatch(
                    s -> s.getDay() == DayOfWeek.THURSDAY);
            case "friday"                -> sessions.stream().allMatch(
                    s -> s.getDay() == DayOfWeek.FRIDAY);
            case "evenly spread"         -> true;   // evaluated globally; per-combo always satisfied
            case "compact"               -> true;   // evaluated globally; per-combo always satisfied
            default                      -> false;
        };
    }

    /** True when all sessions share the same building. */
    private boolean allSameCampus(List<ClassSession> sessions) {
        if (sessions.isEmpty()) return true;
        String first = sessions.get(0).getLocation().getBuilding().toLowerCase();
        return sessions.stream().allMatch(
                s -> s.getLocation().getBuilding().toLowerCase().equals(first));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Unmet preferences
    // ═════════════════════════════════════════════════════════════════════════

    private List<String> findUnmetPreferences(List<ClassOption> chosen,
                                               GenerationSettings settings) {
        List<String> unmet = new ArrayList<>();
        for (String pref : settings.preferenceOrder) {
            if (!prefSatisfied(chosen, pref, settings)) {
                unmet.add(pref);
            }
        }
        return unmet;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Class-type combination builder
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * For one Availability, produces all valid combinations of ClassOptions
     * where exactly one ClassOption is chosen per distinct class name (type).
     *
     * <p>E.g. if an availability has Lecture(×2 instances) and Tutorial(×3),
     * this produces 2×3 = 6 combinations.
     */
    private List<List<ClassOption>> buildClassTypeCombinations(Availability av, Topic topic) {
        // Group options by className (class type)
        Map<String, List<ClassOption>> byType = new LinkedHashMap<>();
        for (ClassOption opt : av.getClassOptions()) {
            byType.computeIfAbsent(opt.getClassName(), k -> new ArrayList<>()).add(opt);
        }

        if (byType.isEmpty()) return List.of();

        // Cartesian product of all class types
        List<List<ClassOption>> result = new ArrayList<>();
        result.add(new ArrayList<>());   // seed

        for (List<ClassOption> typeOptions : byType.values()) {
            List<List<ClassOption>> expanded = new ArrayList<>();
            for (List<ClassOption> partial : result) {
                for (ClassOption opt : typeOptions) {
                    List<ClassOption> newCombo = new ArrayList<>(partial);
                    newCombo.add(opt);
                    expanded.add(newCombo);
                }
            }
            result = expanded;
        }
        return result;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Filtering helpers
    // ═════════════════════════════════════════════════════════════════════════

    /** Filters availabilities by the requested semester setting. */
    private List<Availability> filterBySemester(List<Availability> avails, String semester) {
        if (semester == null || semester.equalsIgnoreCase("both")) return avails;
        String target = "s" + semester.trim();
        List<Availability> matched = new ArrayList<>();
        for (Availability av : avails) {
            if (av.getSemesterN().equalsIgnoreCase(target)) matched.add(av);
        }
        return matched;
    }

    /**
     * Returns true when the availability's campus is compatible with the
     * preferred campus setting.
     *
     * <p>Campus mixing rule: City campus cannot be mixed with Bedford Park /
     * Tonsley classes <em>for the same topic</em>.  Since this method is called
     * per-availability, we enforce: if the user specified a preferred campus,
     * only accept availabilities from that campus group.
     *
     * <p>When no preferred campus is set, all campuses are accepted.
     */
    private boolean campusAllowed(Availability av, String preferredCampus) {
        if (preferredCampus == null || preferredCampus.isBlank()) return true;
        String campus = av.getCampusLocation().toLowerCase(Locale.ENGLISH);
        String pref   = preferredCampus.toLowerCase(Locale.ENGLISH);

        boolean prefIsCity = CITY_CAMPUSES.contains(pref);

        if (prefIsCity) {
            return isCityCampus(av.getCampusLocation());
        } else {
            // Preferred non-city: accept if campus contains the preferred name
            return campus.contains(pref) || (!isCityCampus(av.getCampusLocation()) &&
                   pref.isBlank());
        }
    }

    private boolean isCityCampus(String campusName) {
        String lower = campusName.toLowerCase(Locale.ENGLISH);
        return CITY_CAMPUSES.stream().anyMatch(lower::contains);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Name resolution
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Resolves the timetable name.
     * Returns the resolved name, or null if the name is already taken.
     * An empty/blank name triggers auto-generation.
     */
    private String resolveName(String requestedName, AppState state) {
        if (requestedName == null || requestedName.isBlank()) {
            // Auto-generate — pre-increment to get the right counter value
            // (nextTimetableId will also increment; we sync by calling it here
            //  conceptually; actual call happens after this method returns)
            return "";   // signal "auto" — caller will invoke state.nextAutoName()
        }
        String trimmed = requestedName.trim();
        // Check uniqueness (case-insensitive)
        for (Timetable tt : state.allTimetables()) {
            if (tt.getTimetableName().equalsIgnoreCase(trimmed)) return null;
        }
        return trimmed;
    }
}

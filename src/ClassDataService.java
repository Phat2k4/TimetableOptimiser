import model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;

/**
 * Service layer for all class-data operations.
 *
 * <p>Delegates CSV import to {@link CsvImporter}.
 * All other methods operate directly on the in-memory {@link AppState}.
 *
 * <h3>ANSI colours:</h3>
 * Green = success, Red = error, Yellow = warning, Cyan = prompt/info.
 */
public final class ClassDataService {

    // ── ANSI shortcuts ────────────────────────────────────────────────────────
    private static final String R  = AppState.ANSI_RESET;
    private static final String G  = AppState.ANSI_GREEN;
    private static final String RE = AppState.ANSI_RED;
    private static final String Y  = AppState.ANSI_YELLOW;
    private static final String C  = AppState.ANSI_CYAN;

    // ── Formatters ────────────────────────────────────────────────────────────
    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    // ── Scanner shared within session ─────────────────────────────────────────
    private final Scanner scanner;

    public ClassDataService(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner must not be null");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  1. importFromCsv
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Delegates to {@link CsvImporter} to parse and load a CSV file.
     *
     * @param path file-system path to the CSV
     * @return true if the file was processed without a fatal error
     */
    public boolean importFromCsv(String path) {
        System.out.println(C + "↳ Importing from: " + path + R);
        CsvImporter importer = new CsvImporter();
        return importer.importFile(path);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. browseAll
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Displays one summary row per group keyed by:
     * topicCode + topicName + attendanceMode + campus + semester +
     * availabilityNumber + className + classInstance
     *
     * <p>Multiple sessions in the same group are shown with their time/location
     * on separate sub-lines.
     */
    public void browseAll() {
        List<FlatRecord> all = flatRecords();
        if (all.isEmpty()) {
            System.out.println(Y + "No class data found. Import a CSV first." + R);
            return;
        }

        // Group by the 8-part key
        Map<String, List<FlatRecord>> grouped = new LinkedHashMap<>();
        for (FlatRecord r : all) {
            grouped.computeIfAbsent(r.groupKey(), k -> new ArrayList<>()).add(r);
        }

        System.out.println(C + "\n══ All Classes ══" + R);
        printSeparator();

        int rowNum = 1;
        for (Map.Entry<String, List<FlatRecord>> entry : grouped.entrySet()) {
            List<FlatRecord> group = entry.getValue();
            FlatRecord first = group.get(0);

            System.out.printf(C + "[%3d] " + R + G + "%-12s" + R + " %-40s%n",
                    rowNum++,
                    first.topicCode,
                    first.topicName);

            System.out.printf("       Mode: %-16s  Campus: %-20s  Sem: %s  Avail#: %s%n",
                    first.attendanceMode,
                    first.campusLocation,
                    first.semesterN,
                    first.availabilityNumber);

            System.out.printf("       Class: %-14s  Instance: %d%n",
                    first.className, first.classInstance);

            // Sessions within the group
            for (FlatRecord r : group) {
                System.out.printf("         %s  %s–%s  %s  %s, %s%n",
                        r.day,
                        r.startTime.format(TIME_FMT),
                        r.endTime.format(TIME_FMT),
                        formatDateRange(r.firstDate, r.lastDate),
                        r.building, r.room);
            }
            printSeparator();
        }
        System.out.printf(C + "Total groups: %d%n" + R, grouped.size());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. viewTopicCode
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Shows full detail for every session under the given topic code.
     *
     * @param code topic code (case-insensitive)
     */
    public void viewTopicCode(String code) {
        Optional<Topic> opt = AppState.getInstance().findTopic(code.trim());
        if (opt.isEmpty()) {
            System.out.println(RE + "✘ Topic not found: " + code + R);
            return;
        }
        Topic topic = opt.get();

        System.out.println(C + "\n══ Topic: " + topic.getFullName() + " ══" + R);
        printSeparator();

        for (Availability av : topic.getAvailabilities()) {
            System.out.println(C + "  Availability: " + av + R);
            if (av.getClassOptions().isEmpty()) {
                System.out.println(Y + "    (no class sessions)" + R);
                continue;
            }
            // Table header
            System.out.printf("  %-16s %4s  %-10s  %-11s  %-11s  %-9s  %-8s  %s%n",
                    "Class (Instance)", "Day",
                    "First Date", "Last Date",
                    "Start", "End",
                    "Building", "Room");
            System.out.println("  " + "-".repeat(105));

            for (ClassOption co : av.getClassOptions()) {
                ClassOffering of = co.getClassOffering();
                ClassSession  cs = co.getClassSession();
                Location      lo = cs.getLocation();
                System.out.printf("  %-14s(%2d)  %-10s  %-11s  %-11s  %-9s  %-8s  %s%n",
                        of.getClassName(),
                        of.getClassInstance(),
                        cs.getDay().toString().substring(0, 3),
                        cs.getDateOfFirstClass().format(DATE_FMT),
                        cs.getDateOfLastClass().format(DATE_FMT),
                        cs.getStartTime().format(TIME_FMT),
                        cs.getEndTime().format(TIME_FMT),
                        lo.getBuilding(),
                        lo.getRoom());
            }
            printSeparator();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. search
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Searches across all flat records.
     *
     * <p>Every non-blank criterion in {@code criteria} must match (AND logic).
     * Blank criteria are ignored (treated as "any").
     * Matching is case-insensitive substring for strings; exact for dates/times.
     *
     * @param criteria map of field name → query string (see constants below)
     */
    public void search(Map<String, String> criteria) {
        List<FlatRecord> all = flatRecords();
        if (all.isEmpty()) {
            System.out.println(Y + "No class data found." + R);
            return;
        }

        List<Predicate<FlatRecord>> predicates = buildPredicates(criteria);
        List<FlatRecord> results = new ArrayList<>();
        for (FlatRecord r : all) {
            if (predicates.stream().allMatch(p -> p.test(r))) results.add(r);
        }

        if (results.isEmpty()) {
            System.out.println(Y + "No results matched your search criteria." + R);
            return;
        }

        System.out.printf(C + "\n══ Search Results (%d found) ══%n" + R, results.size());
        printSeparator();
        printFlatTable(results);
    }

    // ── Search field name constants ───────────────────────────────────────────
    public static final String S_TOPIC_CODE     = "topicCode";
    public static final String S_TOPIC_NAME     = "topicName";
    public static final String S_ATTEND_MODE    = "attendanceMode";
    public static final String S_CAMPUS         = "campus";
    public static final String S_SEMESTER       = "semester";
    public static final String S_AVAIL_NUMBER   = "availabilityNumber";
    public static final String S_CLASS_NAME     = "className";
    public static final String S_CLASS_INSTANCE = "classInstance";
    public static final String S_FIRST_DATE     = "firstDate";
    public static final String S_LAST_DATE      = "lastDate";
    public static final String S_DAY            = "day";
    public static final String S_START_TIME     = "startTime";
    public static final String S_END_TIME       = "endTime";
    public static final String S_BUILDING       = "building";
    public static final String S_ROOM           = "room";

    private List<Predicate<FlatRecord>> buildPredicates(Map<String, String> criteria) {
        List<Predicate<FlatRecord>> list = new ArrayList<>();
        for (Map.Entry<String, String> e : criteria.entrySet()) {
            String key = e.getKey();
            String val = e.getValue() == null ? "" : e.getValue().trim();
            if (val.isBlank()) continue;
            switch (key) {
                case S_TOPIC_CODE     -> list.add(r -> ci(r.topicCode,        val));
                case S_TOPIC_NAME     -> list.add(r -> ci(r.topicName,        val));
                case S_ATTEND_MODE    -> list.add(r -> ci(r.attendanceMode,   val));
                case S_CAMPUS         -> list.add(r -> ci(r.campusLocation,   val));
                case S_SEMESTER       -> list.add(r -> ci(r.semesterN,        val));
                case S_AVAIL_NUMBER   -> list.add(r -> ci(r.availabilityNumber, val));
                case S_CLASS_NAME     -> list.add(r -> ci(r.className,        val));
                case S_CLASS_INSTANCE -> list.add(r -> String.valueOf(r.classInstance).contains(val));
                case S_FIRST_DATE     -> list.add(r -> r.firstDate.toString().contains(val)
                                                    || r.firstDate.format(DATE_FMT).toLowerCase()
                                                                  .contains(val.toLowerCase()));
                case S_LAST_DATE      -> list.add(r -> r.lastDate.toString().contains(val)
                                                    || r.lastDate.format(DATE_FMT).toLowerCase()
                                                                 .contains(val.toLowerCase()));
                case S_DAY            -> list.add(r -> ci(r.day.name(),        val));
                case S_START_TIME     -> list.add(r -> r.startTime.format(TIME_FMT).contains(val));
                case S_END_TIME       -> list.add(r -> r.endTime.format(TIME_FMT).contains(val));
                case S_BUILDING       -> list.add(r -> ci(r.building,         val));
                case S_ROOM           -> list.add(r -> ci(r.room,             val));
                default -> System.out.println(Y + "⚠ Unknown search field ignored: " + key + R);
            }
        }
        return list;
    }

    /** Case-insensitive substring match. */
    private static boolean ci(String field, String query) {
        return field.toLowerCase().contains(query.toLowerCase());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. edit
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Interactively edits any field of a {@link ClassOption} identified by
     * the given {@link FlatRecord} reference.
     *
     * <p>Prompts the user field-by-field (blank = keep current), then asks
     * for confirmation before saving.
     *
     * @param record the flat record locating the option to edit
     */
    public void edit(FlatRecord record) {
        ClassOption found = locateOption(record);
        if (found == null) {
            System.out.println(RE + "✘ Could not locate the record in current data." + R);
            return;
        }

        Availability avail = record.availability;
        ClassSession cs    = found.getClassSession();
        ClassOffering co   = found.getClassOffering();

        System.out.println(C + "\n── Edit Record ──" + R);
        System.out.println("  Current: " + found);
        System.out.println(C + "  (Press Enter to keep current value)" + R);

        // ── Editable fields ───────────────────────────────────────────────────
        String  newClassName = promptOrKeep("Class name",          co.getClassName());
        int     newInstance  = promptIntOrKeep("Class instance",   co.getClassInstance());
        LocalDate newFirst   = promptDateOrKeep("First date (D MMM, e.g. 3 Mar)", cs.getDateOfFirstClass());
        LocalDate newLast    = promptDateOrKeep("Last date  (D MMM, e.g. 31 Oct)", cs.getDateOfLastClass());
        DayOfWeek newDay     = promptDayOrKeep("Day (e.g. MONDAY)", cs.getDay());
        LocalTime newStart   = promptTimeOrKeep("Start time (HH:MM)", cs.getStartTime());
        LocalTime newEnd     = promptTimeOrKeep("End time   (HH:MM)", cs.getEndTime());
        String  newBuilding  = promptOrKeep("Building",             cs.getLocation().getBuilding());
        String  newRoom      = promptOrKeep("Room",                 cs.getLocation().getRoom());

        // ── Preview + confirm ─────────────────────────────────────────────────
        ClassOffering newOffering = new ClassOffering(newClassName, newInstance);
        ClassSession  newSession  = new ClassSession(newFirst, newLast, newDay,
                                                     newStart, newEnd,
                                                     new Location(newBuilding, newRoom));
        ClassOption newOpt = new ClassOption(newOffering, newSession);

        System.out.println(C + "\n  New value: " + newOpt + R);
        if (!confirm("Save changes?")) {
            System.out.println(Y + "  Edit cancelled." + R);
            return;
        }

        boolean replaced = avail.replaceClassOption(found, newOpt);
        if (replaced) {
            System.out.println(G + "✔ Record updated successfully." + R);
            AppState.getInstance().save();
        } else {
            System.out.println(RE + "✘ Update failed (record may have changed)." + R);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  6. delete
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Deletes the {@link ClassOption} identified by the given {@link FlatRecord}
     * after user confirmation.
     *
     * @param record the flat record locating the option to delete
     */
    public void delete(FlatRecord record) {
        ClassOption found = locateOption(record);
        if (found == null) {
            System.out.println(RE + "✘ Could not locate the record in current data." + R);
            return;
        }

        System.out.println(C + "\n── Delete Record ──" + R);
        System.out.println("  " + found);
        System.out.printf("  Topic: %s | Avail: %s%n",
                record.topicCode, record.availability);

        if (!confirm("Permanently delete this record?")) {
            System.out.println(Y + "  Deletion cancelled." + R);
            return;
        }

        boolean removed = record.availability.removeClassOption(found);
        if (removed) {
            System.out.println(G + "✔ Record deleted." + R);
            AppState.getInstance().save();
        } else {
            System.out.println(RE + "✘ Deletion failed (record not found in availability)." + R);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FlatRecord — projection used by browse / search / edit / delete
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * A flat view of one {@link ClassOption} plus all its parent context fields.
     * Carries live references to {@link Availability} and {@link ClassOption} so
     * the service can mutate the domain model directly.
     */
    public static final class FlatRecord {

        // Context
        public final String       topicCode;
        public final String       topicName;
        public final Availability availability;
        public final String       attendanceMode;
        public final String       campusLocation;
        public final String       semesterN;
        public final String       availabilityNumber;
        // Offering
        public final String       className;
        public final int          classInstance;
        // Session
        public final LocalDate    firstDate;
        public final LocalDate    lastDate;
        public final DayOfWeek    day;
        public final LocalTime    startTime;
        public final LocalTime    endTime;
        // Location
        public final String       building;
        public final String       room;
        // Live option reference (for mutation)
        public final ClassOption  classOption;

        FlatRecord(Topic topic, Availability av, ClassOption opt) {
            ClassOffering co = opt.getClassOffering();
            ClassSession  cs = opt.getClassSession();
            Location      lo = cs.getLocation();

            this.topicCode          = topic.getTopicCode();
            this.topicName          = topic.getTopicName();
            this.availability       = av;
            this.attendanceMode     = av.getAttendanceMode();
            this.campusLocation     = av.getCampusLocation();
            this.semesterN          = av.getSemesterN();
            this.availabilityNumber = av.getAvailabilityNumber();
            this.className          = co.getClassName();
            this.classInstance      = co.getClassInstance();
            this.firstDate          = cs.getDateOfFirstClass();
            this.lastDate           = cs.getDateOfLastClass();
            this.day                = cs.getDay();
            this.startTime          = cs.getStartTime();
            this.endTime            = cs.getEndTime();
            this.building           = lo.getBuilding();
            this.room               = lo.getRoom();
            this.classOption        = opt;
        }

        /** 8-field group key for browseAll grouping. */
        String groupKey() {
            return topicCode + "|" + topicName + "|" + attendanceMode + "|" +
                   campusLocation + "|" + semesterN + "|" + availabilityNumber + "|" +
                   className + "|" + classInstance;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Internal helpers
    // ══════════════════════════════════════════════════════════════════════════

    /** Builds a flat list of all class options in AppState. */
    private List<FlatRecord> flatRecords() {
        List<FlatRecord> list = new ArrayList<>();
        for (Topic topic : AppState.getInstance().allTopics()) {
            for (Availability av : topic.getAvailabilities()) {
                for (ClassOption opt : av.getClassOptions()) {
                    list.add(new FlatRecord(topic, av, opt));
                }
            }
        }
        return list;
    }

    /** Finds the live {@link ClassOption} for the given flat record reference. */
    private ClassOption locateOption(FlatRecord record) {
        // The FlatRecord already carries a live reference; verify it still exists
        for (ClassOption opt : record.availability.getClassOptions()) {
            if (opt == record.classOption) return opt;   // identity check
        }
        // Fall back to value equality
        for (ClassOption opt : record.availability.getClassOptions()) {
            if (opt.equals(record.classOption)) return opt;
        }
        return null;
    }

    /** Prints a compact table of flat records. */
    private void printFlatTable(List<FlatRecord> records) {
        System.out.printf("  %-12s %-30s %-16s %-12s %-3s %-6s %-6s %s%n",
                "Topic", "Topic Name", "Class(Inst)", "Avail/Sem",
                "Day", "Start", "End", "Location");
        System.out.println("  " + "-".repeat(110));
        for (FlatRecord r : records) {
            System.out.printf("  %-12s %-30s %-12s(%2d) %-12s %-3s %-6s %-6s %s, %s%n",
                    r.topicCode,
                    truncate(r.topicName, 29),
                    truncate(r.className, 12),
                    r.classInstance,
                    r.semesterN + "/" + r.availabilityNumber,
                    r.day.toString().substring(0, 3),
                    r.startTime.format(TIME_FMT),
                    r.endTime.format(TIME_FMT),
                    r.building, r.room);
        }
    }

    // ── Interactive prompts ───────────────────────────────────────────────────

    /** Prompts for a string value; returns current if blank. */
    private String promptOrKeep(String label, String current) {
        System.out.printf(C + "  %s [%s]: " + R, label, current);
        String input = scanner.nextLine().trim();
        return input.isBlank() ? current : input;
    }

    /** Prompts for an int value; returns current if blank or invalid. */
    private int promptIntOrKeep(String label, int current) {
        System.out.printf(C + "  %s [%d]: " + R, label, current);
        String input = scanner.nextLine().trim();
        if (input.isBlank()) return current;
        try { return Integer.parseInt(input); }
        catch (NumberFormatException e) {
            System.out.println(Y + "    Invalid integer — keeping current." + R);
            return current;
        }
    }

    /** Prompts for a date "D MMM" (current year assumed); returns current if blank. */
    private LocalDate promptDateOrKeep(String label, LocalDate current) {
        System.out.printf(C + "  %s [%s]: " + R, label, current.format(DATE_FMT));
        String input = scanner.nextLine().trim();
        if (input.isBlank()) return current;
        try {
            return LocalDate.parse(input + " " + current.getYear(),
                    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            System.out.println(Y + "    Invalid date — keeping current." + R);
            return current;
        }
    }

    /** Prompts for a time "HH:MM"; returns current if blank. */
    private LocalTime promptTimeOrKeep(String label, LocalTime current) {
        System.out.printf(C + "  %s [%s]: " + R, label, current.format(TIME_FMT));
        String input = scanner.nextLine().trim();
        if (input.isBlank()) return current;
        try {
            return LocalTime.parse(input, DateTimeFormatter.ofPattern("H:mm"));
        } catch (DateTimeParseException e) {
            System.out.println(Y + "    Invalid time — keeping current." + R);
            return current;
        }
    }

    /** Prompts for a DayOfWeek name; returns current if blank. */
    private DayOfWeek promptDayOrKeep(String label, DayOfWeek current) {
        System.out.printf(C + "  %s [%s]: " + R, label, current.name());
        String input = scanner.nextLine().trim();
        if (input.isBlank()) return current;
        try {
            return DayOfWeek.valueOf(input.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            System.out.println(Y + "    Invalid day — keeping current." + R);
            return current;
        }
    }

    /** Asks a yes/no confirmation question; returns true for yes. */
    private boolean confirm(String question) {
        System.out.printf(C + "  %s (y/n): " + R, question);
        String response = scanner.nextLine().trim().toLowerCase(Locale.ENGLISH);
        return response.equals("y") || response.equals("yes");
    }

    // ── Formatting helpers ────────────────────────────────────────────────────

    private static String formatDateRange(LocalDate first, LocalDate last) {
        return first.format(DATE_FMT) + " – " + last.format(DATE_FMT);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static void printSeparator() {
        System.out.println("  " + "─".repeat(108));
    }
}

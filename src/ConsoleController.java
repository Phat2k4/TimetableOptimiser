import model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Console controller — the single co-ordinating class between the user and the
 * service / domain layers.
 *
 * <h3>Menu map</h3>
 * <pre>
 *   1  Import classes          → ClassDataService.importFromCsv
 *   2  Browse classes          → ClassDataService.browseAll
 *   3  View classes            → ClassDataService.viewTopicCode
 *   4  Search classes          → ClassDataService.search
 *   5  Edit class              → ClassDataService.edit  (select record first)
 *   6  Delete class            → ClassDataService.delete (select record first)
 *   7  Generate timetable      → TimetableService.create (wizard)
 *   8  Browse timetables       → TimetableService.browseAll
 *   9  View timetable          → TimetableService.view
 *  10  Edit timetable          → TimetableService.edit
 *  11  Delete timetable        → TimetableService.delete
 *  12  Export timetable        → TimetableService.export
 *  13  Exit
 * </pre>
 *
 * <h3>Design notes</h3>
 * <ul>
 *   <li>A single {@link Scanner} wrapping {@code System.in} is shared across all
 *       services (no double-close risk; closed only in {@link #run()}).</li>
 *   <li>{@link InputValidator.ValidationException} is caught at the top of every
 *       prompt loop and re-displayed in red.</li>
 *   <li>{@link AppState#save()} is called automatically before every clean exit.</li>
 * </ul>
 */
public final class ConsoleController {

    // ── ANSI shortcuts ────────────────────────────────────────────────────────
    private static final String R  = AppState.ANSI_RESET;
    private static final String G  = AppState.ANSI_GREEN;
    private static final String RE = AppState.ANSI_RED;
    private static final String Y  = AppState.ANSI_YELLOW;
    private static final String C  = AppState.ANSI_CYAN;
    private static final String B  = DisplayFormatter.BOLD;

    // ── Menu constants ────────────────────────────────────────────────────────
    private static final int CMD_MIN  =  1;
    private static final int CMD_MAX  = 13;
    private static final int CMD_EXIT = 13;

    // ── Services ──────────────────────────────────────────────────────────────
    private final Scanner           scanner;
    private final ClassDataService  classService;
    private final TimetableService  timetableService;

    // ── Available preference labels for the generation wizard ─────────────────
    private static final List<String> ALL_PREFERENCES = List.of(
            "Bedford Park", "Tonsley", "Flinders City Campus",
            "All at same campus",
            "Mornings", "Afternoons",
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Evenly Spread", "Compact");

    // ── Campus choices (for the wizard) ──────────────────────────────────────
    private static final List<String> CAMPUS_OPTIONS = List.of(
            "Bedford Park", "Tonsley", "Flinders City Campus", "(no preference)");

    // ─────────────────────────────────────────────────────────────────────────

    public ConsoleController() {
        this.scanner          = new Scanner(System.in);
        this.classService     = new ClassDataService(scanner);
        this.timetableService = new TimetableService(scanner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  run — main application loop
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Starts the application loop.  Loads persisted state on entry and saves on
     * every clean exit path (menu option 13, or via {@code System.exit}).
     */
    public void run() {
        // State already loaded in Main; show a ready message
        System.out.println(G + "  Ready. Type a menu number and press Enter." + R);

        boolean running = true;
        while (running) {
            showMenu();
            int cmd = readCommand();
            if (cmd == CMD_EXIT) {
                running = false;
            } else {
                dispatch(cmd);
            }
        }

        // Clean exit
        System.out.println(C + "\n  Saving state before exit…" + R);
        AppState.getInstance().save();
        System.out.println(G + "  Goodbye!\n" + R);
        scanner.close();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  showMenu
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Displays the main menu with Unicode box-drawing characters.
     */
    public void showMenu() {
        System.out.println();
        System.out.println(C + B +
                "  ╔══════════════════════════════════════════╗" + R);
        System.out.println(C + B +
                "  ║        MAIN MENU                         ║" + R);
        System.out.println(C +
                "  ╠══════════════════════════════════════════╣" + R);
        menuGroup("  CLASS DATA");
        menuItem( 1, "Import classes from CSV");
        menuItem( 2, "Browse all classes");
        menuItem( 3, "View classes by topic code");
        menuItem( 4, "Search classes");
        menuItem( 5, "Edit a class record");
        menuItem( 6, "Delete a class record");
        System.out.println(C +
                "  ╠══════════════════════════════════════════╣" + R);
        menuGroup("  TIMETABLES");
        menuItem( 7, "Generate new timetable");
        menuItem( 8, "Browse all timetables");
        menuItem( 9, "View a timetable");
        menuItem(10, "Edit a timetable");
        menuItem(11, "Delete a timetable");
        menuItem(12, "Export a timetable");
        System.out.println(C +
                "  ╠══════════════════════════════════════════╣" + R);
        menuItem(13, "Exit");
        System.out.println(C + B +
                "  ╚══════════════════════════════════════════╝" + R);
    }

    private static void menuGroup(String label) {
        System.out.println(C + "  ║" + Y + B + label + R +
                           spaces(42 - label.length() - 1) + C + "║" + R);
    }

    private static void menuItem(int n, String label) {
        String line = String.format("  ║  %s[%2d]%s %-35s", C, n, R, label);
        System.out.println(line + C + "  ║" + R);
    }

    private static String spaces(int n) {
        return n > 0 ? " ".repeat(n) : "";
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  readCommand
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Reads and validates the user's menu choice.  Re-prompts on invalid input.
     *
     * @return a validated integer in [1, 13]
     */
    public int readCommand() {
        while (true) {
            System.out.print(C + "\n  Enter choice [1–13]: " + R);
            String raw = scanner.nextLine();
            try {
                return InputValidator.validateMenuChoice(raw, CMD_MIN, CMD_MAX);
            } catch (InputValidator.ValidationException e) {
                System.out.println(RE + "  ✘ " + e.getMessage() + R);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  dispatch
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Routes a validated menu command to the appropriate service method.
     *
     * @param cmd a validated integer in [1, 12]
     */
    public void dispatch(int cmd) {
        switch (cmd) {
            case  1 -> handleImportClasses();
            case  2 -> handleBrowseClasses();
            case  3 -> handleViewTopicCode();
            case  4 -> handleSearchClasses();
            case  5 -> handleEditClass();
            case  6 -> handleDeleteClass();
            case  7 -> handleGenerateTimetable();
            case  8 -> handleBrowseTimetables();
            case  9 -> handleViewTimetable();
            case 10 -> handleEditTimetable();
            case 11 -> handleDeleteTimetable();
            case 12 -> handleExportTimetable();
            default -> System.out.println(RE + "  ✘ Unknown command: " + cmd + R);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Handlers — class data (options 1–6)
    // ══════════════════════════════════════════════════════════════════════════

    /** Option 1: Import classes from CSV. */
    private void handleImportClasses() {
        DisplayFormatter.printSectionHeader("Import Classes from CSV");
        String path = promptFilePath();
        if (path == null) return;
        classService.importFromCsv(path);
    }

    /** Option 2: Browse all classes. */
    private void handleBrowseClasses() {
        DisplayFormatter.printSectionHeader("Browse All Classes");
        classService.browseAll();
    }

    /** Option 3: View all sessions under a topic code. */
    private void handleViewTopicCode() {
        DisplayFormatter.printSectionHeader("View Classes by Topic Code");
        String code = promptTopicCode(true);
        if (code == null) return;
        classService.viewTopicCode(code);
    }

    /** Option 4: Search classes with multiple optional criteria. */
    private void handleSearchClasses() {
        DisplayFormatter.printSectionHeader("Search Classes");
        System.out.println(C + "  Enter search criteria (press Enter to skip any field):" + R);

        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put(ClassDataService.S_TOPIC_CODE,     prompt("  Topic code"));
        criteria.put(ClassDataService.S_TOPIC_NAME,     prompt("  Topic name"));
        criteria.put(ClassDataService.S_ATTEND_MODE,    prompt("  Attendance mode (e.g. On Campus)"));
        criteria.put(ClassDataService.S_CAMPUS,         prompt("  Campus"));
        criteria.put(ClassDataService.S_SEMESTER,       prompt("  Semester (e.g. S1)"));
        criteria.put(ClassDataService.S_AVAIL_NUMBER,   prompt("  Availability number"));
        criteria.put(ClassDataService.S_CLASS_NAME,     prompt("  Class type (e.g. Lecture)"));
        criteria.put(ClassDataService.S_CLASS_INSTANCE, prompt("  Class instance"));
        criteria.put(ClassDataService.S_DAY,            prompt("  Day (e.g. MONDAY)"));
        criteria.put(ClassDataService.S_START_TIME,     prompt("  Start time (HH:MM)"));
        criteria.put(ClassDataService.S_END_TIME,       prompt("  End time (HH:MM)"));
        criteria.put(ClassDataService.S_BUILDING,       prompt("  Building"));
        criteria.put(ClassDataService.S_ROOM,           prompt("  Room"));

        classService.search(criteria);
    }

    /**
     * Option 5: Edit a class record.
     * The user picks a topic, then a record is selected from search results.
     */
    private void handleEditClass() {
        DisplayFormatter.printSectionHeader("Edit Class Record");
        ClassDataService.FlatRecord record = selectFlatRecord("edit");
        if (record == null) return;
        classService.edit(record);
    }

    /**
     * Option 6: Delete a class record.
     * The user picks a topic, then a record is selected from search results.
     */
    private void handleDeleteClass() {
        DisplayFormatter.printSectionHeader("Delete Class Record");
        ClassDataService.FlatRecord record = selectFlatRecord("delete");
        if (record == null) return;
        classService.delete(record);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Handlers — timetable management (options 7–12)
    // ══════════════════════════════════════════════════════════════════════════

    /** Option 7: Generate a new timetable — full interactive wizard. */
    private void handleGenerateTimetable() {
        DisplayFormatter.printSectionHeader("Generate New Timetable");

        // Seed builder from last-used settings or fresh defaults
        TimetableGenerator.GenerationSettings last =
                TimetableGenerator.getLastUsedSettings();
        TimetableGenerator.GenerationSettings.Builder builder =
                (last != null) ? last.toBuilder()
                               : new TimetableGenerator.GenerationSettings.Builder();

        if (last != null) {
            System.out.println(C + "  (Previous settings shown in brackets — press Enter to keep)" + R);
        }

        // ── Timetable name ────────────────────────────────────────────────────
        String name = promptValidated(
                "  Timetable name (Enter for auto \"Timetable_N\")",
                raw -> InputValidator.validateName(raw));
        builder.name(name == null ? "" : name);

        // ── Semester ──────────────────────────────────────────────────────────
        String semDefault = (last != null ? "[" + last.semester + "] " : "");
        String semester = promptValidated(
                "  Semester " + semDefault + "(1 / 2 / both)",
                raw -> {
                    if (raw.isBlank() && last != null) return last.semester;
                    return InputValidator.validateSemester(raw);
                });
        builder.semester(semester);

        // ── Topics ────────────────────────────────────────────────────────────
        System.out.println(C + "\n  Available topics:" + R);
        List<Topic> allTopics = new ArrayList<>(AppState.getInstance().allTopics());
        if (allTopics.isEmpty()) {
            System.out.println(RE + "  ✘ No topics loaded. Import CSV data first (option 1)." + R);
            return;
        }
        for (int i = 0; i < allTopics.size(); i++) {
            System.out.printf("    %s[%2d]%s %s – %s%n",
                    C, i + 1, R,
                    allTopics.get(i).getTopicCode(),
                    allTopics.get(i).getTopicName());
        }

        String topicDefault = (last != null && !last.topicCodes.isEmpty())
                ? " [" + String.join(", ", last.topicCodes) + "]" : "";
        System.out.printf(C + "  Enter topic numbers%s (comma-separated, e.g. 1,3): " + R,
                topicDefault);
        List<String> topicCodes = promptTopicNumbers(allTopics, last);
        if (topicCodes == null) return;
        builder.topicCodes(topicCodes);

        // ── Campus preference ─────────────────────────────────────────────────
        System.out.println(C + "\n  Campus preference:" + R);
        for (int i = 0; i < CAMPUS_OPTIONS.size(); i++) {
            System.out.printf("    %s[%d]%s %s%n", C, i + 1, R, CAMPUS_OPTIONS.get(i));
        }
        String campusDefault = (last != null && !last.preferredCampus.isBlank())
                ? "[" + last.preferredCampus + "] " : "[any] ";
        String campusRaw = promptValidated(
                "  Choose campus " + campusDefault + "(1-4 or name)",
                raw -> {
                    if (raw.isBlank() && last != null) return last.preferredCampus;
                    // numeric shortcut
                    if (raw.equals("4") || raw.equalsIgnoreCase("none")) return "";
                    return InputValidator.validateCampus(raw);
                });
        builder.preferredCampus(campusRaw == null ? "" : campusRaw);

        // ── Lecture overlap ───────────────────────────────────────────────────
        boolean prevOverlap = (last != null && last.allowLectureOverlap);
        String overlapDefault = "[" + (prevOverlap ? "y" : "n") + "] ";
        boolean allowOverlap = promptYesNo(
                "  Allow lecture overlap? " + overlapDefault + "(y/n)",
                prevOverlap);
        builder.allowLectureOverlap(allowOverlap);

        // ── Preferences ───────────────────────────────────────────────────────
        List<String> prefOrder = promptPreferences(last);
        builder.preferenceOrder(prefOrder);

        // ── Generate ──────────────────────────────────────────────────────────
        TimetableGenerator.GenerationSettings settings = builder.build();
        timetableService.create(settings);
    }

    /** Option 8: Browse all timetables. */
    private void handleBrowseTimetables() {
        DisplayFormatter.printSectionHeader("Browse All Timetables");
        timetableService.browseAll();
    }

    /** Option 9: View a specific timetable. */
    private void handleViewTimetable() {
        DisplayFormatter.printSectionHeader("View Timetable");
        String name = promptTimetableName();
        if (name == null) return;
        timetableService.view(name);
    }

    /** Option 10: Edit (swap class instance) in a timetable. */
    private void handleEditTimetable() {
        DisplayFormatter.printSectionHeader("Edit Timetable");
        String name = promptTimetableName();
        if (name == null) return;
        timetableService.edit(name);
    }

    /** Option 11: Delete a timetable. */
    private void handleDeleteTimetable() {
        DisplayFormatter.printSectionHeader("Delete Timetable");
        String name = promptTimetableName();
        if (name == null) return;
        timetableService.delete(name);
    }

    /** Option 12: Export a timetable to file. */
    private void handleExportTimetable() {
        DisplayFormatter.printSectionHeader("Export Timetable");
        String name = promptTimetableName();
        if (name == null) return;
        String format = promptValidated(
                "  Export format (txt / csv)",
                raw -> InputValidator.validateExportFormat(raw));
        if (format == null) return;
        timetableService.export(name, format);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Wizard helpers — topic / preference selection
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Prompts the user to select one or more topics by number.
     * Returns a list of topic codes, or null on cancel.
     */
    private List<String> promptTopicNumbers(
            List<Topic> allTopics,
            TimetableGenerator.GenerationSettings last) {
        while (true) {
            String raw = scanner.nextLine().trim();
            if (raw.isBlank() && last != null && !last.topicCodes.isEmpty()) {
                return new ArrayList<>(last.topicCodes);
            }
            if (raw.equalsIgnoreCase("cancel")) return null;
            try {
                List<String> codes = new ArrayList<>();
                for (String part : raw.split(",")) {
                    part = part.trim();
                    if (part.isBlank()) continue;
                    // Accept either a number index or a topic code directly
                    try {
                        int idx = Integer.parseInt(part) - 1;
                        if (idx < 0 || idx >= allTopics.size()) {
                            throw new InputValidator.ValidationException(
                                    "Index " + (idx + 1) + " is out of range.");
                        }
                        codes.add(allTopics.get(idx).getTopicCode());
                    } catch (NumberFormatException e) {
                        // Treat as a topic code
                        String code = InputValidator.validateTopicCodeExists(part);
                        codes.add(code);
                    }
                }
                if (codes.isEmpty()) {
                    throw new InputValidator.ValidationException(
                            "At least one topic must be selected.");
                }
                return codes;
            } catch (InputValidator.ValidationException e) {
                System.out.println(RE + "  ✘ " + e.getMessage() + R);
                System.out.print(C + "  Try again (topic numbers or codes, comma-separated): " + R);
            }
        }
    }

    /**
     * Prompts the user to rank preferences from the available list.
     * Returns the ordered list of selected preference type strings.
     */
    private List<String> promptPreferences(TimetableGenerator.GenerationSettings last) {
        System.out.println(C + "\n  Available preferences (enter numbers in priority order):" + R);
        for (int i = 0; i < ALL_PREFERENCES.size(); i++) {
            System.out.printf("    %s[%2d]%s %s%n", C, i + 1, R, ALL_PREFERENCES.get(i));
        }

        String prevStr = "";
        if (last != null && !last.preferenceOrder.isEmpty()) {
            prevStr = " [" + String.join(", ", last.preferenceOrder) + "]";
        }

        System.out.printf(C + "  Enter preference numbers in priority order%s%n" + R, prevStr);
        System.out.println(C + "  (comma-separated, e.g. 5,6,13 — or Enter for none / keep previous):" + R);
        System.out.print(C + "  > " + R);

        while (true) {
            String raw = scanner.nextLine().trim();

            if (raw.isBlank()) {
                return (last != null) ? new ArrayList<>(last.preferenceOrder) : new ArrayList<>();
            }

            try {
                List<String> chosen = new ArrayList<>();
                for (String part : raw.split(",")) {
                    part = part.trim();
                    if (part.isBlank()) continue;
                    int idx = Integer.parseInt(part) - 1;
                    if (idx < 0 || idx >= ALL_PREFERENCES.size()) {
                        throw new InputValidator.ValidationException(
                                "Preference number " + (idx + 1) + " is out of range (1–"
                                + ALL_PREFERENCES.size() + ").");
                    }
                    String pref = ALL_PREFERENCES.get(idx);
                    if (!chosen.contains(pref)) chosen.add(pref);
                }
                return chosen;
            } catch (NumberFormatException e) {
                System.out.println(RE + "  ✘ Enter numbers only, separated by commas." + R);
            } catch (InputValidator.ValidationException e) {
                System.out.println(RE + "  ✘ " + e.getMessage() + R);
            }
            System.out.print(C + "  > " + R);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FlatRecord selection helper (for edit/delete class)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Guides the user to find and select a specific flat class record.
     * First prompts for a topic code, lists all records for that topic,
     * then lets the user pick one by number.
     *
     * @param action "edit" or "delete" — for contextual prompts
     * @return the selected record, or null if the user cancelled / no records found
     */
    private ClassDataService.FlatRecord selectFlatRecord(String action) {
        String code = promptTopicCode(true);
        if (code == null) return null;

        // Collect all flat records for this topic
        List<ClassDataService.FlatRecord> records = new ArrayList<>();
        Optional<Topic> topicOpt = AppState.getInstance().findTopic(code);
        if (topicOpt.isEmpty()) {
            System.out.println(RE + "  ✘ Topic not found: " + code + R);
            return null;
        }
        Topic topic = topicOpt.get();
        for (model.Availability av : topic.getAvailabilities()) {
            for (model.ClassOption opt : av.getClassOptions()) {
                // Reconstruct a FlatRecord using a package-visible constructor workaround:
                // we create it via the inner class (constructor is package-private in same file)
                records.add(makeFlatRecord(topic, av, opt));
            }
        }

        if (records.isEmpty()) {
            System.out.println(Y + "  No class records found for topic " + code + "." + R);
            return null;
        }

        System.out.printf(C + "%n  Records for %s:%n" + R, code);
        for (int i = 0; i < records.size(); i++) {
            System.out.printf("  %s[%2d]%s %s%n", C, i + 1, R,
                    DisplayFormatter.renderClass(records.get(i)));
        }
        System.out.printf(C + "\n  Select record to %s [1–%d] (0 to cancel): " + R,
                action, records.size());

        while (true) {
            String raw = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(raw);
                if (choice == 0) {
                    System.out.println(Y + "  Cancelled." + R);
                    return null;
                }
                if (choice < 1 || choice > records.size()) {
                    System.out.printf(RE + "  ✘ Enter a number between 1 and %d (or 0 to cancel).%n" + R,
                            records.size());
                } else {
                    return records.get(choice - 1);
                }
            } catch (NumberFormatException e) {
                System.out.print(RE + "  ✘ Invalid input. Enter a number: " + R);
            }
        }
    }

    /**
     * Creates a {@link ClassDataService.FlatRecord} by going through
     * the ClassDataService's own package-private constructor.
     * Since FlatRecord's constructor is package-private (default access),
     * we use a tiny helper that constructs it via the public API path —
     * i.e. we call ClassDataService's own flatRecords-style logic inline.
     */
    private static ClassDataService.FlatRecord makeFlatRecord(
            Topic topic, model.Availability av, model.ClassOption opt) {
        // FlatRecord constructor is package-private to ClassDataService.
        // We work around this by delegating to a filter search via the service
        // — or, since they're in the same (default) package, we can call it directly.
        return new ClassDataService.FlatRecord(topic, av, opt);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Low-level prompt helpers
    // ══════════════════════════════════════════════════════════════════════════

    /** Reads a raw line with a coloured prompt, returning the trimmed string (may be blank). */
    private String prompt(String label) {
        System.out.print(C + label + ": " + R);
        return scanner.nextLine().trim();
    }

    /**
     * Prompts until the validator succeeds.  Returns the validated value, or
     * null if the user types "cancel" (case-insensitive).
     */
    private String promptValidated(String label,
                                    ValidatorFn validator) {
        while (true) {
            System.out.print(C + label + ": " + R);
            String raw = scanner.nextLine().trim();
            if (raw.equalsIgnoreCase("cancel")) {
                System.out.println(Y + "  Cancelled." + R);
                return null;
            }
            try {
                return validator.validate(raw);
            } catch (InputValidator.ValidationException e) {
                System.out.println(RE + "  ✘ " + e.getMessage() + R);
            }
        }
    }

    @FunctionalInterface
    private interface ValidatorFn {
        String validate(String raw) throws InputValidator.ValidationException;
    }

    /** Prompts for a file path, validating existence. Returns null on cancel. */
    private String promptFilePath() {
        return promptValidated("  File path (or 'cancel')",
                raw -> InputValidator.validateFilePath(raw));
    }

    /**
     * Prompts for a topic code.
     *
     * @param mustExist if true, also checks AppState
     */
    private String promptTopicCode(boolean mustExist) {
        return promptValidated("  Topic code (e.g. COMP1234)",
                raw -> mustExist
                       ? InputValidator.validateTopicCodeExists(raw)
                       : InputValidator.validateTopicCode(raw));
    }

    /** Prompts for a timetable name or ID. Returns null on cancel. */
    private String promptTimetableName() {
        // Show existing timetables as a quick reference
        Collection<Timetable> all = AppState.getInstance().allTimetables();
        if (all.isEmpty()) {
            System.out.println(Y + "  No timetables exist. Generate one first (option 7)." + R);
            return null;
        }
        System.out.println(C + "  Existing timetables:" + R);
        for (Timetable tt : all) {
            System.out.printf("    • %s%s%s  [%s]%n", G, tt.getTimetableName(), R,
                    tt.getTimetableId());
        }
        return promptValidated("  Enter timetable name or ID",
                raw -> InputValidator.validateExistingTimetableName(raw));
    }

    /**
     * Prompts for a yes/no answer, with a fallback default when the user presses Enter.
     *
     * @param label    the prompt text (should include the default in brackets)
     * @param fallback the default value when input is blank
     */
    private boolean promptYesNo(String label, boolean fallback) {
        while (true) {
            System.out.print(C + label + ": " + R);
            String raw = scanner.nextLine().trim();
            if (raw.isBlank()) return fallback;
            try {
                return InputValidator.validateYesNo(raw);
            } catch (InputValidator.ValidationException e) {
                System.out.println(RE + "  ✘ " + e.getMessage() + R);
            }
        }
    }
}

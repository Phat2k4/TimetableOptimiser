import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for all timetable operations.
 *
 * <p>Delegates timetable generation to {@link TimetableGenerator} and
 * clash/commute detection to {@link ClashDetector}.
 *
 * <h3>Supported export formats:</h3>
 * <ul>
 *   <li>{@code "txt"} — plain-text table</li>
 *   <li>{@code "csv"} — comma-separated values</li>
 * </ul>
 *
 * <h3>ANSI colours:</h3>
 * Green = success, Red = error/clash, Yellow = warning/commute, Cyan = info/prompt.
 */
public final class TimetableService {

    // ── ANSI shortcuts ────────────────────────────────────────────────────────
    private static final String R  = AppState.ANSI_RESET;
    private static final String G  = AppState.ANSI_GREEN;
    private static final String RE = AppState.ANSI_RED;
    private static final String Y  = AppState.ANSI_YELLOW;
    private static final String C  = AppState.ANSI_CYAN;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter EXPORT_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final Scanner scanner;

    public TimetableService(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner must not be null");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  1. create
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Calls {@link TimetableGenerator} with the given settings, prints a
     * summary, and saves the timetable to {@link AppState}.
     *
     * @param settings user's generation customisations
     * @return the created {@link Timetable}, or null on failure
     */
    public Timetable create(TimetableGenerator.GenerationSettings settings) {
        Objects.requireNonNull(settings, "settings must not be null");
        System.out.println(C + "\n↳ Generating timetable…" + R);

        TimetableGenerator generator = new TimetableGenerator();
        TimetableGenerator.GenerationResult result = generator.generate(settings);

        if (!result.success) {
            System.out.println(RE + "✘ Generation failed: " + result.errorMessage + R);
            return null;
        }

        Timetable tt = result.timetable;
        System.out.printf(G + "✔ Timetable '%s' created successfully.%n" + R,
                tt.getTimetableName());

        if (!result.unmetPreferences.isEmpty()) {
            System.out.println(Y + "  ⚠ The following preferences could not be fully satisfied:");
            for (String pref : result.unmetPreferences) {
                System.out.println(Y + "    • " + pref + R);
            }
        }

        printTimetableSummary(tt);
        return tt;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. view
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Shows full class details for the named timetable.
     * Clash warnings are shown in red; commute warnings in yellow.
     *
     * @param name timetable name (case-insensitive) or ID
     */
    public void view(String name) {
        Timetable tt = findByName(name);
        if (tt == null) { printNotFound(name); return; }

        System.out.printf(C + "%n══ Timetable: %s  [%s]  Semester: %s ══%n" + R,
                tt.getTimetableName(), tt.getTimetableId(), tt.getSemesterN());
        System.out.println(C + "  Lecture overlap allowed: " + tt.isAllowLectureOverlap() + R);

        if (!tt.getPreferences().isEmpty()) {
            System.out.print(C + "  Preferences: " + R);
            System.out.println(tt.getEffectivePreferences().stream()
                    .map(Preference::getPreferenceType)
                    .collect(Collectors.joining(" > ")));
        }

        printSep();

        // Recompute warnings before display
        tt.recomputeWarnings();

        // Collect all sessions for clash/commute annotation
        List<SessionRow> allRows = collectRows(tt);

        for (TimetableEntry entry : tt.getEntries()) {
            Topic topic = entry.getTopic();

            // Entry heading with warning badges
            String badge = "";
            if (entry.isClashWarning())   badge += RE + " [⚠ CLASH]"   + R;
            if (entry.isCommuteWarning()) badge += Y  + " [⚠ COMMUTE]" + R;

            System.out.printf("%n  " + G + "%-12s" + R + " %s%s%n",
                    topic.getTopicCode(), topic.getTopicName(), badge);

            // Session detail table
            System.out.printf("  %-16s %3s  %-11s %-11s  %-6s %-6s  %-20s %s%n",
                    "Class (Instance)", "Day",
                    "First", "Last", "Start", "End", "Building", "Room");
            System.out.println("  " + "-".repeat(100));

            for (ClassOption opt : entry.getChosenOptions()) {
                ClassOffering of = opt.getClassOffering();
                ClassSession  cs = opt.getClassSession();
                Location      lo = cs.getLocation();

                // Per-row clash/commute annotation
                String rowBadge = buildRowBadge(opt, allRows, tt);

                System.out.printf("  %-14s(%2d)  %-3s  %-11s %-11s  %-6s %-6s  %-20s %s%s%n",
                        of.getClassName(), of.getClassInstance(),
                        cs.getDay().toString().substring(0, 3),
                        cs.getDateOfFirstClass().format(DATE_FMT),
                        cs.getDateOfLastClass().format(DATE_FMT),
                        cs.getStartTime().format(TIME_FMT),
                        cs.getEndTime().format(TIME_FMT),
                        lo.getBuilding(),
                        lo.getRoom(),
                        rowBadge);
            }
        }
        printSep();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. browseAll
    // ══════════════════════════════════════════════════════════════════════════

    /** Lists all timetables with name, ID, semester, topic count, and warning summary. */
    public void browseAll() {
        Collection<Timetable> all = AppState.getInstance().allTimetables();
        if (all.isEmpty()) {
            System.out.println(Y + "No timetables exist yet. Use 'create' to build one." + R);
            return;
        }

        System.out.println(C + "\n══ All Timetables ══" + R);
        printSep();
        System.out.printf("  %-5s %-30s %-10s %-7s %-8s %-8s%n",
                "#", "Name", "ID", "Sem", "Topics", "Warnings");
        System.out.println("  " + "-".repeat(80));

        int idx = 1;
        for (Timetable tt : all) {
            tt.recomputeWarnings();
            long clashes  = tt.getEntries().stream().filter(TimetableEntry::isClashWarning).count();
            long commutes = tt.getEntries().stream().filter(TimetableEntry::isCommuteWarning).count();

            String warnings = "";
            if (clashes  > 0) warnings += RE + clashes  + "✗clash "  + R;
            if (commutes > 0) warnings += Y  + commutes + "⚠commute" + R;
            if (warnings.isBlank()) warnings = G + "OK" + R;

            System.out.printf("  %-5d %-30s %-10s %-7s %-8d %s%n",
                    idx++,
                    tt.getTimetableName(),
                    tt.getTimetableId(),
                    tt.getSemesterN(),
                    tt.getEntries().size(),
                    warnings);

            // One-line topic summary
            String topics = tt.getEntries().stream()
                    .map(e -> e.getTopic().getTopicCode())
                    .collect(Collectors.joining(", "));
            System.out.println("        " + C + topics + R);
        }
        printSep();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. edit — swap one class instance for another
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Lets the user swap one class option for another with the same
     * {@code className} and {@code topicCode} within the named timetable.
     *
     * <p>Warns if the swap introduces a clash or commute violation and
     * requires confirmation to proceed.
     *
     * @param name timetable name or ID (case-insensitive)
     */
    public void edit(String name) {
        Timetable tt = findByName(name);
        if (tt == null) { printNotFound(name); return; }

        System.out.printf(C + "%n── Edit Timetable: %s ──%n" + R, tt.getTimetableName());

        // ── Step 1: pick a topic ──────────────────────────────────────────────
        List<TimetableEntry> entries = tt.getEntries();
        if (entries.isEmpty()) { System.out.println(Y + "  Timetable has no entries." + R); return; }

        System.out.println(C + "  Topics in this timetable:" + R);
        for (int i = 0; i < entries.size(); i++) {
            System.out.printf("  [%d] %s – %s%n", i + 1,
                    entries.get(i).getTopic().getTopicCode(),
                    entries.get(i).getTopic().getTopicName());
        }
        int topicIdx = promptInt("  Select topic number", 1, entries.size()) - 1;
        TimetableEntry entry = entries.get(topicIdx);
        Topic topic = entry.getTopic();

        // ── Step 2: pick a class option to replace ────────────────────────────
        List<ClassOption> current = entry.getChosenOptions();
        if (current.isEmpty()) { System.out.println(Y + "  No class options in this entry." + R); return; }

        System.out.println(C + "\n  Current class options for " + topic.getTopicCode() + ":" + R);
        for (int i = 0; i < current.size(); i++) {
            ClassOption co = current.get(i);
            System.out.printf("  [%d] %s%n", i + 1, formatOption(co));
        }
        int optIdx = promptInt("  Select option to swap", 1, current.size()) - 1;
        ClassOption toReplace = current.get(optIdx);
        String targetClassName = toReplace.getClassName();

        // ── Step 3: find available alternatives ───────────────────────────────
        // Alternatives = ClassOptions with same className in same topic's availabilities
        List<ClassOption> alternatives = findAlternatives(topic, targetClassName, toReplace);
        if (alternatives.isEmpty()) {
            System.out.println(Y + "  No alternative instances found for class type: "
                    + targetClassName + R);
            return;
        }

        System.out.printf(C + "%n  Available alternatives for '%s':%n" + R, targetClassName);
        for (int i = 0; i < alternatives.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, formatOption(alternatives.get(i)));
        }
        int altIdx = promptInt("  Select replacement", 1, alternatives.size()) - 1;
        ClassOption replacement = alternatives.get(altIdx);

        // ── Step 4: clash/commute check ───────────────────────────────────────
        // Build a temporary list of all chosen options excluding the one being replaced
        List<ClassOption> otherOptions = new ArrayList<>();
        for (TimetableEntry e : tt.getEntries()) {
            for (ClassOption co : e.getChosenOptions()) {
                if (co != toReplace) otherOptions.add(co);
            }
        }

        List<String> violations = checkViolations(replacement, otherOptions, tt);

        if (!violations.isEmpty()) {
            System.out.println(Y + "\n  ⚠ The replacement introduces the following issues:" + R);
            for (String v : violations) System.out.println(Y + "    • " + v + R);
            if (!confirm("  Proceed with swap despite warnings?")) {
                System.out.println(Y + "  Swap cancelled." + R);
                return;
            }
        }

        // ── Step 5: apply swap ────────────────────────────────────────────────
        entry.removeChosenOption(toReplace);
        entry.addChosenOption(replacement);
        tt.recomputeWarnings();
        AppState.getInstance().save();
        System.out.printf(G + "%n  ✔ Swapped to: %s%n" + R, formatOption(replacement));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. delete
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Deletes a timetable after user confirmation.
     *
     * @param name timetable name or ID (case-insensitive)
     */
    public void delete(String name) {
        Timetable tt = findByName(name);
        if (tt == null) { printNotFound(name); return; }

        System.out.printf(C + "%n── Delete Timetable ──%n" + R);
        System.out.printf("  Name: %s  ID: %s  Semester: %s%n",
                tt.getTimetableName(), tt.getTimetableId(), tt.getSemesterN());
        System.out.printf("  Topics: %d entries%n", tt.getEntries().size());

        if (!confirm("  Permanently delete '" + tt.getTimetableName() + "'?")) {
            System.out.println(Y + "  Deletion cancelled." + R);
            return;
        }

        boolean removed = AppState.getInstance().removeTimetable(tt.getTimetableId());
        if (removed) {
            System.out.println(G + "✔ Timetable deleted." + R);
            AppState.getInstance().save();
        } else {
            System.out.println(RE + "✘ Deletion failed." + R);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  6. export
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Exports the named timetable to a file.
     *
     * @param name   timetable name or ID (case-insensitive)
     * @param format "txt" or "csv" (case-insensitive)
     */
    public void export(String name, String format) {
        Timetable tt = findByName(name);
        if (tt == null) { printNotFound(name); return; }

        String fmt = (format == null ? "txt" : format.trim().toLowerCase(Locale.ENGLISH));
        if (!fmt.equals("txt") && !fmt.equals("csv")) {
            System.out.println(RE + "✘ Unsupported format: '" + format +
                    "'. Use 'txt' or 'csv'." + R);
            return;
        }

        String safeFilename = tt.getTimetableName().replaceAll("[^A-Za-z0-9_\\-]", "_");
        String timestamp    = LocalDateTime.now().format(EXPORT_TS);
        String filename     = safeFilename + "_" + timestamp + "." + fmt;

        String content = fmt.equals("csv")
                ? buildCsvExport(tt)
                : buildTxtExport(tt);

        try {
            Files.writeString(Path.of(filename), content, StandardCharsets.UTF_8);
            System.out.println(G + "✔ Exported to: " + filename + R);
        } catch (IOException e) {
            System.out.println(RE + "✘ Export failed: " + e.getMessage() + R);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Export builders
    // ═════════════════════════════════════════════════════════════════════════

    private String buildTxtExport(Timetable tt) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("  Timetable : ").append(tt.getTimetableName()).append('\n');
        sb.append("  ID        : ").append(tt.getTimetableId()).append('\n');
        sb.append("  Semester  : ").append(tt.getSemesterN()).append('\n');
        sb.append("  Lecture overlap allowed: ").append(tt.isAllowLectureOverlap()).append('\n');
        if (!tt.getEffectivePreferences().isEmpty()) {
            sb.append("  Preferences: ");
            sb.append(tt.getEffectivePreferences().stream()
                    .map(Preference::getPreferenceType)
                    .collect(Collectors.joining(" > ")));
            sb.append('\n');
        }
        sb.append("  Exported  : ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm")))
          .append('\n');
        sb.append("═══════════════════════════════════════════════════════════\n\n");

        tt.recomputeWarnings();

        for (TimetableEntry entry : tt.getEntries()) {
            Topic topic = entry.getTopic();
            sb.append("Topic: ").append(topic.getTopicCode())
              .append("  ").append(topic.getTopicName()).append('\n');
            if (entry.isClashWarning())   sb.append("  [WARNING: TIME CLASH]\n");
            if (entry.isCommuteWarning()) sb.append("  [WARNING: COMMUTE GAP < 30 MIN]\n");

            sb.append(String.format("  %-16s %3s  %-11s %-11s  %-6s %-6s  %-20s %s%n",
                    "Class (Instance)", "Day", "First", "Last",
                    "Start", "End", "Building", "Room"));
            sb.append("  ").append("-".repeat(98)).append('\n');

            for (ClassOption opt : entry.getChosenOptions()) {
                ClassOffering of = opt.getClassOffering();
                ClassSession  cs = opt.getClassSession();
                Location      lo = cs.getLocation();
                sb.append(String.format("  %-14s(%2d)  %-3s  %-11s %-11s  %-6s %-6s  %-20s %s%n",
                        of.getClassName(), of.getClassInstance(),
                        cs.getDay().toString().substring(0, 3),
                        cs.getDateOfFirstClass().format(DATE_FMT),
                        cs.getDateOfLastClass().format(DATE_FMT),
                        cs.getStartTime().format(TIME_FMT),
                        cs.getEndTime().format(TIME_FMT),
                        lo.getBuilding(),
                        lo.getRoom()));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String buildCsvExport(Timetable tt) {
        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("TimetableName,TimetableId,Semester,AllowLectureOverlap,");
        sb.append("TopicCode,TopicName,ClassName,ClassInstance,");
        sb.append("Day,FirstDate,LastDate,StartTime,EndTime,Building,Room,");
        sb.append("ClashWarning,CommuteWarning\n");

        tt.recomputeWarnings();

        for (TimetableEntry entry : tt.getEntries()) {
            Topic topic = entry.getTopic();
            for (ClassOption opt : entry.getChosenOptions()) {
                ClassOffering of = opt.getClassOffering();
                ClassSession  cs = opt.getClassSession();
                Location      lo = cs.getLocation();
                sb.append(csvField(tt.getTimetableName())).append(',')
                  .append(csvField(tt.getTimetableId())).append(',')
                  .append(csvField(tt.getSemesterN())).append(',')
                  .append(tt.isAllowLectureOverlap()).append(',')
                  .append(csvField(topic.getTopicCode())).append(',')
                  .append(csvField(topic.getTopicName())).append(',')
                  .append(csvField(of.getClassName())).append(',')
                  .append(of.getClassInstance()).append(',')
                  .append(cs.getDay()).append(',')
                  .append(cs.getDateOfFirstClass().format(DATE_FMT)).append(',')
                  .append(cs.getDateOfLastClass().format(DATE_FMT)).append(',')
                  .append(cs.getStartTime().format(TIME_FMT)).append(',')
                  .append(cs.getEndTime().format(TIME_FMT)).append(',')
                  .append(csvField(lo.getBuilding())).append(',')
                  .append(csvField(lo.getRoom())).append(',')
                  .append(entry.isClashWarning()).append(',')
                  .append(entry.isCommuteWarning()).append('\n');
            }
        }
        return sb.toString();
    }

    /** Wraps a CSV field in quotes if it contains commas or quotes. */
    private static String csvField(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Internal helpers
    // ═════════════════════════════════════════════════════════════════════════

    /** Finds a timetable by name (case-insensitive trim) or by ID. */
    private Timetable findByName(String nameOrId) {
        if (nameOrId == null || nameOrId.isBlank()) return null;
        String key = nameOrId.trim();
        for (Timetable tt : AppState.getInstance().allTimetables()) {
            if (tt.getTimetableName().equalsIgnoreCase(key) ||
                tt.getTimetableId().equalsIgnoreCase(key)) {
                return tt;
            }
        }
        return null;
    }

    /** Finds ClassOptions for the same className in the same topic's availabilities. */
    private List<ClassOption> findAlternatives(Topic topic, String className,
                                                ClassOption exclude) {
        List<ClassOption> results = new ArrayList<>();
        for (Availability av : topic.getAvailabilities()) {
            for (ClassOption opt : av.getClassOptions()) {
                if (opt.getClassName().equalsIgnoreCase(className) && !opt.equals(exclude)) {
                    results.add(opt);
                }
            }
        }
        return results;
    }

    /**
     * Checks whether {@code replacement} clashes with or violates commute gaps
     * against all {@code others}.  Returns a list of human-readable violation strings.
     */
    private List<String> checkViolations(ClassOption replacement,
                                          List<ClassOption> others,
                                          Timetable timetable) {
        List<String> violations = new ArrayList<>();
        ClassSession rs = replacement.getClassSession();

        for (ClassOption other : others) {
            ClassSession os = other.getClassSession();
            if (!rs.getDay().equals(os.getDay())) continue;

            if (ClashDetector.isLectureExempt(replacement, other, timetable)) continue;

            boolean sameCampus = rs.getLocation().isSameCampusAs(os.getLocation());
            if (sameCampus) {
                if (ClashDetector.hasTimeClash(rs, os)) {
                    violations.add(String.format("TIME CLASH with %s (%s %s–%s)",
                            other.getClassName() + " " + other.getClassInstance(),
                            os.getDay(), os.getStartTime().format(TIME_FMT),
                            os.getEndTime().format(TIME_FMT)));
                }
            } else {
                if (ClashDetector.violatesCommuteGap(rs, os)) {
                    violations.add(String.format("COMMUTE GAP < 30 min with %s (%s %s–%s)",
                            other.getClassName() + " " + other.getClassInstance(),
                            os.getDay(), os.getStartTime().format(TIME_FMT),
                            os.getEndTime().format(TIME_FMT)));
                }
            }
        }
        return violations;
    }

    // ── Row-level badge building for view() ───────────────────────────────────

    /** Lightweight record for per-session annotation. */
    private record SessionRow(ClassOption option, TimetableEntry entry) {}

    private List<SessionRow> collectRows(Timetable tt) {
        List<SessionRow> rows = new ArrayList<>();
        for (TimetableEntry entry : tt.getEntries()) {
            for (ClassOption opt : entry.getChosenOptions()) {
                rows.add(new SessionRow(opt, entry));
            }
        }
        return rows;
    }

    /**
     * Builds a per-row badge string by checking this option against all other
     * options in the timetable.
     */
    private String buildRowBadge(ClassOption opt, List<SessionRow> allRows, Timetable tt) {
        StringBuilder badge = new StringBuilder();
        ClassSession cs = opt.getClassSession();
        for (SessionRow other : allRows) {
            if (other.option() == opt) continue;
            ClassSession os = other.option().getClassSession();
            if (!cs.getDay().equals(os.getDay())) continue;
            if (ClashDetector.isLectureExempt(opt, other.option(), tt)) continue;

            boolean sameCampus = cs.getLocation().isSameCampusAs(os.getLocation());
            if (sameCampus && ClashDetector.hasTimeClash(cs, os)) {
                badge.append(RE).append(" [CLASH]").append(R);
                break;
            } else if (!sameCampus && ClashDetector.violatesCommuteGap(cs, os)) {
                badge.append(Y).append(" [COMMUTE]").append(R);
            }
        }
        return badge.toString();
    }

    // ── Printing helpers ──────────────────────────────────────────────────────

    private void printTimetableSummary(Timetable tt) {
        System.out.printf(C + "  Entries: %d topic(s)  |  Semester: %s%n" + R,
                tt.getEntries().size(), tt.getSemesterN());
        for (TimetableEntry e : tt.getEntries()) {
            System.out.printf("    • %s – %d class option(s)%n",
                    e.getTopic().getTopicCode(), e.getChosenOptions().size());
        }
    }

    private void printNotFound(String name) {
        System.out.println(RE + "✘ Timetable not found: '" + name +
                "'. Use browseAll() to see available timetables." + R);
    }

    private static void printSep() {
        System.out.println("  " + "─".repeat(108));
    }

    private static String formatOption(ClassOption opt) {
        ClassOffering of = opt.getClassOffering();
        ClassSession  cs = opt.getClassSession();
        Location      lo = cs.getLocation();
        return String.format("%s (instance %d) | %s %s–%s | %s, %s",
                of.getClassName(), of.getClassInstance(),
                cs.getDay(), cs.getStartTime().format(TIME_FMT),
                cs.getEndTime().format(TIME_FMT),
                lo.getBuilding(), lo.getRoom());
    }

    // ── Interactive prompts ───────────────────────────────────────────────────

    private int promptInt(String label, int min, int max) {
        while (true) {
            System.out.printf(C + "%s [%d-%d]: " + R, label, min, max);
            String line = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) return v;
                System.out.printf(Y + "  Please enter a number between %d and %d.%n" + R, min, max);
            } catch (NumberFormatException e) {
                System.out.println(Y + "  Invalid input — enter a number." + R);
            }
        }
    }

    private boolean confirm(String question) {
        System.out.printf(C + "%s (y/n): " + R, question);
        String r = scanner.nextLine().trim().toLowerCase(Locale.ENGLISH);
        return r.equals("y") || r.equals("yes");
    }
}

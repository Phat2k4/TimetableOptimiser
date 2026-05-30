import model.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure rendering utilities for the Student Timetable Optimiser console UI.
 *
 * <p>All methods are static — this class holds no state.  It is responsible only
 * for formatting; it never reads user input or modifies domain objects.
 *
 * <h3>ANSI colour conventions:</h3>
 * <ul>
 *   <li>Cyan   — section headers, labels, prompts</li>
 *   <li>Green  — success messages, topic codes, clean status</li>
 *   <li>Red    — errors, clash warnings</li>
 *   <li>Yellow — soft warnings, commute violations, unmet preferences</li>
 *   <li>Reset  — always appended after coloured text</li>
 * </ul>
 */
public final class DisplayFormatter {

    // ── ANSI constants (re-export for convenience) ────────────────────────────
    public static final String RESET  = AppState.ANSI_RESET;
    public static final String GREEN  = AppState.ANSI_GREEN;
    public static final String RED    = AppState.ANSI_RED;
    public static final String YELLOW = AppState.ANSI_YELLOW;
    public static final String CYAN   = AppState.ANSI_CYAN;
    public static final String BOLD   = "\u001B[1m";

    // ── Date / time formatters ────────────────────────────────────────────────
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    private DisplayFormatter() {}   // utility class

    // ══════════════════════════════════════════════════════════════════════════
    //  renderTitle — ASCII art banner printed on startup
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Prints the ASCII art application title to stdout in cyan.
     * Called once from {@code Main.main()}.
     */
    public static void renderTitle() {
        String art = CYAN + BOLD +
            "\n" +
            "  ╔══════════════════════════════════════════════════════════════════╗\n" +
            "  ║                                                                  ║\n" +
            "  ║   ███████╗████████╗ ██████╗                                     ║\n" +
            "  ║   ██╔════╝╚══██╔══╝██╔═══██╗                                    ║\n" +
            "  ║   ███████╗   ██║   ██║   ██║                                    ║\n" +
            "  ║   ╚════██║   ██║   ██║   ██║                                    ║\n" +
            "  ║   ███████║   ██║   ╚██████╔╝                                    ║\n" +
            "  ║   ╚══════╝   ╚═╝    ╚═════╝                                     ║\n" +
            "  ║                                                                  ║\n" +
            "  ║        Student Timetable Optimiser  v1.0                         ║\n" +
            "  ║        Flinders University  ·  Java 17  ·  Console Edition       ║\n" +
            "  ║                                                                  ║\n" +
            "  ╚══════════════════════════════════════════════════════════════════╝\n" +
            RESET;
        System.out.println(art);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  renderClass — formats a single ClassDataService.FlatRecord
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Formats a {@link ClassDataService.FlatRecord} as a compact single-line
     * string suitable for list views and search results.
     *
     * <p>Format:
     * <pre>
     *   [COMP1234] Introduction to Computing | Lecture (1) | MON 09:00–11:00 | Building A, Room 101
     * </pre>
     */
    public static String renderClass(ClassDataService.FlatRecord data) {
        return String.format("%s[%-10s]%s %-35s %s %-14s (%d)%s %s %-3s %s–%s  %s, %s",
                GREEN,
                data.topicCode,
                RESET,
                truncate(data.topicName, 34),
                CYAN, data.className, data.classInstance, RESET,
                YELLOW,
                data.day.toString().substring(0, 3),
                data.startTime.format(TIME_FMT),
                data.endTime.format(TIME_FMT),
                RESET,
                data.building,
                data.room);
    }

    /**
     * Formats a {@link ClassDataService.FlatRecord} as a verbose multi-line block,
     * including all context fields (availability, semester, campus, dates).
     */
    public static String renderClassVerbose(ClassDataService.FlatRecord data) {
        return String.format(
                "%s┌─ %s %s ─%s%n" +
                "│  Topic      : %s%s%s%n" +
                "│  Class      : %s (instance %d)%n" +
                "│  Availability: %s | Campus: %s | Sem: %s | Avail#: %s%n" +
                "│  Day / Time : %s  %s – %s%n" +
                "│  Dates      : %s → %s%n" +
                "│  Location   : %s, %s%n" +
                "%s└─────────────────────────────────────────────────────────────%s",
                CYAN,
                data.topicCode,
                data.topicName,
                RESET,
                GREEN, data.topicCode, RESET,
                data.className, data.classInstance,
                data.attendanceMode, data.campusLocation, data.semesterN, data.availabilityNumber,
                data.day, data.startTime.format(TIME_FMT), data.endTime.format(TIME_FMT),
                data.firstDate.format(DATE_FMT), data.lastDate.format(DATE_FMT),
                data.building, data.room,
                CYAN, RESET);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  renderTimetable — full timetable rendering with clash/commute badges
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Formats a complete {@link Timetable} to a {@link String}, including
     * clash and commute-warning annotations on each affected row.
     *
     * <p>This is a formatting-only method — it calls
     * {@link Timetable#recomputeWarnings()} before rendering but does not
     * persist any state.
     *
     * @param t the timetable to render
     * @return the full formatted string (may span many lines)
     */
    public static String renderTimetable(Timetable t) {
        t.recomputeWarnings();

        StringBuilder sb = new StringBuilder();

        // ── Header block ──────────────────────────────────────────────────────
        sb.append(CYAN).append(BOLD)
          .append("\n  ╔══════════════════════════════════════════════════════════╗\n")
          .append(RESET);
        sb.append(headerLine("  ║  Timetable : " + t.getTimetableName()));
        sb.append(headerLine("  ║  ID        : " + t.getTimetableId()
                             + "   Semester: " + t.getSemesterN()));
        sb.append(headerLine("  ║  Lecture overlap: "
                             + (t.isAllowLectureOverlap() ? "Allowed" : "Not allowed")));

        if (!t.getEffectivePreferences().isEmpty()) {
            String prefs = t.getEffectivePreferences().stream()
                    .map(Preference::getPreferenceType)
                    .collect(Collectors.joining(" › "));
            sb.append(headerLine("  ║  Preferences: " + prefs));
        }

        // Warning summary
        long clashCount   = t.getEntries().stream().filter(TimetableEntry::isClashWarning).count();
        long commuteCount = t.getEntries().stream().filter(TimetableEntry::isCommuteWarning).count();
        if (clashCount > 0 || commuteCount > 0) {
            String warn = "";
            if (clashCount   > 0) warn += RED    + "  " + clashCount   + " clash(es)"   + RESET;
            if (commuteCount > 0) warn += YELLOW + "  " + commuteCount + " commute gap(s)" + RESET;
            sb.append("  ║  Warnings  :").append(warn).append('\n');
        }
        sb.append(CYAN).append(BOLD)
          .append("  ╚══════════════════════════════════════════════════════════╝\n")
          .append(RESET);

        // ── Entries ───────────────────────────────────────────────────────────
        List<ClassOption> allOptions = collectAllOptions(t);

        for (TimetableEntry entry : t.getEntries()) {
            Topic topic = entry.getTopic();

            // Topic heading line
            String badge = "";
            if (entry.isClashWarning())   badge += RED    + " ⚠ CLASH"   + RESET;
            if (entry.isCommuteWarning()) badge += YELLOW + " ⚠ COMMUTE" + RESET;

            sb.append('\n');
            sb.append(GREEN).append(BOLD)
              .append("  ► ").append(topic.getTopicCode()).append(RESET)
              .append("  ").append(topic.getTopicName())
              .append(badge).append('\n');

            // Column header
            sb.append("    ")
              .append(String.format("%-16s %-9s %-11s %-11s %-6s %-6s %-22s %-12s%n",
                      "Class (Inst)", "Day",
                      "First", "Last",
                      "Start", "End",
                      "Building", "Room"));
            sb.append("    ").append("─".repeat(100)).append('\n');

            for (ClassOption opt : entry.getChosenOptions()) {
                ClassOffering of = opt.getClassOffering();
                ClassSession  cs = opt.getClassSession();
                Location      lo = cs.getLocation();

                String rowBadge = rowBadge(opt, allOptions, t);

                sb.append("    ")
                  .append(String.format("%-14s(%2d) %-9s %-11s %-11s %-6s %-6s %-22s %-12s",
                          of.getClassName(), of.getClassInstance(),
                          cs.getDay().toString().substring(0, 3),
                          cs.getDateOfFirstClass().format(DATE_FMT),
                          cs.getDateOfLastClass().format(DATE_FMT),
                          cs.getStartTime().format(TIME_FMT),
                          cs.getEndTime().format(TIME_FMT),
                          truncate(lo.getBuilding(), 21),
                          lo.getRoom()))
                  .append(rowBadge)
                  .append('\n');
            }
        }

        sb.append('\n');
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  applyAnsiStyle — programmatic styling helper
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Wraps {@code text} with the specified ANSI escape sequence and appends
     * a reset code.
     *
     * <p>Recognised {@code style} values:
     * <ul>
     *   <li>{@code "green"}, {@code "red"}, {@code "yellow"}, {@code "cyan"}</li>
     *   <li>{@code "bold"}</li>
     *   <li>{@code "error"}   — alias for red</li>
     *   <li>{@code "warn"}    — alias for yellow</li>
     *   <li>{@code "success"} — alias for green</li>
     *   <li>{@code "info"}    — alias for cyan</li>
     * </ul>
     *
     * @param text  the text to style
     * @param style the style name (case-insensitive)
     * @return the styled string, or the plain text if the style is unrecognised
     */
    public static String applyAnsiStyle(String text, String style) {
        if (text == null) return "";
        String ansi = switch (style == null ? "" : style.toLowerCase(Locale.ENGLISH)) {
            case "green",   "success" -> GREEN;
            case "red",     "error"   -> RED;
            case "yellow",  "warn"    -> YELLOW;
            case "cyan",    "info"    -> CYAN;
            case "bold"               -> BOLD;
            default                   -> "";
        };
        return ansi.isBlank() ? text : ansi + text + RESET;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Section dividers / banners
    // ══════════════════════════════════════════════════════════════════════════

    /** Prints a full-width cyan section divider. */
    public static void printDivider() {
        System.out.println(CYAN + "  " + "═".repeat(70) + RESET);
    }

    /** Prints a thinner separator line. */
    public static void printSeparator() {
        System.out.println("  " + "─".repeat(70));
    }

    /**
     * Prints a section header: cyan, bold, with a Unicode arrow.
     *
     * @param title the section name
     */
    public static void printSectionHeader(String title) {
        System.out.println();
        System.out.println(CYAN + BOLD + "  ▶ " + title + RESET);
        printSeparator();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Private helpers
    // ══════════════════════════════════════════════════════════════════════════

    /** Pads a header line to fill the box width. */
    private static String headerLine(String content) {
        // pad to column 62 then close the box
        int padLen = Math.max(0, 62 - content.length());
        return content + " ".repeat(padLen) + "║\n";
    }

    /** Collects all ClassOptions from all entries for pairwise clash checks. */
    private static List<ClassOption> collectAllOptions(Timetable t) {
        List<ClassOption> all = new ArrayList<>();
        for (TimetableEntry e : t.getEntries()) all.addAll(e.getChosenOptions());
        return all;
    }

    /**
     * Builds a per-row badge string by checking {@code opt} against all other
     * options in the timetable using {@link ClashDetector}.
     */
    private static String rowBadge(ClassOption opt,
                                    List<ClassOption> allOptions,
                                    Timetable t) {
        ClassSession cs = opt.getClassSession();
        StringBuilder badge = new StringBuilder();
        for (ClassOption other : allOptions) {
            if (other == opt) continue;
            ClassSession os = other.getClassSession();
            if (!cs.getDay().equals(os.getDay())) continue;
            if (ClashDetector.isLectureExempt(opt, other, t)) continue;

            if (cs.getLocation().isSameCampusAs(os.getLocation())) {
                if (ClashDetector.hasTimeClash(cs, os)) {
                    badge.append(RED).append(" [CLASH]").append(RESET);
                    break;
                }
            } else {
                if (ClashDetector.violatesCommuteGap(cs, os)) {
                    badge.append(YELLOW).append(" [COMMUTE <30 min]").append(RESET);
                }
            }
        }
        return badge.toString();
    }

    /** Truncates {@code s} to at most {@code max} characters, appending "…" if cut. */
    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}

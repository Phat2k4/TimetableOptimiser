import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * Reads a timetable CSV file and populates {@link AppState} with
 * {@link Topic} / {@link Availability} / {@link ClassOption} data.
 *
 * <h3>Expected CSV columns (header row required):</h3>
 * <pre>
 *   Topic | Availability | Class | Class instance | Date | Day | Time | Location
 * </pre>
 *
 * <h3>Duplicate detection key:</h3>
 * Topic + Availability + Class + ClassInstance + Date + Day (all six fields).
 * When a duplicate is found, only Time and Location are updated.
 *
 * <h3>Date format:</h3>  "DD MMM - DD MMM"  (current year assumed)
 * <h3>Time format:</h3>  "HH:MM - HH:MM"   (24-hour)
 * <h3>Location format:</h3> "Building, Room"
 */
public final class CsvImporter {

    // ── ANSI shortcuts ────────────────────────────────────────────────────────
    private static final String R  = AppState.ANSI_RESET;
    private static final String G  = AppState.ANSI_GREEN;
    private static final String RE = AppState.ANSI_RED;
    private static final String Y  = AppState.ANSI_YELLOW;
    private static final String C  = AppState.ANSI_CYAN;

    // ── Date / time formatters ────────────────────────────────────────────────
    /** Parses "28 Jul" with the current year injected by the caller. */
    private static final DateTimeFormatter DATE_PART_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    /** Parses "09:00". */
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH);

    // ── Expected column header names (case-insensitive, trimmed) ─────────────
    private static final String COL_TOPIC     = "topic";
    private static final String COL_AVAIL     = "availability";
    private static final String COL_CLASS     = "class";
    private static final String COL_INSTANCE  = "class instance";
    private static final String COL_DATE      = "date";
    private static final String COL_DAY       = "day";
    private static final String COL_TIME      = "time";
    private static final String COL_LOCATION  = "location";

    private static final List<String> REQUIRED_COLS = List.of(
            COL_TOPIC, COL_AVAIL, COL_CLASS, COL_INSTANCE,
            COL_DATE, COL_DAY, COL_TIME, COL_LOCATION);

    // ── Result counters ───────────────────────────────────────────────────────
    private int imported = 0;
    private int updated  = 0;
    private int skipped  = 0;

    // ── Public entry point ────────────────────────────────────────────────────

    /**
     * Imports records from {@code filePath} into {@link AppState}.
     *
     * @param filePath path to the CSV file
     * @return true if the file was processed without a fatal error
     */
    public boolean importFile(String filePath) {
        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            System.out.println(RE + "✘ File not found: " + filePath + R);
            return false;
        }
        if (!Files.isRegularFile(path)) {
            System.out.println(RE + "✘ Path is not a regular file: " + filePath + R);
            return false;
        }

        imported = 0; updated = 0; skipped = 0;
        AppState state = AppState.getInstance();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            // ── Header row ────────────────────────────────────────────────────
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                System.out.println(RE + "✘ CSV file is empty or has no header row." + R);
                return false;
            }
            // Strip UTF-8 BOM if present
            if (headerLine.charAt(0) == 0xFEFF) headerLine = headerLine.substring(1);
            Map<String, Integer> colIndex = parseHeader(headerLine);
            if (colIndex == null) return false;   // error already printed

            // ── Data rows ─────────────────────────────────────────────────────
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                processRow(line, lineNum, colIndex, state);
            }

        } catch (IOException e) {
            System.out.println(RE + "✘ I/O error reading file: " + e.getMessage() + R);
            return false;
        }

        // ── Summary ───────────────────────────────────────────────────────────
        System.out.println(G  + "✔ Import complete." + R);
        System.out.printf( G  + "  New records imported : %d%n" + R, imported);
        System.out.printf( C  + "  Records updated      : %d%n" + R, updated);
        if (skipped > 0) {
            System.out.printf(Y + "  Rows skipped (bad)   : %d%n" + R, skipped);
        }

        if (imported > 0 || updated > 0) {
            state.save();
        }
        return true;
    }

    // ── Header parsing ────────────────────────────────────────────────────────

    /**
     * Builds a column-name → index map from the header line.
     * Returns null (and prints an error) if any required column is absent.
     *
     * <p>Accepted aliases for the location column (case-insensitive):
     * {@code location}, {@code room}, {@code venue}, {@code building},
     * {@code campus/room}.  The first matching alias is mapped to the
     * canonical key {@code "location"} so the rest of the importer
     * never needs to know which name the CSV file used.
     */
    private Map<String, Integer> parseHeader(String headerLine) {
        String[] cols = splitCsv(headerLine);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < cols.length; i++) {
            map.put(cols[i].trim().toLowerCase(), i);
        }

        // ── Accept alternative column names for the location field ────────────
        // Covers CSV files that call this column "Room", "Venue", etc.
        if (!map.containsKey(COL_LOCATION)) {
            for (String alias : new String[]{"room", "venue", "building", "campus/room"}) {
                if (map.containsKey(alias)) {
                    map.put(COL_LOCATION, map.get(alias));
                    System.out.println(C + "  ℹ Column '" + alias +
                            "' recognised as 'location'." + R);
                    break;
                }
            }
        }
        // ─────────────────────────────────────────────────────────────────────

        List<String> missing = new ArrayList<>();
        for (String req : REQUIRED_COLS) {
            if (!map.containsKey(req)) missing.add(req);
        }
        if (!missing.isEmpty()) {
            System.out.println(RE + "✘ CSV header is missing required column(s): " + missing + R);
            System.out.println(RE + "  Expected: " + REQUIRED_COLS + R);
            System.out.println(RE + "  Accepted aliases for 'location': room, venue, building" + R);
            return null;
        }
        return map;
    }

    // ── Row processing ────────────────────────────────────────────────────────

    private void processRow(String line, int lineNum,
                            Map<String, Integer> colIdx, AppState state) {
        String[] cells = splitCsv(line);
        try {
            // Extract raw cell values
            String rawTopic    = cell(cells, colIdx, COL_TOPIC);
            String rawAvail    = cell(cells, colIdx, COL_AVAIL);
            String rawClass    = cell(cells, colIdx, COL_CLASS);
            String rawInstance = cell(cells, colIdx, COL_INSTANCE);
            String rawDate     = cell(cells, colIdx, COL_DATE);
            String rawDay      = cell(cells, colIdx, COL_DAY);
            String rawTime     = cell(cells, colIdx, COL_TIME);
            String rawLocation = cell(cells, colIdx, COL_LOCATION);

            // Validate non-blank for key fields
            requireNonBlank(rawTopic,    "Topic",          lineNum);
            requireNonBlank(rawAvail,    "Availability",   lineNum);
            requireNonBlank(rawClass,    "Class",          lineNum);
            requireNonBlank(rawInstance, "Class instance", lineNum);
            requireNonBlank(rawDate,     "Date",           lineNum);
            requireNonBlank(rawDay,      "Day",            lineNum);
            requireNonBlank(rawTime,     "Time",           lineNum);
            requireNonBlank(rawLocation, "Location",       lineNum);

            // Parse key fields
            Topic          topic    = parseTopic(rawTopic, state);
            Availability   avail    = findOrCreateAvailability(topic, rawAvail);
            String         className = rawClass.trim();
            int            instance  = parseInstance(rawInstance, lineNum);
            LocalDate[]    dates     = parseDateRange(rawDate, lineNum);
            DayOfWeek      day       = parseDay(rawDay, lineNum);

            // Parse update fields
            LocalTime[]    times     = parseTimeRange(rawTime, lineNum);
            Location       location  = Location.parse(rawLocation);

            ClassOffering offering = new ClassOffering(className, instance);

            // ── Duplicate check ───────────────────────────────────────────────
            // Key: topic + availability + className + instance + dateRange + day
            ClassOption existing = findExisting(avail, offering, dates[0], dates[1], day);

            if (existing != null) {
                // Update: replace the ClassOption with new time + location
                ClassSession newSession = new ClassSession(
                        dates[0], dates[1], day, times[0], times[1], location);
                ClassOption  newOpt     = new ClassOption(offering, newSession);
                replaceOption(avail, existing, newOpt);
                updated++;
            } else {
                // New record
                ClassSession session = new ClassSession(
                        dates[0], dates[1], day, times[0], times[1], location);
                ClassOption  opt     = new ClassOption(offering, session);
                avail.addClassOption(opt);
                imported++;
            }

        } catch (CsvRowException e) {
            System.out.printf(Y + "  ⚠ Row %d skipped: %s%n" + R, lineNum, e.getMessage());
            skipped++;
        } catch (Exception e) {
            System.out.printf(Y + "  ⚠ Row %d skipped (unexpected error): %s%n" + R,
                    lineNum, e.getMessage());
            skipped++;
        }
    }

    // ── Duplicate detection ───────────────────────────────────────────────────

    /**
     * Searches {@code avail}'s class options for one whose offering and session
     * key (dates + day) match.  Returns null if none found.
     */
    private ClassOption findExisting(Availability avail,
                                     ClassOffering offering,
                                     LocalDate firstDate,
                                     LocalDate lastDate,
                                     DayOfWeek day) {
        for (ClassOption opt : avail.getClassOptions()) {
            ClassOffering co = opt.getClassOffering();
            ClassSession  cs = opt.getClassSession();
            if (co.equals(offering)
                    && cs.getDateOfFirstClass().equals(firstDate)
                    && cs.getDateOfLastClass().equals(lastDate)
                    && cs.getDay().equals(day)) {
                return opt;
            }
        }
        return null;
    }

    /**
     * Replaces {@code oldOpt} with {@code newOpt} inside {@code avail}.
     * Uses the package-visible mutator added to Availability.
     */
    private void replaceOption(Availability avail,
                               ClassOption  oldOpt,
                               ClassOption  newOpt) {
        avail.replaceClassOption(oldOpt, newOpt);
    }

    // ── Topic / Availability helpers ──────────────────────────────────────────

    private Topic parseTopic(String rawTopic, AppState state) {
        Topic candidate = Topic.parse(rawTopic);
        Optional<Topic> existing = state.findTopic(candidate.getTopicCode());
        if (existing.isPresent()) return existing.get();
        state.addTopic(candidate);
        return candidate;
    }

    private Availability findOrCreateAvailability(Topic topic, String rawAvail) {
        return topic.findOrCreateAvailability(rawAvail);
    }

    // ── Field parsers ─────────────────────────────────────────────────────────

    /**
     * Parses "DD MMM - DD MMM" into a [firstDate, lastDate] pair.
     * Uses the current calendar year.
     */
    private LocalDate[] parseDateRange(String raw, int lineNum) throws CsvRowException {
        String[] parts = raw.split(" - ", 2);
        if (parts.length != 2) {
            throw new CsvRowException("Invalid Date format (expected 'DD MMM - DD MMM'): " + raw);
        }
        int year = LocalDate.now().getYear();
        try {
            LocalDate first = LocalDate.parse(parts[0].trim() + " " + year, DATE_PART_FMT);
            LocalDate last  = LocalDate.parse(parts[1].trim() + " " + year, DATE_PART_FMT);
            // Handle year wrap-around (e.g. Dec - Feb)
            if (last.isBefore(first)) last = last.plusYears(1);
            return new LocalDate[]{first, last};
        } catch (DateTimeParseException e) {
            throw new CsvRowException("Cannot parse date '" + raw + "': " + e.getMessage());
        }
    }

    /**
     * Parses "HH:MM - HH:MM" into a [startTime, endTime] pair.
     */
    private LocalTime[] parseTimeRange(String raw, int lineNum) throws CsvRowException {
        String[] parts = raw.split(" - ", 2);
        if (parts.length != 2) {
            throw new CsvRowException("Invalid Time format (expected 'HH:MM - HH:MM'): " + raw);
        }
        try {
            LocalTime start = LocalTime.parse(parts[0].trim(), TIME_FMT);
            LocalTime end   = LocalTime.parse(parts[1].trim(), TIME_FMT);
            return new LocalTime[]{start, end};
        } catch (DateTimeParseException e) {
            throw new CsvRowException("Cannot parse time '" + raw + "': " + e.getMessage());
        }
    }

    /** Parses the Day column into a {@link DayOfWeek}. */
    private DayOfWeek parseDay(String raw, int lineNum) throws CsvRowException {
        try {
            String dayStr = raw.trim();
            // Strip qualifiers like "(once-only)" before parsing
            int paren = dayStr.indexOf('(');
            if (paren >= 0) dayStr = dayStr.substring(0, paren).trim();
            return DayOfWeek.valueOf(dayStr.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new CsvRowException("Unknown day value: '" + raw + "'");
        }
    }

    /** Parses the Class instance column as an integer. */
    private int parseInstance(String raw, int lineNum) throws CsvRowException {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new CsvRowException("Class instance is not a valid integer: '" + raw + "'");
        }
    }

    // ── CSV splitting ─────────────────────────────────────────────────────────

    /**
     * Splits a CSV line respecting quoted fields (double-quote RFC 4180).
     */
    static String[] splitCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"'); i++;   // escaped quote ""
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    // ── Small helpers ─────────────────────────────────────────────────────────

    private String cell(String[] cells, Map<String, Integer> colIdx, String col) {
        int idx = colIdx.get(col);
        return idx < cells.length ? cells[idx].trim() : "";
    }

    private void requireNonBlank(String value, String fieldName, int lineNum)
            throws CsvRowException {
        if (value == null || value.isBlank()) {
            throw new CsvRowException("Missing required field '" + fieldName + "'");
        }
    }

    // ── Accessors (for testing / reporting) ───────────────────────────────────

    public int getImported() { return imported; }
    public int getUpdated()  { return updated; }
    public int getSkipped()  { return skipped; }

    // ── Inner exception ───────────────────────────────────────────────────────

    /** Signals a skippable row-level parse error. */
    private static final class CsvRowException extends Exception {
        CsvRowException(String message) { super(message); }
    }
}
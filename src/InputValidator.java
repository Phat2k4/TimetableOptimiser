import model.AppState;
import model.Topic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;

/**
 * Stateless validation helpers for all user-facing input in the console layer.
 *
 * <p>Every method either returns a validated/normalised value, or throws
 * {@link ValidationException} with a human-readable message that the controller
 * can print in red and re-prompt.
 *
 * <h3>ANSI colours:</h3>
 * Red = validation error, Yellow = warning hint.
 */
public final class InputValidator {

    // ── ANSI shortcuts ────────────────────────────────────────────────────────
    private static final String R  = AppState.ANSI_RESET;
    private static final String RE = AppState.ANSI_RED;
    private static final String Y  = AppState.ANSI_YELLOW;

    private InputValidator() {}   // utility class

    // ══════════════════════════════════════════════════════════════════════════
    //  Menu choice
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Validates that the raw input is an integer within [{@code min}, {@code max}].
     *
     * @param raw  raw string read from stdin
     * @param min  inclusive lower bound
     * @param max  inclusive upper bound
     * @return the parsed integer
     * @throws ValidationException if the value is non-numeric or out of range
     */
    public static int validateMenuChoice(String raw, int min, int max)
            throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Please enter a number between "
                    + min + " and " + max + ".");
        }
        int value;
        try {
            value = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(
                    "'" + raw.trim() + "' is not a valid number. Enter a value between "
                    + min + " and " + max + ".");
        }
        if (value < min || value > max) {
            throw new ValidationException(
                    value + " is out of range. Choose between " + min + " and " + max + ".");
        }
        return value;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  File path
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Validates that the given path string points to an existing, readable,
     * regular file.  The extension is not enforced here (CsvImporter checks
     * the header row, which is a stronger guarantee).
     *
     * @param raw raw path string entered by the user
     * @return the trimmed, validated path string
     * @throws ValidationException if the path is blank, does not exist, or is not a file
     */
    public static String validateFilePath(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("File path must not be empty.");
        }
        String trimmed = raw.trim();
        Path path = Path.of(trimmed);

        if (!Files.exists(path)) {
            throw new ValidationException("File not found: " + trimmed);
        }
        if (!Files.isRegularFile(path)) {
            throw new ValidationException("Path is not a regular file: " + trimmed);
        }
        if (!Files.isReadable(path)) {
            throw new ValidationException("File is not readable: " + trimmed);
        }
        return trimmed;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Topic code
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Validates a topic code: must be non-blank and match the pattern
     * {@code [A-Za-z]{2,8}[0-9]{1,6}} (e.g. COMP1234, NURS101).
     *
     * <p>The code is normalised to upper-case before it is returned.
     *
     * @param raw raw topic code entered by the user
     * @return the trimmed, upper-cased topic code
     * @throws ValidationException if the code is blank or does not match the pattern
     */
    public static String validateTopicCode(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Topic code must not be empty.");
        }
        String code = raw.trim().toUpperCase(Locale.ENGLISH);
        // Allow codes like "COMP1234", "NURS101", "MED3001A"
        if (!code.matches("[A-Z]{2,8}[0-9]{1,6}[A-Z]?")) {
            throw new ValidationException(
                    "'" + code + "' does not look like a valid topic code " +
                    "(expected 2–8 letters followed by 1–6 digits, e.g. COMP1234).");
        }
        return code;
    }

    /**
     * Like {@link #validateTopicCode(String)} but also checks that the topic
     * already exists in {@link AppState}.
     *
     * @throws ValidationException if the code is invalid or the topic is not loaded
     */
    public static String validateTopicCodeExists(String raw) throws ValidationException {
        String code = validateTopicCode(raw);
        if (AppState.getInstance().findTopic(code).isEmpty()) {
            throw new ValidationException(
                    "Topic '" + code + "' is not in the system. " +
                    "Import its CSV data first (option 1).");
        }
        return code;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Timetable name
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Validates a timetable display name.
     *
     * <ul>
     *   <li>If blank, the empty string is returned (signals auto-generation).</li>
     *   <li>If non-blank: trimmed, then checked for uniqueness (case-insensitive)
     *       against all existing timetable names.</li>
     *   <li>Names longer than 60 characters are rejected.</li>
     * </ul>
     *
     * @param raw raw name string entered by the user
     * @return the trimmed name (may be empty for auto-generated names)
     * @throws ValidationException if the name is too long or already in use
     */
    public static String validateName(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            return "";   // blank → auto-generate
        }
        String trimmed = raw.trim();

        if (trimmed.length() > 60) {
            throw new ValidationException(
                    "Timetable name is too long (" + trimmed.length() +
                    " characters). Maximum is 60.");
        }

        // Uniqueness check (case-insensitive)
        Collection<model.Timetable> existing = AppState.getInstance().allTimetables();
        for (model.Timetable tt : existing) {
            if (tt.getTimetableName().equalsIgnoreCase(trimmed)) {
                throw new ValidationException(
                        "A timetable named '" + trimmed + "' already exists. " +
                        "Choose a different name or leave blank for auto-naming.");
            }
        }
        return trimmed;
    }

    /**
     * Validates that a timetable name or ID refers to an existing timetable.
     * Returns the name/ID as-is for the service layer to resolve.
     *
     * @throws ValidationException if blank
     */
    public static String validateExistingTimetableName(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Timetable name or ID must not be empty.");
        }
        return raw.trim();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  General helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Validates a non-blank free-text field (e.g. a search query, export format).
     *
     * @param raw       the raw input
     * @param fieldName a human-readable label used in error messages
     * @return the trimmed value
     * @throws ValidationException if blank
     */
    public static String validateNonBlank(String raw, String fieldName)
            throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(fieldName + " must not be empty.");
        }
        return raw.trim();
    }

    /**
     * Validates a yes/no answer, accepting "y", "yes", "n", "no" (case-insensitive).
     *
     * @return true for yes, false for no
     * @throws ValidationException for any other input
     */
    public static boolean validateYesNo(String raw) throws ValidationException {
        if (raw == null) throw new ValidationException("Please enter 'y' or 'n'.");
        switch (raw.trim().toLowerCase(Locale.ENGLISH)) {
            case "y", "yes" -> { return true; }
            case "n", "no"  -> { return false; }
            default -> throw new ValidationException(
                    "Invalid response '" + raw.trim() + "'. Enter 'y' or 'n'.");
        }
    }

    /**
     * Validates that a semester choice is "1", "2", or "both" (case-insensitive).
     *
     * @return normalised value: "1", "2", or "both"
     * @throws ValidationException for any other input
     */
    public static String validateSemester(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Semester must be '1', '2', or 'both'.");
        }
        return switch (raw.trim().toLowerCase(Locale.ENGLISH)) {
            case "1"          -> "1";
            case "2"          -> "2";
            case "both", "12" -> "both";
            default -> throw new ValidationException(
                    "'" + raw.trim() + "' is not a valid semester. Enter '1', '2', or 'both'.");
        };
    }

    /**
     * Validates that a campus choice matches one of the allowed values.
     *
     * @return the matched campus string (canonical casing), or "" for no preference
     * @throws ValidationException for unrecognised input
     */
    public static String validateCampus(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) return "";   // no preference
        return switch (raw.trim().toLowerCase(Locale.ENGLISH)) {
            case "1", "bedford park", "bp"          -> "Bedford Park";
            case "2", "tonsley"                      -> "Tonsley";
            case "3", "city", "flinders city",
                 "flinders city campus", "fcc"       -> "Flinders City Campus";
            case "0", "none", "any", ""              -> "";
            default -> throw new ValidationException(
                    "Unknown campus '" + raw.trim() + "'. Choose: Bedford Park, Tonsley, " +
                    "Flinders City Campus, or leave blank for any.");
        };
    }

    /**
     * Validates that an export format is "txt" or "csv".
     *
     * @return the lower-cased format string
     * @throws ValidationException for unrecognised input
     */
    public static String validateExportFormat(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Export format must be 'txt' or 'csv'.");
        }
        return switch (raw.trim().toLowerCase(Locale.ENGLISH)) {
            case "txt", "text" -> "txt";
            case "csv"         -> "csv";
            default -> throw new ValidationException(
                    "Unknown format '" + raw.trim() + "'. Use 'txt' or 'csv'.");
        };
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ValidationException
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Signals a recoverable validation failure in the presentation layer.
     * The controller catches this and re-prompts the user.
     */
    public static final class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}

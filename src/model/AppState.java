package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Application-wide singleton holding all {@link Topic}s and {@link Timetable}s
 * in memory.
 *
 * <p>Persistence is handled via a hand-rolled JSON serialiser / deserialiser that
 * uses only the Java standard library (no third-party JSON libraries).
 *
 * <p>Saved to {@code appstate.json} in the working directory.
 *
 * <h3>ANSI colour codes used in console output:</h3>
 * <ul>
 *   <li>Green  – success</li>
 *   <li>Red    – error</li>
 *   <li>Yellow – warning</li>
 *   <li>Cyan   – prompt / info</li>
 * </ul>
 */
public final class AppState {

    // ── ANSI colour constants ─────────────────────────────────────────────────

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN   = "\u001B[36m";

    // ── Persistence file ──────────────────────────────────────────────────────

    private static final String SAVE_FILE = "appstate.json";

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static AppState instance;

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
            instance.load();
        }
        return instance;
    }

    // ── In-memory state ───────────────────────────────────────────────────────

    /** All topics known to the application, keyed by topicCode (upper-case). */
    private final Map<String, Topic> topics = new LinkedHashMap<>();

    /**
     * All timetables, keyed by timetableId.
     * Note: a single-user app owns all timetables; Student association is
     * recorded inside Timetable and persisted in JSON.
     */
    private final Map<String, Timetable> timetables = new LinkedHashMap<>();

    /** Counter used to auto-generate timetable IDs. */
    private int timetableCounter = 0;

    private AppState() {}

    // ── Topic management ──────────────────────────────────────────────────────

    public void addTopic(Topic topic) {
        Objects.requireNonNull(topic);
        topics.put(topic.getTopicCode().toUpperCase(), topic);
    }

    public Optional<Topic> findTopic(String topicCode) {
        return Optional.ofNullable(topics.get(topicCode.toUpperCase()));
    }

    public Collection<Topic> allTopics() {
        return Collections.unmodifiableCollection(topics.values());
    }

    // ── Timetable management ──────────────────────────────────────────────────

    /**
     * Registers a timetable.  Auto-assigns an ID if timetableId is null/blank.
     */
    public Timetable addTimetable(Timetable timetable) {
        Objects.requireNonNull(timetable);
        timetables.put(timetable.getTimetableId(), timetable);
        return timetable;
    }

    /** Generates the next auto timetable ID (e.g. "TT-1", "TT-2"). */
    public String nextTimetableId() {
        return "TT-" + (++timetableCounter);
    }

    /** Generates the next auto timetable display name (e.g. "Timetable_1"). */
    public String nextAutoName() {
        return Timetable.generateAutoName(timetableCounter);
    }

    public Optional<Timetable> findTimetable(String timetableId) {
        return Optional.ofNullable(timetables.get(timetableId));
    }

    public Collection<Timetable> allTimetables() {
        return Collections.unmodifiableCollection(timetables.values());
    }

    public boolean removeTimetable(String timetableId) {
        return timetables.remove(timetableId) != null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Persistence — save
    // ══════════════════════════════════════════════════════════════════════════

    public void save() {
        try {
            String json = serialise();
            Files.writeString(Path.of(SAVE_FILE), json, StandardCharsets.UTF_8);
            System.out.println(ANSI_GREEN + "✔ State saved to " + SAVE_FILE + ANSI_RESET);
        } catch (IOException e) {
            System.err.println(ANSI_RED + "✘ Failed to save state: " + e.getMessage() + ANSI_RESET);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Persistence — load
    // ══════════════════════════════════════════════════════════════════════════

    public void load() {
        Path path = Path.of(SAVE_FILE);
        if (!Files.exists(path)) {
            System.out.println(ANSI_CYAN + "ℹ No saved state found; starting fresh." + ANSI_RESET);
            return;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            deserialise(json);
            System.out.println(ANSI_GREEN + "✔ State loaded from " + SAVE_FILE + ANSI_RESET);
        } catch (Exception e) {
            System.err.println(ANSI_RED + "✘ Failed to load state: " + e.getMessage() + ANSI_RESET);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Hand-rolled JSON serialiser
    // ══════════════════════════════════════════════════════════════════════════

    private String serialise() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"timetableCounter\": ").append(timetableCounter).append(",\n");

        // ── Topics ────────────────────────────────────────────────────────────
        sb.append("  \"topics\": [\n");
        List<Topic> topicList = new ArrayList<>(topics.values());
        for (int ti = 0; ti < topicList.size(); ti++) {
            serialiseTopic(sb, topicList.get(ti), "    ");
            if (ti < topicList.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        // ── Timetables ────────────────────────────────────────────────────────
        sb.append("  \"timetables\": [\n");
        List<Timetable> ttList = new ArrayList<>(timetables.values());
        for (int ti = 0; ti < ttList.size(); ti++) {
            serialiseTimetable(sb, ttList.get(ti), "    ");
            if (ti < ttList.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    private void serialiseTopic(StringBuilder sb, Topic topic, String indent) {
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"topicCode\": ").append(jsonStr(topic.getTopicCode())).append(",\n");
        sb.append(indent).append("  \"topicName\": ").append(jsonStr(topic.getTopicName())).append(",\n");
        sb.append(indent).append("  \"availabilities\": [\n");
        List<Availability> avails = topic.getAvailabilities();
        for (int i = 0; i < avails.size(); i++) {
            serialiseAvailability(sb, avails.get(i), indent + "    ");
            if (i < avails.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ]\n");
        sb.append(indent).append("}");
    }

    private void serialiseAvailability(StringBuilder sb, Availability av, String indent) {
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"attendanceMode\": ").append(jsonStr(av.getAttendanceMode())).append(",\n");
        sb.append(indent).append("  \"campusLocation\": ").append(jsonStr(av.getCampusLocation())).append(",\n");
        sb.append(indent).append("  \"semesterN\": ").append(jsonStr(av.getSemesterN())).append(",\n");
        sb.append(indent).append("  \"availabilityNumber\": ").append(jsonStr(av.getAvailabilityNumber())).append(",\n");
        sb.append(indent).append("  \"classOptions\": [\n");
        List<ClassOption> opts = av.getClassOptions();
        for (int i = 0; i < opts.size(); i++) {
            serialiseClassOption(sb, opts.get(i), indent + "    ");
            if (i < opts.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ]\n");
        sb.append(indent).append("}");
    }

    private void serialiseClassOption(StringBuilder sb, ClassOption opt, String indent) {
        sb.append(indent).append("{\n");
        // ClassOffering
        ClassOffering co = opt.getClassOffering();
        sb.append(indent).append("  \"className\": ").append(jsonStr(co.getClassName())).append(",\n");
        sb.append(indent).append("  \"classInstance\": ").append(co.getClassInstance()).append(",\n");
        // ClassSession
        ClassSession cs = opt.getClassSession();
        sb.append(indent).append("  \"dateOfFirstClass\": ").append(jsonStr(cs.getDateOfFirstClass().toString())).append(",\n");
        sb.append(indent).append("  \"dateOfLastClass\": ").append(jsonStr(cs.getDateOfLastClass().toString())).append(",\n");
        sb.append(indent).append("  \"day\": ").append(jsonStr(cs.getDay().name())).append(",\n");
        sb.append(indent).append("  \"startTime\": ").append(jsonStr(cs.getStartTime().toString())).append(",\n");
        sb.append(indent).append("  \"endTime\": ").append(jsonStr(cs.getEndTime().toString())).append(",\n");
        // Location
        Location loc = cs.getLocation();
        sb.append(indent).append("  \"building\": ").append(jsonStr(loc.getBuilding())).append(",\n");
        sb.append(indent).append("  \"room\": ").append(jsonStr(loc.getRoom())).append("\n");
        sb.append(indent).append("}");
    }

    private void serialiseTimetable(StringBuilder sb, Timetable tt, String indent) {
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"timetableName\": ").append(jsonStr(tt.getTimetableName())).append(",\n");
        sb.append(indent).append("  \"timetableId\": ").append(jsonStr(tt.getTimetableId())).append(",\n");
        sb.append(indent).append("  \"semesterN\": ").append(jsonStr(tt.getSemesterN())).append(",\n");
        sb.append(indent).append("  \"allowLectureOverlap\": ").append(tt.isAllowLectureOverlap()).append(",\n");

        // Preferences
        sb.append(indent).append("  \"preferences\": [\n");
        List<Preference> prefs = tt.getPreferences();
        for (int i = 0; i < prefs.size(); i++) {
            Preference p = prefs.get(i);
            sb.append(indent).append("    { \"preferenceType\": ")
              .append(jsonStr(p.getPreferenceType()))
              .append(", \"priorityOrder\": ").append(p.getPriorityOrder()).append(" }");
            if (i < prefs.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ],\n");

        // Entries
        sb.append(indent).append("  \"entries\": [\n");
        List<TimetableEntry> entries = tt.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            serialiseTimetableEntry(sb, entries.get(i), indent + "    ");
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ]\n");
        sb.append(indent).append("}");
    }

    private void serialiseTimetableEntry(StringBuilder sb, TimetableEntry entry, String indent) {
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"topicCode\": ").append(jsonStr(entry.getTopic().getTopicCode())).append(",\n");
        sb.append(indent).append("  \"clashWarning\": ").append(entry.isClashWarning()).append(",\n");
        sb.append(indent).append("  \"commuteWarning\": ").append(entry.isCommuteWarning()).append(",\n");
        // Chosen options — serialise inline (className + instance + session key)
        sb.append(indent).append("  \"chosenOptions\": [\n");
        List<ClassOption> opts = entry.getChosenOptions();
        for (int i = 0; i < opts.size(); i++) {
            serialiseClassOption(sb, opts.get(i), indent + "    ");
            if (i < opts.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ]\n");
        sb.append(indent).append("}");
    }

    // ── Tiny JSON string helper ───────────────────────────────────────────────

    private static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t") + "\"";
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Hand-rolled JSON deserialiser  (recursive-descent / tokeniser)
    // ══════════════════════════════════════════════════════════════════════════

    private void deserialise(String json) {
        JsonParser parser = new JsonParser(json);
        Map<String, Object> root = parser.parseObject();

        // timetableCounter
        Object counterObj = root.get("timetableCounter");
        if (counterObj instanceof Number n) {
            timetableCounter = n.intValue();
        }

        // topics
        Object topicsObj = root.get("topics");
        if (topicsObj instanceof List<?> topicList) {
            for (Object t : topicList) {
                if (t instanceof Map<?, ?> tm) {
                    @SuppressWarnings("unchecked")
                    Topic topic = deserialiseTopic((Map<String, Object>) tm);
                    topics.put(topic.getTopicCode().toUpperCase(), topic);
                }
            }
        }

        // timetables
        Object ttObj = root.get("timetables");
        if (ttObj instanceof List<?> ttList) {
            for (Object t : ttList) {
                if (t instanceof Map<?, ?> tm) {
                    @SuppressWarnings("unchecked")
                    Timetable tt = deserialiseTimetable((Map<String, Object>) tm);
                    timetables.put(tt.getTimetableId(), tt);
                }
            }
        }
    }

    private Topic deserialiseTopic(Map<String, Object> m) {
        String code = str(m, "topicCode");
        String name = str(m, "topicName");
        Topic topic = new Topic(code, name);

        Object availsObj = m.get("availabilities");
        if (availsObj instanceof List<?> availList) {
            for (Object a : availList) {
                if (a instanceof Map<?, ?> am) {
                    @SuppressWarnings("unchecked")
                    Availability av = deserialiseAvailability((Map<String, Object>) am);
                    topic.addAvailability(av);
                }
            }
        }
        return topic;
    }

    private Availability deserialiseAvailability(Map<String, Object> m) {
        Availability av = new Availability(
                str(m, "attendanceMode"),
                str(m, "campusLocation"),
                str(m, "semesterN"),
                str(m, "availabilityNumber"));

        Object optsObj = m.get("classOptions");
        if (optsObj instanceof List<?> optList) {
            for (Object o : optList) {
                if (o instanceof Map<?, ?> om) {
                    @SuppressWarnings("unchecked")
                    ClassOption opt = deserialiseClassOption((Map<String, Object>) om);
                    av.addClassOption(opt);
                }
            }
        }
        return av;
    }

    private ClassOption deserialiseClassOption(Map<String, Object> m) {
        ClassOffering offering = new ClassOffering(
                str(m, "className"),
                intVal(m, "classInstance"));

        LocalDate first = LocalDate.parse(str(m, "dateOfFirstClass"));
        LocalDate last  = LocalDate.parse(str(m, "dateOfLastClass"));
        DayOfWeek day   = DayOfWeek.valueOf(str(m, "day"));
        LocalTime start = LocalTime.parse(str(m, "startTime"));
        LocalTime end   = LocalTime.parse(str(m, "endTime"));
        Location  loc   = new Location(str(m, "building"), str(m, "room"));

        ClassSession session = new ClassSession(first, last, day, start, end, loc);
        return new ClassOption(offering, session);
    }

    private Timetable deserialiseTimetable(Map<String, Object> m) {
        Timetable tt = new Timetable(
                str(m, "timetableName"),
                str(m, "timetableId"),
                str(m, "semesterN"),
                boolVal(m, "allowLectureOverlap"));

        // Preferences
        Object prefsObj = m.get("preferences");
        if (prefsObj instanceof List<?> prefList) {
            for (Object p : prefList) {
                if (p instanceof Map<?, ?> pm) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pmap = (Map<String, Object>) pm;
                    tt.addPreference(new Preference(str(pmap, "preferenceType"),
                                                    intVal(pmap, "priorityOrder")));
                }
            }
        }

        // Entries
        Object entriesObj = m.get("entries");
        if (entriesObj instanceof List<?> entryList) {
            for (Object e : entryList) {
                if (e instanceof Map<?, ?> em) {
                    @SuppressWarnings("unchecked")
                    TimetableEntry entry = deserialiseTimetableEntry((Map<String, Object>) em);
                    tt.addEntry(entry);
                }
            }
        }
        return tt;
    }

    private TimetableEntry deserialiseTimetableEntry(Map<String, Object> m) {
        String topicCode = str(m, "topicCode");
        // Look up or reconstruct a minimal Topic stub for reference
        Topic topic = topics.computeIfAbsent(topicCode.toUpperCase(),
                k -> new Topic(topicCode, ""));

        TimetableEntry entry = new TimetableEntry(topic);
        entry.setClashWarning(boolVal(m, "clashWarning"));
        entry.setCommuteWarning(boolVal(m, "commuteWarning"));

        Object optsObj = m.get("chosenOptions");
        if (optsObj instanceof List<?> optList) {
            for (Object o : optList) {
                if (o instanceof Map<?, ?> om) {
                    @SuppressWarnings("unchecked")
                    ClassOption opt = deserialiseClassOption((Map<String, Object>) om);
                    entry.addChosenOption(opt);
                }
            }
        }
        return entry;
    }

    // ── Deserialise helpers ───────────────────────────────────────────────────

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : v.toString();
    }

    private static int intVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(str(m, key)); } catch (NumberFormatException e) { return 0; }
    }

    private static boolean boolVal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Boolean b) return b;
        return "true".equalsIgnoreCase(str(m, key));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Minimal recursive-descent JSON parser (standard Java only)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Parses a JSON document into Java primitives:
     * <ul>
     *   <li>JSON object → {@code Map<String, Object>}</li>
     *   <li>JSON array  → {@code List<Object>}</li>
     *   <li>JSON string → {@code String}</li>
     *   <li>JSON number → {@code Double} or {@code Long}</li>
     *   <li>JSON true/false → {@code Boolean}</li>
     *   <li>JSON null → {@code null}</li>
     * </ul>
     */
    private static final class JsonParser {

        private final String src;
        private int pos;

        JsonParser(String src) {
            this.src = Objects.requireNonNull(src);
            this.pos = 0;
        }

        Map<String, Object> parseObject() {
            skipWs();
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWs();
            if (peek() == '}') { pos++; return map; }
            while (true) {
                skipWs();
                String key = parseString();
                skipWs();
                expect(':');
                skipWs();
                Object value = parseValue();
                map.put(key, value);
                skipWs();
                char c = peek();
                if (c == '}') { pos++; break; }
                if (c == ',') { pos++; continue; }
                throw new IllegalStateException("Expected ',' or '}' at pos " + pos + " got '" + c + "'");
            }
            return map;
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWs();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                skipWs();
                list.add(parseValue());
                skipWs();
                char c = peek();
                if (c == ']') { pos++; break; }
                if (c == ',') { pos++; continue; }
                throw new IllegalStateException("Expected ',' or ']' at pos " + pos);
            }
            return list;
        }

        private Object parseValue() {
            skipWs();
            char c = peek();
            return switch (c) {
                case '"' -> parseString();
                case '{' -> parseObject();
                case '[' -> parseArray();
                case 't' -> parseLiteral("true",  Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null",  null);
                default  -> parseNumber();
            };
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') break;
                if (c == '\\') {
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/'  -> sb.append('/');
                        case 'n'  -> sb.append('\n');
                        case 'r'  -> sb.append('\r');
                        case 't'  -> sb.append('\t');
                        case 'u'  -> {
                            String hex = src.substring(pos, pos + 4); pos += 4;
                            sb.append((char) Integer.parseInt(hex, 16));
                        }
                        default   -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Number parseNumber() {
            int start = pos;
            if (peek() == '-') pos++;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) ||
                   src.charAt(pos) == '.' || src.charAt(pos) == 'e' ||
                   src.charAt(pos) == 'E' || src.charAt(pos) == '+' ||
                   src.charAt(pos) == '-')) pos++;
            String numStr = src.substring(start, pos);
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.parseDouble(numStr);
            }
            return Long.parseLong(numStr);
        }

        private Object parseLiteral(String literal, Object value) {
            if (src.startsWith(literal, pos)) {
                pos += literal.length();
                return value;
            }
            throw new IllegalStateException("Expected literal '" + literal + "' at pos " + pos);
        }

        private void skipWs() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }

        private char peek() {
            if (pos >= src.length()) throw new IllegalStateException("Unexpected end of JSON");
            return src.charAt(pos);
        }

        private void expect(char c) {
            if (peek() != c) throw new IllegalStateException(
                    "Expected '" + c + "' at pos " + pos + " but got '" + peek() + "'");
            pos++;
        }
    }
}

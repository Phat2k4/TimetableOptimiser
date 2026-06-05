import model.*;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DisplayFormatterTest {

    private Topic topic(String code, String name) {
        return new Topic(code, name);
    }

    private Location location(String building, String room) {
        return new Location(building, room);
    }

    private ClassSession session(LocalTime start, LocalTime end, Location location) {
        return new ClassSession(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 6, 1),
                DayOfWeek.MONDAY,
                start,
                end,
                location
        );
    }

    private ClassOption option(String className, int instance,
                               LocalTime start, LocalTime end,
                               String building, String room) {
        return new ClassOption(
                new ClassOffering(className, instance),
                session(start, end, location(building, room))
        );
    }

    private Timetable timetable() {
        return new Timetable("My Timetable", "TT001", "S1", false);
    }

    @Test
    @Order(1)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies green style")
    @Tag("binn0049")
    @Tag("core")
    void tc001_applyAnsiStyleShouldApplyGreenStyle() {
        String result = DisplayFormatter.applyAnsiStyle("Success", "green");

        assertTrue(result.contains("Success"));
        assertTrue(result.startsWith(DisplayFormatter.GREEN));
        assertTrue(result.endsWith(DisplayFormatter.RESET));
    }

    @Test
    @Order(2)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies red style")
    @Tag("binn0049")
    @Tag("core")
    void tc002_applyAnsiStyleShouldApplyRedStyle() {
        String result = DisplayFormatter.applyAnsiStyle("Error", "red");

        assertTrue(result.contains("Error"));
        assertTrue(result.startsWith(DisplayFormatter.RED));
        assertTrue(result.endsWith(DisplayFormatter.RESET));
    }

    @Test
    @Order(3)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies yellow style")
    @Tag("binn0049")
    @Tag("core")
    void tc003_applyAnsiStyleShouldApplyYellowStyle() {
        String result = DisplayFormatter.applyAnsiStyle("Warning", "yellow");

        assertTrue(result.contains("Warning"));
        assertTrue(result.startsWith(DisplayFormatter.YELLOW));
        assertTrue(result.endsWith(DisplayFormatter.RESET));
    }

    @Test
    @Order(4)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies cyan style")
    @Tag("binn0049")
    @Tag("core")
    void tc004_applyAnsiStyleShouldApplyCyanStyle() {
        String result = DisplayFormatter.applyAnsiStyle("Info", "cyan");

        assertTrue(result.contains("Info"));
        assertTrue(result.startsWith(DisplayFormatter.CYAN));
        assertTrue(result.endsWith(DisplayFormatter.RESET));
    }

    @Test
    @Order(5)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies bold style")
    @Tag("binn0049")
    @Tag("core")
    void tc005_applyAnsiStyleShouldApplyBoldStyle() {
        String result = DisplayFormatter.applyAnsiStyle("Bold", "bold");

        assertTrue(result.contains("Bold"));
        assertTrue(result.startsWith(DisplayFormatter.BOLD));
        assertTrue(result.endsWith(DisplayFormatter.RESET));
    }

    @Test
    @Order(6)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies success alias")
    @Tag("binn0049")
    @Tag("core")
    void tc006_applyAnsiStyleShouldApplySuccessAlias() {
        assertTrue(DisplayFormatter.applyAnsiStyle("Done", "success")
                .startsWith(DisplayFormatter.GREEN));
    }

    @Test
    @Order(7)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies error alias")
    @Tag("binn0049")
    @Tag("core")
    void tc007_applyAnsiStyleShouldApplyErrorAlias() {
        assertTrue(DisplayFormatter.applyAnsiStyle("Failed", "error")
                .startsWith(DisplayFormatter.RED));
    }

    @Test
    @Order(8)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies warn alias")
    @Tag("binn0049")
    @Tag("core")
    void tc008_applyAnsiStyleShouldApplyWarnAlias() {
        assertTrue(DisplayFormatter.applyAnsiStyle("Careful", "warn")
                .startsWith(DisplayFormatter.YELLOW));
    }

    @Test
    @Order(9)
    @DisplayName("DisplayFormatter - applyAnsiStyle applies info alias")
    @Tag("binn0049")
    @Tag("core")
    void tc009_applyAnsiStyleShouldApplyInfoAlias() {
        assertTrue(DisplayFormatter.applyAnsiStyle("Details", "info")
                .startsWith(DisplayFormatter.CYAN));
    }

    @Test
    @Order(10)
    @DisplayName("DisplayFormatter - applyAnsiStyle handles uppercase style")
    @Tag("binn0049")
    @Tag("core")
    void tc010_applyAnsiStyleShouldHandleUppercaseStyle() {
        assertTrue(DisplayFormatter.applyAnsiStyle("Success", "GREEN")
                .startsWith(DisplayFormatter.GREEN));
    }

    @Test
    @Order(11)
    @DisplayName("DisplayFormatter - applyAnsiStyle returns plain text for unknown style")
    @Tag("binn0049")
    @Tag("core")
    void tc011_applyAnsiStyleShouldReturnPlainTextForUnknownStyle() {
        assertEquals("Plain", DisplayFormatter.applyAnsiStyle("Plain", "unknown"));
    }

    @Test
    @Order(12)
    @DisplayName("DisplayFormatter - applyAnsiStyle returns plain text for null style")
    @Tag("binn0049")
    @Tag("core")
    void tc012_applyAnsiStyleShouldReturnPlainTextForNullStyle() {
        assertEquals("Plain", DisplayFormatter.applyAnsiStyle("Plain", null));
    }

    @Test
    @Order(13)
    @DisplayName("DisplayFormatter - applyAnsiStyle returns empty string for null text")
    @Tag("binn0049")
    @Tag("core")
    void tc013_applyAnsiStyleShouldReturnEmptyStringForNullText() {
        assertEquals("", DisplayFormatter.applyAnsiStyle(null, "green"));
    }

    @Test
    @Order(14)
    @DisplayName("DisplayFormatter - renderTitle prints application title")
    @Tag("binn0049")
    @Tag("core")
    void tc014_renderTitleShouldPrintApplicationTitle() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;

        System.setOut(new PrintStream(output));
        DisplayFormatter.renderTitle();
        System.setOut(original);

        assertTrue(output.toString().contains("Student Timetable Optimiser"));
        assertTrue(output.toString().contains("Console Edition"));
    }

    @Test
    @Order(15)
    @DisplayName("DisplayFormatter - printDivider prints divider")
    @Tag("binn0049")
    @Tag("core")
    void tc015_printDividerShouldPrintDivider() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;

        System.setOut(new PrintStream(output));
        DisplayFormatter.printDivider();
        System.setOut(original);

        assertTrue(output.toString().contains("═"));
    }

    @Test
    @Order(16)
    @DisplayName("DisplayFormatter - printSeparator prints separator")
    @Tag("binn0049")
    @Tag("core")
    void tc016_printSeparatorShouldPrintSeparator() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;

        System.setOut(new PrintStream(output));
        DisplayFormatter.printSeparator();
        System.setOut(original);

        assertTrue(output.toString().contains("─"));
    }

    @Test
    @Order(17)
    @DisplayName("DisplayFormatter - printSectionHeader prints supplied title")
    @Tag("binn0049")
    @Tag("core")
    void tc017_printSectionHeaderShouldPrintTitle() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;

        System.setOut(new PrintStream(output));
        DisplayFormatter.printSectionHeader("Main Menu");
        System.setOut(original);

        assertTrue(output.toString().contains("Main Menu"));
        assertTrue(output.toString().contains("▶"));
    }

    @Test
    @Order(18)
    @DisplayName("DisplayFormatter - renderTimetable renders empty timetable header")
    @Tag("binn0049")
    @Tag("core")
    void tc018_renderTimetableShouldRenderEmptyHeader() {
        Timetable timetable = timetable();

        String result = DisplayFormatter.renderTimetable(timetable);

        assertTrue(result.contains("My Timetable"));
        assertTrue(result.contains("TT001"));
        assertTrue(result.contains("S1"));
        assertTrue(result.contains("Not allowed"));
    }

    @Test
    @Order(19)
    @DisplayName("DisplayFormatter - renderTimetable displays lecture overlap allowed")
    @Tag("binn0049")
    @Tag("core")
    void tc019_renderTimetableShouldDisplayLectureOverlapAllowed() {
        Timetable timetable = new Timetable("Allowed Table", "TT002", "S2", true);

        String result = DisplayFormatter.renderTimetable(timetable);

        assertTrue(result.contains("Allowed"));
    }

    @Test
    @Order(20)
    @DisplayName("DisplayFormatter - renderTimetable renders preferences")
    @Tag("binn0049")
    @Tag("core")
    void tc020_renderTimetableShouldRenderPreferences() {
        Timetable timetable = timetable();
        timetable.addPreference(new Preference(Preference.TYPE_NO_MORNINGS, 1));
        timetable.addPreference(new Preference(Preference.TYPE_NO_FRIDAYS, 2));

        String result = DisplayFormatter.renderTimetable(timetable);

        assertTrue(result.contains("Preferences"));
        assertTrue(result.contains("No Mornings"));
        assertTrue(result.contains("No Fridays"));
    }

    @Test
    @Order(21)
    @DisplayName("DisplayFormatter - renderTimetable renders one topic entry")
    @Tag("binn0049")
    @Tag("core")
    void tc021_renderTimetableShouldRenderOneTopicEntry() {
        Timetable timetable = timetable();
        TimetableEntry entry = new TimetableEntry(topic("COMP1001", "Programming"));
        entry.addChosenOption(option("Lecture", 1,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Tonsley",
                "Room 1"));

        timetable.addEntry(entry);

        String result = DisplayFormatter.renderTimetable(timetable);

        assertTrue(result.contains("COMP1001"));
        assertTrue(result.contains("Programming"));
        assertTrue(result.contains("Lecture"));
        assertTrue(result.contains("Room 1"));
    }

    @Test
    @Order(22)
    @DisplayName("DisplayFormatter - renderTimetable renders multiple chosen options")
    @Tag("binn0049")
    @Tag("core")
    void tc022_renderTimetableShouldRenderMultipleOptions() {
        Timetable timetable = timetable();
        TimetableEntry entry = new TimetableEntry(topic("COMP1001", "Programming"));

        entry.addChosenOption(option("Lecture", 1,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Tonsley",
                "Room 1"));

        entry.addChosenOption(option("Tutorial", 2,
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                "Tonsley",
                "Room 2"));

        timetable.addEntry(entry);

        String result = DisplayFormatter.renderTimetable(timetable);

        assertTrue(result.contains("Lecture"));
        assertTrue(result.contains("Tutorial"));
        assertTrue(result.contains("Room 1"));
        assertTrue(result.contains("Room 2"));
    }

    @Test
    @Order(23)
    @DisplayName("DisplayFormatter - renderTimetable displays clash warning")
    @Tag("binn0049")
    @Tag("core")
    void tc023_renderTimetableShouldDisplayClashWarning() {
        Timetable timetable = timetable();

        TimetableEntry entry1 = new TimetableEntry(topic("COMP1001", "Programming"));
        entry1.addChosenOption(option("Tutorial", 1,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Tonsley",
                "Room 1"));

        TimetableEntry entry2 = new TimetableEntry(topic("COMP2001", "Database"));
        entry2.addChosenOption(option("Workshop", 1,
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                "Tonsley",
                "Room 2"));

        timetable.addEntry(entry1);
        timetable.addEntry(entry2);

        String result = DisplayFormatter.renderTimetable(timetable);

        assertTrue(result.contains("Warnings"));
        assertTrue(result.contains("clash"));
        assertTrue(result.contains("CLASH"));
    }

    @Test
    @Order(24)
    @DisplayName("DisplayFormatter - renderTimetable displays commute warning")
    @Tag("binn0049")
    @Tag("core")
    void tc024_renderTimetableShouldDisplayCommuteWarning() {
        Timetable timetable = timetable();

        TimetableEntry entry1 = new TimetableEntry(topic("COMP1001", "Programming"));
        entry1.addChosenOption(option("Tutorial", 1,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "Tonsley",
                "Room 1"));

        TimetableEntry entry2 = new TimetableEntry(topic("COMP2001", "Database"));
        entry2.addChosenOption(option("Workshop", 1,
                LocalTime.of(10, 10),
                LocalTime.of(11, 0),
                "Bedford Park",
                "Room 2"));

        timetable.addEntry(entry1);
        timetable.addEntry(entry2);

        String result = DisplayFormatter.renderTimetable(timetable);

        assertTrue(result.contains("Warnings"));
        assertTrue(result.contains("commute"));
        assertTrue(result.contains("COMMUTE"));
    }

    @Test
    @Order(25)
    @DisplayName("DisplayFormatter - renderTimetable truncates long building names")
    @Tag("binn0049")
    @Tag("core")
    void tc025_renderTimetableShouldTruncateLongBuildingName() {
        Timetable timetable = timetable();
        TimetableEntry entry = new TimetableEntry(topic("COMP1001", "Programming"));

        entry.addChosenOption(option("Lecture", 1,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "This Building Name Is Definitely Too Long",
                "Room 1"));

        timetable.addEntry(entry);

        String result = DisplayFormatter.renderTimetable(timetable);

        assertTrue(result.contains("…"));
    }
}

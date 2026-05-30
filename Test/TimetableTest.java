import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Timetable Tests")
//can we add topics to a timetable and find them again? Does the program warn us when two classes clash?
class TimetableTest {

    private static Location bedfordA101;

    private Timetable timetable;
    private Topic     topicComp;

    @BeforeAll
    static void setUpSuite() {
        bedfordA101 = new Location("Bedford Park", "101");
    }

    @BeforeEach
    void setUpTimetable() {
        timetable = new Timetable("My Timetable", "TT-99", "S1", false);
        topicComp = Topic.parse("COMP1102 Programming Fundamentals");
    }

    private ClassSession buildSession(DayOfWeek day, String start, String end, Location location) {
        return new ClassSession(
                LocalDate.of(2026, 2, 23),
                LocalDate.of(2026, 6,  1),
                day,
                LocalTime.parse(start),
                LocalTime.parse(end),
                location);
    }

    private ClassOption buildOption(String className, int instance,
                                    DayOfWeek day, String start, String end,
                                    Location location) {
        ClassOffering offering = new ClassOffering(className, instance);
        ClassSession   session  = buildSession(day, start, end, location);
        return new ClassOption(offering, session);
    }

    @Test
    @Order(37)
    @DisplayName("TC-37: Timetable.generateAutoName — returns Timetable_N format")
    @Tag("student1")
    @Tag("core")
    void tc37_generateAutoName_returnsCorrectFormat() {
        assertAll("auto name format",
                () -> assertEquals("Timetable_1",  Timetable.generateAutoName(1)),
                () -> assertEquals("Timetable_10", Timetable.generateAutoName(10)),
                () -> assertEquals("Timetable_0",  Timetable.generateAutoName(0))
        );
    }

    @Test
    @Order(38)
    @DisplayName("TC-38: Timetable.addEntry — entry is retrievable by topic")
    @Tag("student1")
    @Tag("critical")
    void tc38_addEntry_retrievableByTopic() {
        TimetableEntry entry = new TimetableEntry(topicComp);
        timetable.addEntry(entry);
        assertNotNull(timetable.findEntryForTopic(topicComp));
    }

    @Test
    @Order(39)
    @DisplayName("TC-39: Timetable.addEntry — null entry throws NullPointerException")
    @Tag("student1")
    @Tag("core")
    void tc39_addEntry_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> timetable.addEntry(null));
    }

    @Test
    @Order(40)
    @DisplayName("TC-40: Timetable.resolveSpreadVsCompact — higher priority preference wins")
    @Tag("student2")
    @Tag("critical")
    void tc40_resolveSpreadVsCompact_higherPriorityWins() {
        timetable.addPreference(new Preference("Evenly Spread", 2));
        timetable.addPreference(new Preference("Compact",       1));

        Preference winner = timetable.resolveSpreadVsCompact();

        assertAll("compact wins because priority 1 < 2",
                () -> assertNotNull(winner),
                () -> assertTrue(winner.isCompact())
        );
    }

    @Test
    @Order(41)
    @DisplayName("TC-41: Timetable.recomputeWarnings — clash detected between overlapping entries")
    @Tag("student2")
    @Tag("critical")
    void tc41_recomputeWarnings_overlappingEntries_clashFlagged() {
        TimetableEntry entryA = new TimetableEntry(topicComp);
        entryA.addChosenOption(buildOption("Lecture", 1,
                DayOfWeek.MONDAY, "09:00", "11:00", bedfordA101));

        Topic topicNurs = Topic.parse("NURS1001 Nursing");
        TimetableEntry entryB = new TimetableEntry(topicNurs);
        entryB.addChosenOption(buildOption("Tutorial", 1,
                DayOfWeek.MONDAY, "10:00", "12:00", bedfordA101));

        timetable.addEntry(entryA);
        timetable.addEntry(entryB);
        timetable.recomputeWarnings();

        assertAll("clash warning set on both entries",
                () -> assertTrue(entryA.isClashWarning()),
                () -> assertTrue(entryB.isClashWarning())
        );
    }
}

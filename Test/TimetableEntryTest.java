import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("TimetableEntry Tests")
class TimetableEntryTest {

    private static Location bedford;
    private Topic          topic;
    private TimetableEntry entry;
    private ClassOption    option1;
    private ClassOption    option2;

    @BeforeAll
    static void setUpSuite() {
        bedford = new Location("Bedford Park", "101");
    }

    @BeforeEach
    void setUp() {
        topic  = Topic.parse("COMP1102 Programming Fundamentals");
        entry  = new TimetableEntry(topic);

        ClassSession s1 = new ClassSession(
                LocalDate.of(2026, 2, 23), LocalDate.of(2026, 6, 1),
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), bedford);
        ClassSession s2 = new ClassSession(
                LocalDate.of(2026, 2, 23), LocalDate.of(2026, 6, 1),
                DayOfWeek.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(14, 0), bedford);

        option1 = new ClassOption(new ClassOffering("Lecture",  1), s1);
        option2 = new ClassOption(new ClassOffering("Tutorial", 1), s2);
    }

    @Test
    @Order(1)
    @DisplayName("TC-TE-01: Constructor — null topic throws NullPointerException")
    void tc_te_01_nullTopic_throws() {
        assertThrows(NullPointerException.class, () -> new TimetableEntry(null));
    }

    @Test
    @Order(2)
    @DisplayName("TC-TE-02: getTopic — returns correct topic")
    void tc_te_02_getTopic() {
        assertEquals(topic, entry.getTopic());
    }

    @Test
    @Order(3)
    @DisplayName("TC-TE-03: Initial state — no options, no warnings")
    void tc_te_03_initialState() {
        assertAll("initial state",
                () -> assertTrue(entry.getChosenOptions().isEmpty()),
                () -> assertFalse(entry.isClashWarning()),
                () -> assertFalse(entry.isCommuteWarning())
        );
    }

    @Test
    @Order(4)
    @DisplayName("TC-TE-04: addChosenOption — option is added and retrievable")
    void tc_te_04_addChosenOption() {
        entry.addChosenOption(option1);
        assertAll("after add",
                () -> assertEquals(1, entry.getChosenOptions().size()),
                () -> assertTrue(entry.getChosenOptions().contains(option1))
        );
    }

    @Test
    @Order(5)
    @DisplayName("TC-TE-05: addChosenOption — null throws NullPointerException")
    void tc_te_05_addNull_throws() {
        assertThrows(NullPointerException.class, () -> entry.addChosenOption(null));
    }

    @Test
    @Order(6)
    @DisplayName("TC-TE-06: addChosenOption — multiple options added correctly")
    void tc_te_06_addMultipleOptions() {
        entry.addChosenOption(option1);
        entry.addChosenOption(option2);
        assertEquals(2, entry.getChosenOptions().size());
    }

    @Test
    @Order(7)
    @DisplayName("TC-TE-07: removeChosenOption — returns true and removes option")
    void tc_te_07_removeChosenOption_exists() {
        entry.addChosenOption(option1);
        boolean removed = entry.removeChosenOption(option1);
        assertAll("after remove",
                () -> assertTrue(removed),
                () -> assertTrue(entry.getChosenOptions().isEmpty())
        );
    }

    @Test
    @Order(8)
    @DisplayName("TC-TE-08: removeChosenOption — returns false when option not present")
    void tc_te_08_removeChosenOption_notPresent() {
        assertFalse(entry.removeChosenOption(option1));
    }

    @Test
    @Order(9)
    @DisplayName("TC-TE-09: setClashWarning — updates isClashWarning correctly")
    void tc_te_09_setClashWarning() {
        entry.setClashWarning(true);
        assertTrue(entry.isClashWarning());
        entry.setClashWarning(false);
        assertFalse(entry.isClashWarning());
    }

    @Test
    @Order(10)
    @DisplayName("TC-TE-10: setCommuteWarning — updates isCommuteWarning correctly")
    void tc_te_10_setCommuteWarning() {
        entry.setCommuteWarning(true);
        assertTrue(entry.isCommuteWarning());
        entry.setCommuteWarning(false);
        assertFalse(entry.isCommuteWarning());
    }

    @Test
    @Order(11)
    @DisplayName("TC-TE-11: getAllSessions — returns sessions from all chosen options")
    void tc_te_11_getAllSessions() {
        entry.addChosenOption(option1);
        entry.addChosenOption(option2);
        List<ClassSession> sessions = entry.getAllSessions();
        assertAll("sessions list",
                () -> assertEquals(2, sessions.size()),
                () -> assertTrue(sessions.contains(option1.getClassSession())),
                () -> assertTrue(sessions.contains(option2.getClassSession()))
        );
    }

    @Test
    @Order(12)
    @DisplayName("TC-TE-12: getAllSessions — returns empty list when no options added")
    void tc_te_12_getAllSessions_empty() {
        assertTrue(entry.getAllSessions().isEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("TC-TE-13: equals — same topic and options returns true")
    void tc_te_13_equals_same() {
        TimetableEntry other = new TimetableEntry(topic);
        entry.addChosenOption(option1);
        other.addChosenOption(option1);
        assertEquals(entry, other);
    }

    @Test
    @Order(14)
    @DisplayName("TC-TE-14: toString — contains topic code and option count")
    void tc_te_14_toString() {
        entry.addChosenOption(option1);
        String str = entry.toString();
        assertAll("toString",
                () -> assertTrue(str.contains("COMP1102")),
                () -> assertTrue(str.contains("1"))
        );
    }
}

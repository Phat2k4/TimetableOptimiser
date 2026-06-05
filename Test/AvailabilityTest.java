import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Availability Tests")
class AvailabilityTest {

    private static Location bedford;
    private Availability availability;
    private ClassOption  option1;
    private ClassOption  option2;

    @BeforeAll
    static void setUpSuite() {
        bedford = new Location("Bedford Park", "101");
    }

    @BeforeEach
    void setUp() {
        availability = new Availability("On Campus", "City", "S1", "1");

        ClassSession s1 = new ClassSession(
                LocalDate.of(2026, 2, 23), LocalDate.of(2026, 6, 1),
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), bedford);
        ClassSession s2 = new ClassSession(
                LocalDate.of(2026, 2, 23), LocalDate.of(2026, 6, 1),
                DayOfWeek.TUESDAY, LocalTime.of(13, 0), LocalTime.of(14, 0), bedford);

        option1 = new ClassOption(new ClassOffering("Lecture",  1), s1);
        option2 = new ClassOption(new ClassOffering("Tutorial", 1), s2);
    }

    @Test
    @Order(1)
    @DisplayName("TC-AV-01: Constructor — getters return correct values")
    void tc_av_01_constructorGetters() {
        assertAll("getters",
                () -> assertEquals("On Campus", availability.getAttendanceMode()),
                () -> assertEquals("City",      availability.getCampusLocation()),
                () -> assertEquals("S1",        availability.getSemesterN()),
                () -> assertEquals("1",         availability.getAvailabilityNumber())
        );
    }

    @Test
    @Order(2)
    @DisplayName("TC-AV-02: Constructor — null field throws NullPointerException")
    void tc_av_02_nullField_throws() {
        assertAll("null fields",
                () -> assertThrows(NullPointerException.class,
                        () -> new Availability(null, "City", "S1", "1")),
                () -> assertThrows(NullPointerException.class,
                        () -> new Availability("On Campus", null, "S1", "1")),
                () -> assertThrows(NullPointerException.class,
                        () -> new Availability("On Campus", "City", null, "1")),
                () -> assertThrows(NullPointerException.class,
                        () -> new Availability("On Campus", "City", "S1", null))
        );
    }

    @Test
    @Order(3)
    @DisplayName("TC-AV-03: parse — valid string splits correctly")
    void tc_av_03_parse_valid() {
        Availability a = Availability.parse("On Campus - City - S1 - 1");
        assertAll("parsed fields",
                () -> assertEquals("On Campus", a.getAttendanceMode()),
                () -> assertEquals("City",      a.getCampusLocation()),
                () -> assertEquals("S1",        a.getSemesterN()),
                () -> assertEquals("1",         a.getAvailabilityNumber())
        );
    }

    @Test
    @Order(4)
    @DisplayName("TC-AV-04: parse — partial string fills missing fields as empty")
    void tc_av_04_parse_partial() {
        Availability a = Availability.parse("On Campus - City");
        assertAll("partial parse",
                () -> assertEquals("On Campus", a.getAttendanceMode()),
                () -> assertEquals("City",      a.getCampusLocation()),
                () -> assertEquals("",          a.getSemesterN()),
                () -> assertEquals("",          a.getAvailabilityNumber())
        );
    }

    @Test
    @Order(5)
    @DisplayName("TC-AV-05: parse — null input throws NullPointerException")
    void tc_av_05_parse_null_throws() {
        assertThrows(NullPointerException.class, () -> Availability.parse(null));
    }

    @Test
    @Order(6)
    @DisplayName("TC-AV-06: addClassOption — option is added and retrievable")
    void tc_av_06_addClassOption() {
        availability.addClassOption(option1);
        assertAll("after add",
                () -> assertEquals(1, availability.getClassOptions().size()),
                () -> assertTrue(availability.getClassOptions().contains(option1))
        );
    }

    @Test
    @Order(7)
    @DisplayName("TC-AV-07: addClassOption — null throws NullPointerException")
    void tc_av_07_addNull_throws() {
        assertThrows(NullPointerException.class, () -> availability.addClassOption(null));
    }

    @Test
    @Order(8)
    @DisplayName("TC-AV-08: removeClassOption — returns true and removes option")
    void tc_av_08_remove_exists() {
        availability.addClassOption(option1);
        boolean removed = availability.removeClassOption(option1);
        assertAll("after remove",
                () -> assertTrue(removed),
                () -> assertTrue(availability.getClassOptions().isEmpty())
        );
    }

    @Test
    @Order(9)
    @DisplayName("TC-AV-09: removeClassOption — returns false when not present")
    void tc_av_09_remove_notPresent() {
        assertFalse(availability.removeClassOption(option1));
    }

    @Test
    @Order(10)
    @DisplayName("TC-AV-10: replaceClassOption — replaces correctly and returns true")
    void tc_av_10_replace_exists() {
        availability.addClassOption(option1);
        boolean replaced = availability.replaceClassOption(option1, option2);
        assertAll("after replace",
                () -> assertTrue(replaced),
                () -> assertTrue(availability.getClassOptions().contains(option2)),
                () -> assertFalse(availability.getClassOptions().contains(option1))
        );
    }

    @Test
    @Order(11)
    @DisplayName("TC-AV-11: replaceClassOption — returns false when old option not present")
    void tc_av_11_replace_notPresent() {
        assertFalse(availability.replaceClassOption(option1, option2));
    }

    @Test
    @Order(12)
    @DisplayName("TC-AV-12: equals — same fields returns true (case-insensitive)")
    void tc_av_12_equals_caseInsensitive() {
        Availability other = new Availability("ON CAMPUS", "city", "s1", "1");
        assertEquals(availability, other);
    }

    @Test
    @Order(13)
    @DisplayName("TC-AV-13: equals — different semester returns false")
    void tc_av_13_equals_differentSemester() {
        Availability other = new Availability("On Campus", "City", "S2", "1");
        assertNotEquals(availability, other);
    }

    @Test
    @Order(14)
    @DisplayName("TC-AV-14: toString — returns correct format")
    void tc_av_14_toString() {
        assertEquals("On Campus - City - S1 - 1", availability.toString());
    }
}

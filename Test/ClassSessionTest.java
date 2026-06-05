import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ClassSession Tests")
class ClassSessionTest {

    private static Location bedfordA;
    private static Location bedfordB;
    private static Location cityTower;

    private static final LocalDate FIRST = LocalDate.of(2026, 2, 23);
    private static final LocalDate LAST  = LocalDate.of(2026, 6,  1);

    @BeforeAll
    static void setUpSuite() {
        bedfordA  = new Location("Bedford Park", "101");
        bedfordB  = new Location("Bedford Park", "102");
        cityTower = new Location("Flinders City Campus", "201");
    }

    private ClassSession session(DayOfWeek day, String start, String end, Location loc) {
        return new ClassSession(FIRST, LAST, day, LocalTime.parse(start), LocalTime.parse(end), loc);
    }

    @Test
    @Order(1)
    @DisplayName("TC-CS-01: getDay — returns correct day")
    void tc_cs_01_getDay() {
        assertEquals(DayOfWeek.MONDAY, session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA).getDay());
    }

    @Test
    @Order(2)
    @DisplayName("TC-CS-02: getStartTime and getEndTime — return correct times")
    void tc_cs_02_getTimes() {
        ClassSession s = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        assertAll("times",
                () -> assertEquals(LocalTime.of(9,  0), s.getStartTime()),
                () -> assertEquals(LocalTime.of(11, 0), s.getEndTime())
        );
    }

    @Test
    @Order(3)
    @DisplayName("TC-CS-03: getLocation — returns correct location")
    void tc_cs_03_getLocation() {
        ClassSession s = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        assertEquals(bedfordA, s.getLocation());
    }

    @Test
    @Order(4)
    @DisplayName("TC-CS-04: getDateOfFirstClass and getDateOfLastClass — correct dates")
    void tc_cs_04_getDates() {
        ClassSession s = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        assertAll("dates",
                () -> assertEquals(FIRST, s.getDateOfFirstClass()),
                () -> assertEquals(LAST,  s.getDateOfLastClass())
        );
    }

    @Test
    @Order(5)
    @DisplayName("TC-CS-05: isMorning — returns true for 09:00 start")
    void tc_cs_05_isMorning_true() {
        assertTrue(session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA).isMorning());
    }

    @Test
    @Order(6)
    @DisplayName("TC-CS-06: isMorning — returns false for 13:00 start")
    void tc_cs_06_isMorning_false() {
        assertFalse(session(DayOfWeek.MONDAY, "13:00", "15:00", bedfordA).isMorning());
    }

    @Test
    @Order(7)
    @DisplayName("TC-CS-07: isAfternoon — returns true for 13:00 start")
    void tc_cs_07_isAfternoon_true() {
        assertTrue(session(DayOfWeek.MONDAY, "13:00", "15:00", bedfordA).isAfternoon());
    }

    @Test
    @Order(8)
    @DisplayName("TC-CS-08: isAfternoon — returns false for 09:00 start")
    void tc_cs_08_isAfternoon_false() {
        assertFalse(session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA).isAfternoon());
    }

    @Test
    @Order(9)
    @DisplayName("TC-CS-09: clashesWith — overlapping same campus returns true")
    void tc_cs_09_clashesWith_overlap_sameCampus() {
        ClassSession a = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        ClassSession b = session(DayOfWeek.MONDAY, "10:00", "12:00", bedfordB);
        assertTrue(a.clashesWith(b));
    }

    @Test
    @Order(10)
    @DisplayName("TC-CS-10: clashesWith — back-to-back same campus returns false")
    void tc_cs_10_clashesWith_backToBack_sameCampus() {
        ClassSession a = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        ClassSession b = session(DayOfWeek.MONDAY, "11:00", "12:00", bedfordB);
        assertFalse(a.clashesWith(b));
    }

    @Test
    @Order(11)
    @DisplayName("TC-CS-11: clashesWith — different days returns false")
    void tc_cs_11_clashesWith_differentDays() {
        ClassSession a = session(DayOfWeek.MONDAY,  "09:00", "11:00", bedfordA);
        ClassSession b = session(DayOfWeek.TUESDAY, "09:00", "11:00", bedfordA);
        assertFalse(a.clashesWith(b));
    }

    @Test
    @Order(12)
    @DisplayName("TC-CS-12: clashesWith — null other returns false")
    void tc_cs_12_clashesWith_null() {
        ClassSession a = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        assertFalse(a.clashesWith(null));
    }

    @Test
    @Order(13)
    @DisplayName("TC-CS-13: clashesWith — different campus gap < 30 min returns true")
    void tc_cs_13_clashesWith_differentCampus_shortGap() {
        ClassSession a = session(DayOfWeek.MONDAY, "09:00", "10:00", bedfordA);
        ClassSession b = session(DayOfWeek.MONDAY, "10:20", "11:00", cityTower);
        assertTrue(a.clashesWith(b));
    }

    @Test
    @Order(14)
    @DisplayName("TC-CS-14: clashesWith — different campus gap exactly 30 min returns false")
    void tc_cs_14_clashesWith_differentCampus_exactly30() {
        ClassSession a = session(DayOfWeek.MONDAY, "09:00", "10:00", bedfordA);
        ClassSession b = session(DayOfWeek.MONDAY, "10:30", "11:00", cityTower);
        assertFalse(a.clashesWith(b));
    }

    @Test
    @Order(15)
    @DisplayName("TC-CS-15: clashesWith — lecture overlap allowed when flag is true")
    void tc_cs_15_clashesWith_lectureOverlapAllowed() {
        ClassSession a = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        ClassSession b = session(DayOfWeek.MONDAY, "10:00", "12:00", bedfordB);
        assertFalse(a.clashesWith(b, true, true, true));
    }

    @Test
    @Order(16)
    @DisplayName("TC-CS-16: equals — same values returns true")
    void tc_cs_16_equals_same() {
        ClassSession a = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        ClassSession b = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        assertEquals(a, b);
    }

    @Test
    @Order(17)
    @DisplayName("TC-CS-17: toString — contains day and time info")
    void tc_cs_17_toString() {
        ClassSession s = session(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA);
        String str = s.toString();
        assertAll("toString contains key info",
                () -> assertTrue(str.contains("MONDAY")),
                () -> assertTrue(str.contains("09:00")),
                () -> assertTrue(str.contains("11:00"))
        );
    }
}

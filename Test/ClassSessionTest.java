package model;

import org.junit.jupiter.api.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ClassSession Tests")
class ClassSessionTest {

    private final LocalDate first = LocalDate.of(2026, 3, 1);
    private final LocalDate last = LocalDate.of(2026, 6, 1);

    private ClassSession session(LocalTime start, LocalTime end, String building) {
        Location location = new Location(building, "Room 1");

        return new ClassSession(
                first,
                last,
                DayOfWeek.MONDAY,
                start,
                end,
                location
        );
    }

    private ClassSession sessionOnDay(DayOfWeek day, LocalTime start, LocalTime end, String building) {
        Location location = new Location(building, "Room 1");

        return new ClassSession(
                first,
                last,
                day,
                start,
                end,
                location
        );
    }

    @Test
    @Order(200)
    @DisplayName(" ClassSession — constructor stores all values")
    @Tag("binn0049")
    @Tag("critical")
    void constructorShouldStoreAllValues() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        assertAll("stored values",
                () -> assertEquals(first, s1.getDateOfFirstClass()),
                () -> assertEquals(last, s1.getDateOfLastClass()),
                () -> assertEquals(DayOfWeek.MONDAY, s1.getDay()),
                () -> assertEquals(LocalTime.of(9, 0), s1.getStartTime()),
                () -> assertEquals(LocalTime.of(10, 0), s1.getEndTime()),
                () -> assertEquals(new Location("Tonsley", "Room 1"), s1.getLocation())
        );
    }

    @Test
    @Order(201)
    @DisplayName(" ClassSession — constructor rejects null first date")
    @Tag("binn0049")
    @Tag("core")
    void tc201_constructorShouldRejectNullFirstDate() {
        assertThrows(NullPointerException.class,
                () -> new ClassSession(
                        null,
                        last,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        new Location("Tonsley", "Room 1")
                ));
    }

    @Test
    @Order(202)
    @DisplayName(" ClassSession — constructor rejects null last date")
    @Tag("binn0049")
    @Tag("core")
    void tc202_constructorShouldRejectNullLastDate() {
        assertThrows(NullPointerException.class,
                () -> new ClassSession(
                        first,
                        null,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        new Location("Tonsley", "Room 1")
                ));
    }

    @Test
    @Order(203)
    @DisplayName("ClassSession — constructor rejects null day")
    @Tag("binn0049")
    @Tag("core")
    void tc203_constructorShouldRejectNullDay() {
        assertThrows(NullPointerException.class,
                () -> new ClassSession(
                        first,
                        last,
                        null,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        new Location("Tonsley", "Room 1")
                ));
    }

    @Test
    @Order(204)
    @DisplayName("ClassSession — constructor rejects null start time")
    @Tag("binn0049")
    @Tag("core")
    void tc204_constructorShouldRejectNullStartTime() {
        assertThrows(NullPointerException.class,
                () -> new ClassSession(
                        first,
                        last,
                        DayOfWeek.MONDAY,
                        null,
                        LocalTime.of(10, 0),
                        new Location("Tonsley", "Room 1")
                ));
    }

    @Test
    @Order(205)
    @DisplayName("ClassSession — constructor rejects null end time")
    @Tag("binn0049")
    @Tag("core")
    void tc205_constructorShouldRejectNullEndTime() {
        assertThrows(NullPointerException.class,
                () -> new ClassSession(
                        first,
                        last,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        null,
                        new Location("Tonsley", "Room 1")
                ));
    }

    @Test
    @Order(206)
    @DisplayName("ClassSession — constructor rejects null location")
    @Tag("binn0049")
    @Tag("core")
    void tc206_constructorShouldRejectNullLocation() {
        assertThrows(NullPointerException.class,
                () -> new ClassSession(
                        first,
                        last,
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        null
                ));
    }

    @Test
    @Order(207)
    @DisplayName("ClassSession — null other session does not clash")
    @Tag("binn0049")
    @Tag("core")
    void tc207_nullOtherSessionShouldNotClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        assertFalse(s1.clashesWith(null));
    }

    @Test
    @Order(208)
    @DisplayName(" ClassSession — different day sessions do not clash")
    @Tag("binn0049")
    @Tag("critical")
    void tc208_differentDaySessionsShouldNotClash() {
        ClassSession s1 = sessionOnDay(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = sessionOnDay(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley");

        assertFalse(s1.clashesWith(s2));
    }

    @Test
    @Order(209)
    @DisplayName("ClassSession — same campus overlapping sessions clash")
    @Tag("binn0049")
    @Tag("critical")
    void tc209_sameCampusOverlappingSessionsShouldClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley");

        assertTrue(s1.clashesWith(s2));
    }

    @Test
    @Order(210)
    @DisplayName("ClassSession — same campus back-to-back sessions do not clash")
    @Tag("binn0049")
    @Tag("critical")
    void tc210_sameCampusBackToBackSessionsShouldNotClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(10, 0), LocalTime.of(11, 0), "Tonsley");

        assertFalse(s1.clashesWith(s2));
    }

    @Test
    @Order(211)
    @DisplayName("ClassSession — same campus one minute overlap clashes")
    @Tag("binn0049")
    @Tag("core")
    void tc211_sameCampusOneMinuteOverlapShouldClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 1), "Tonsley");
        ClassSession s2 = session(LocalTime.of(10, 0), LocalTime.of(11, 0), "Tonsley");

        assertTrue(s1.clashesWith(s2));
    }

    @Test
    @Order(212)
    @DisplayName("ClassSession — same campus separated sessions do not clash")
    @Tag("binn0049")
    @Tag("core")
    void tc212_sameCampusSeparatedSessionsShouldNotClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(10, 30), LocalTime.of(11, 30), "Tonsley");

        assertFalse(s1.clashesWith(s2));
    }

    @Test
    @Order(213)
    @DisplayName("ClassSession — different campus less than 30 minute gap clashes")
    @Tag("binn0049")
    @Tag("critical")
    void tc213_differentCampusLessThanThirtyMinuteGapShouldClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(10, 20), LocalTime.of(11, 0), "Bedford Park");

        assertTrue(s1.clashesWith(s2));
    }

    @Test
    @Order(214)
    @DisplayName("ClassSession — different campus 30 minute gap does not clash")
    @Tag("binn0049")
    @Tag("critical")
    void tc214_differentCampusThirtyMinuteGapShouldNotClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(10, 30), LocalTime.of(11, 30), "Bedford Park");

        assertFalse(s1.clashesWith(s2));
    }

    @Test
    @Order(215)
    @DisplayName("ClassSession — different campus more than 30 minute gap does not clash")
    @Tag("binn0049")
    @Tag("core")
    void tc215_differentCampusMoreThanThirtyMinuteGapShouldNotClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(10, 45), LocalTime.of(11, 30), "Bedford Park");

        assertFalse(s1.clashesWith(s2));
    }

    @Test
    @Order(216)
    @DisplayName(" ClassSession — different campus overlapping sessions clash")
    @Tag("binn0049")
    @Tag("critical")
    void tc216_differentCampusOverlappingSessionsShouldClash() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(11, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(10, 0), LocalTime.of(12, 0), "Bedford Park");

        assertTrue(s1.clashesWith(s2));
    }

    @Test
    @Order(217)
    @DisplayName("ClassSession — lecture overlap is allowed when flag is true")
    @Tag("binn0049")
    @Tag("critical")
    void tc217_lectureOverlapShouldBeAllowedWhenFlagIsTrue() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley");

        assertFalse(s1.clashesWith(s2, true, true, true));
    }

    @Test
    @Order(218)
    @DisplayName("ClassSession — lecture overlap is not allowed when only one is lecture")
    @Tag("binn0049")
    @Tag("core")
    void tc218_lectureOverlapShouldNotBeAllowedWhenOnlyOneIsLecture() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley");

        assertTrue(s1.clashesWith(s2, true, true, false));
    }

    @Test
    @Order(219)
    @DisplayName("ClassSession — morning session returns true")
    @Tag("binn0049")
    @Tag("critical")
    void tc219_morningSessionShouldReturnTrue() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        assertTrue(s1.isMorning());
        assertFalse(s1.isAfternoon());
    }

    @Test
    @Order(220)
    @DisplayName("ClassSession — afternoon session returns true at noon")
    @Tag("binn0049")
    @Tag("critical")
    void tc220_afternoonSessionShouldReturnTrueAtNoon() {
        ClassSession s1 = session(LocalTime.NOON, LocalTime.of(13, 0), "Tonsley");

        assertFalse(s1.isMorning());
        assertTrue(s1.isAfternoon());
    }

    @Test
    @Order(221)
    @DisplayName("ClassSession — equals returns true for same object")
    @Tag("binn0049")
    @Tag("core")
    void tc221_equalsShouldReturnTrueForSameObject() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        assertEquals(s1, s1);
    }

    @Test
    @Order(222)
    @DisplayName("ClassSession — equals and hashCode match for same values")
    @Tag("binn0049")
    @Tag("core")
    void tc222_equalsAndHashCodeShouldMatchForSameValues() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        assertAll("equals and hashCode",
                () -> assertEquals(s1, s2),
                () -> assertEquals(s1.hashCode(), s2.hashCode())
        );
    }

    @Test
    @Order(223)
    @DisplayName("ClassSession — equals returns false for different time")
    @Tag("binn0049")
    @Tag("core")
    void tc223_equalsShouldReturnFalseForDifferentTime() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(10, 0), LocalTime.of(11, 0), "Tonsley");

        assertNotEquals(s1, s2);
    }

    @Test
    @Order(224)
    @DisplayName(" ClassSession — equals returns false for null")
    @Tag("binn0049")
    @Tag("core")
    void tc224_equalsShouldReturnFalseForNull() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        assertNotEquals(null, s1);
    }

    @Test
    @Order(225)
    @DisplayName("ClassSession — equals returns false for different object type")
    @Tag("binn0049")
    @Tag("additional")
    void tc225_equalsShouldReturnFalseForDifferentObjectType() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        assertNotEquals("session", s1);
    }

    @Test
    @Order(226)
    @DisplayName(" ClassSession — toString contains main session details")
    @Tag("binn0049")
    @Tag("additional")
    void tc226_toStringShouldContainMainSessionDetails() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        String text = s1.toString();

        assertAll("toString output",
                () -> assertTrue(text.contains("MONDAY")),
                () -> assertTrue(text.contains("09:00")),
                () -> assertTrue(text.contains("10:00")),
                () -> assertTrue(text.contains("Tonsley")),
                () -> assertTrue(text.contains("2026-03-01")),
                () -> assertTrue(text.contains("2026-06-01"))
        );
    }

    @Test
    @Order(227)
    @DisplayName("ClassSession — different campus reverse order with less than 30 minute gap clashes")
    @Tag("binn0049")
    @Tag("core")
    void tc227_differentCampusReverseOrderLessThanThirtyMinuteGapShouldClash() {
        ClassSession s1 = session(LocalTime.of(10, 20), LocalTime.of(11, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Bedford Park");

        assertTrue(s1.clashesWith(s2));
    }

    @Test
    @Order(228)
    @DisplayName("ClassSession — different campus reverse order with 30 minute gap does not clash")
    @Tag("binn0049")
    @Tag("core")
    void tc228_differentCampusReverseOrderThirtyMinuteGapShouldNotClash() {
        ClassSession s1 = session(LocalTime.of(10, 30), LocalTime.of(11, 30), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Bedford Park");

        assertFalse(s1.clashesWith(s2));
    }

    @Test
    @Order(229)
    @DisplayName("ClassSession — lecture overlap is not allowed when flag is false")
    @Tag("binn0049")
    @Tag("core")
    void tc229_lectureOverlapShouldNotBeAllowedWhenFlagIsFalse() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley");

        assertTrue(s1.clashesWith(s2, false, true, true));
    }

    @Test
    @Order(230)
    @DisplayName("ClassSession — lecture overlap is not exempt when neither session is lecture")
    @Tag("binn0049")
    @Tag("core")
    void tc230_lectureOverlapShouldNotBeAllowedWhenNeitherIsLecture() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley");

        assertTrue(s1.clashesWith(s2, true, false, false));
    }

    @Test
    @Order(231)
    @DisplayName("ClassSession — equals returns false for different day")
    @Tag("binn0049")
    @Tag("core")
    void tc231_equalsShouldReturnFalseForDifferentDay() {
        ClassSession s1 = sessionOnDay(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = sessionOnDay(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        assertNotEquals(s1, s2);
    }

    @Test
    @Order(232)
    @DisplayName("ClassSession — equals returns false for different location")
    @Tag("binn0049")
    @Tag("core")
    void tc232_equalsShouldReturnFalseForDifferentLocation() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");
        ClassSession s2 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Bedford Park");

        assertNotEquals(s1, s2);
    }

    @Test
    @Order(233)
    @DisplayName("ClassSession — equals returns false for different first date")
    @Tag("binn0049")
    @Tag("core")
    void tc233_equalsShouldReturnFalseForDifferentFirstDate() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        ClassSession s2 = new ClassSession(
                LocalDate.of(2026, 3, 2),
                last,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                new Location("Tonsley", "Room 1")
        );

        assertNotEquals(s1, s2);
    }

    @Test
    @Order(234)
    @DisplayName(" ClassSession — equals returns false for different last date")
    @Tag("binn0049")
    @Tag("core")
    void tc234_equalsShouldReturnFalseForDifferentLastDate() {
        ClassSession s1 = session(LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley");

        ClassSession s2 = new ClassSession(
                first,
                LocalDate.of(2026, 6, 2),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                new Location("Tonsley", "Room 1")
        );

        assertNotEquals(s1, s2);
    }
}

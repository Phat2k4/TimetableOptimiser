import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ClashDetector Tests")
//It checks if two classes overlap in time.
// For example — if one class is 9am to 11am, and another is 10am to 12pm, they clash.
// We also check travel time between different campuses.
// If two classes are at different campuses with less than 30 minutes gap, that is a problem.
class ClashDetectorTest {

    private static Location bedfordA101;
    private static Location bedfordA102;
    private static Location cityTower201;
    private static Timetable ttNoOverlap;
    private static Timetable ttAllowOverlap;

    @BeforeAll
    static void setUpSuite() {
        bedfordA101    = new Location("Bedford Park", "101");
        bedfordA102    = new Location("Bedford Park", "102");
        cityTower201   = new Location("Flinders City Campus", "201");
        ttNoOverlap    = new Timetable("Test TT",    "TT-TEST-1", "S1", false);
        ttAllowOverlap = new Timetable("Test TT OL", "TT-TEST-2", "S1", true);
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

    // ── hasTimeClash ──────────────────────────────────────────────────────────

    @Test
    @Order(16)
    @DisplayName("TC-16: hasTimeClash — same day, overlapping sessions returns true")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc16_hasTimeClash_sameDay_overlapping_returnsTrue() {
        ClassSession a = buildSession(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.MONDAY, "10:00", "12:00", bedfordA102);
        assertTrue(ClashDetector.hasTimeClash(a, b));
    }

    @Test
    @Order(17)
    @DisplayName("TC-17: hasTimeClash — same day, back-to-back returns false")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc17_hasTimeClash_sameDay_backToBack_returnsFalse() {
        ClassSession a = buildSession(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.MONDAY, "11:00", "12:00", bedfordA102);
        assertFalse(ClashDetector.hasTimeClash(a, b));
    }

    @Test
    @Order(18)
    @DisplayName("TC-18: hasTimeClash — different days returns false")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc18_hasTimeClash_differentDays_returnsFalse() {
        ClassSession a = buildSession(DayOfWeek.MONDAY,  "09:00", "11:00", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.TUESDAY, "09:00", "11:00", bedfordA101);
        assertFalse(ClashDetector.hasTimeClash(a, b));
    }

    @Test
    @Order(19)
    @DisplayName("TC-19: hasTimeClash — exactly 1 minute overlap returns true (boundary)")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc19_hasTimeClash_exactlyOneMinuteOverlap_returnsTrue() {
        ClassSession a = buildSession(DayOfWeek.MONDAY, "09:00", "10:01", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.MONDAY, "10:00", "11:00", bedfordA102);
        assertTrue(ClashDetector.hasTimeClash(a, b));
    }

    @Test
    @Order(20)
    @DisplayName("TC-20: hasTimeClash — null session a returns false (null safety)")
    @Tag("Nguy1687")
    @Tag("core")
    void tc20_hasTimeClash_nullSessionA_returnsFalse() {
        ClassSession b = buildSession(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA101);
        assertFalse(ClashDetector.hasTimeClash(null, b));
    }

    @Test
    @Order(21)
    @DisplayName("TC-21: hasTimeClash — null session b returns false (null safety)")
    @Tag("Nguy1687")
    @Tag("core")
    void tc21_hasTimeClash_nullSessionB_returnsFalse() {
        ClassSession a = buildSession(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA101);
        assertFalse(ClashDetector.hasTimeClash(a, null));
    }

    @ParameterizedTest(name = "TC-22 [{index}]: {0}–{1} vs {2}–{3} on same day expect clash={4}")
    @Order(22)
    @DisplayName("TC-22: hasTimeClash — parameterised overlap boundaries")
    @Tag("Nguy1687")
    @Tag("core")
    @CsvSource({
            "09:00, 10:00, 10:00, 11:00, false",
            "09:00, 10:01, 10:00, 11:00, true",
            "09:00, 11:00, 10:00, 12:00, true",
            "09:00, 09:30, 10:00, 11:00, false",
            "09:00, 11:00, 09:00, 11:00, true"
    })
    void tc22_hasTimeClash_parameterised(String startA, String endA,
                                          String startB, String endB,
                                          boolean expected) {
        ClassSession a = buildSession(DayOfWeek.WEDNESDAY, startA, endA, bedfordA101);
        ClassSession b = buildSession(DayOfWeek.WEDNESDAY, startB, endB, bedfordA102);
        assertEquals(expected, ClashDetector.hasTimeClash(a, b));
    }

    // ── violatesCommuteGap ────────────────────────────────────────────────────

    @Test
    @Order(23)
    @DisplayName("TC-23: violatesCommuteGap — different campus, gap < 30 min returns true")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc23_violatesCommuteGap_differentCampus_gapUnder30_returnsTrue() {
        ClassSession a = buildSession(DayOfWeek.MONDAY, "09:00", "10:00", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.MONDAY, "10:20", "11:00", cityTower201);
        assertTrue(ClashDetector.violatesCommuteGap(a, b));
    }

    @Test
    @Order(24)
    @DisplayName("TC-24: violatesCommuteGap — different campus, gap exactly 30 min returns false (boundary)")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc24_violatesCommuteGap_differentCampus_gapExactly30_returnsFalse() {
        ClassSession a = buildSession(DayOfWeek.MONDAY, "09:00", "10:00", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.MONDAY, "10:30", "11:00", cityTower201);
        assertFalse(ClashDetector.violatesCommuteGap(a, b));
    }

    @Test
    @Order(25)
    @DisplayName("TC-25: violatesCommuteGap — same campus, back-to-back returns false")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc25_violatesCommuteGap_sameCampus_backToBack_returnsFalse() {
        ClassSession a = buildSession(DayOfWeek.MONDAY, "09:00", "10:00", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.MONDAY, "10:00", "11:00", bedfordA102);
        assertFalse(ClashDetector.violatesCommuteGap(a, b));
    }

    @Test
    @Order(26)
    @DisplayName("TC-26: violatesCommuteGap — different campus, gap > 30 min returns false")
    @Tag("Nguy1687")
    @Tag("core")
    void tc26_violatesCommuteGap_differentCampus_gapOver30_returnsFalse() {
        ClassSession a = buildSession(DayOfWeek.MONDAY, "09:00", "10:00", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.MONDAY, "10:45", "12:00", cityTower201);
        assertFalse(ClashDetector.violatesCommuteGap(a, b));
    }

    @Test
    @Order(27)
    @DisplayName("TC-27: violatesCommuteGap — different days returns false")
    @Tag("Nguy1687")
    @Tag("core")
    void tc27_violatesCommuteGap_differentDays_returnsFalse() {
        ClassSession a = buildSession(DayOfWeek.MONDAY,  "09:00", "10:00", bedfordA101);
        ClassSession b = buildSession(DayOfWeek.TUESDAY, "10:00", "11:00", cityTower201);
        assertFalse(ClashDetector.violatesCommuteGap(a, b));
    }

    @ParameterizedTest(name = "TC-28 [{index}]: endA={0} startB={1} expect violation={2}")
    @Order(28)
    @DisplayName("TC-28: violatesCommuteGap — parameterised gap boundaries")
    @Tag("Nguy1687")
    @Tag("core")
    @CsvSource({
            "10:00, 10:10, true",
            "10:00, 10:29, true",
            "10:00, 10:30, false",
            "10:00, 10:31, false",
            "10:00, 11:00, false"
    })
    void tc28_violatesCommuteGap_parameterised(String endA, String startB, boolean expected) {
        ClassSession a = buildSession(DayOfWeek.THURSDAY, "09:00", endA,    bedfordA101);
        ClassSession b = buildSession(DayOfWeek.THURSDAY, startB,  "12:00", cityTower201);
        assertEquals(expected, ClashDetector.violatesCommuteGap(a, b));
    }

    // ── isLectureExempt ───────────────────────────────────────────────────────

    private ClassSession sessionA;
    private ClassSession sessionB;

    @BeforeEach
    void buildSessions() {
        sessionA = buildSession(DayOfWeek.MONDAY, "09:00", "11:00", bedfordA101);
        sessionB = buildSession(DayOfWeek.MONDAY, "10:00", "12:00", bedfordA102);
    }

    @Test
    @Order(29)
    @DisplayName("TC-29: isLectureExempt — both lectures, overlap allowed returns true")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc29_isLectureExempt_bothLectures_overlapAllowed_returnsTrue() {
        assertTrue(ClashDetector.isLectureExempt(sessionA, sessionB, true, true, ttAllowOverlap));
    }

    @Test
    @Order(30)
    @DisplayName("TC-30: isLectureExempt — both lectures, overlap NOT allowed returns false")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc30_isLectureExempt_bothLectures_overlapNotAllowed_returnsFalse() {
        assertFalse(ClashDetector.isLectureExempt(sessionA, sessionB, true, true, ttNoOverlap));
    }

    @Test
    @Order(31)
    @DisplayName("TC-31: isLectureExempt — only one is lecture returns false")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc31_isLectureExempt_onlyOneLecture_returnsFalse() {
        assertAll("one lecture scenarios",
                () -> assertFalse(ClashDetector.isLectureExempt(sessionA, sessionB, true,  false, ttAllowOverlap)),
                () -> assertFalse(ClashDetector.isLectureExempt(sessionA, sessionB, false, true,  ttAllowOverlap))
        );
    }

    @Test
    @Order(32)
    @DisplayName("TC-32: isLectureExempt — null timetable returns false (null safety)")
    @Tag("Nguy1687")
    @Tag("core")
    void tc32_isLectureExempt_nullTimetable_returnsFalse() {
        assertFalse(ClashDetector.isLectureExempt(sessionA, sessionB, true, true, null));
    }

    // ── violatesHardConstraint ────────────────────────────────────────────────

    @Test
    @Order(33)
    @DisplayName("TC-33: violatesHardConstraint — same campus overlap returns true")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc33_violatesHardConstraint_sameCampusOverlap_returnsTrue() {
        ClassOption existing  = buildOption("Tutorial", 1, DayOfWeek.MONDAY, "09:00", "11:00", bedfordA101);
        ClassOption candidate = buildOption("Workshop", 1, DayOfWeek.MONDAY, "10:00", "12:00", bedfordA102);
        assertTrue(ClashDetector.violatesHardConstraint(existing, candidate, ttNoOverlap));
    }

    @Test
    @Order(34)
    @DisplayName("TC-34: violatesHardConstraint — different campus, gap < 30 returns true")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc34_violatesHardConstraint_differentCampusShortGap_returnsTrue() {
        ClassOption existing  = buildOption("Lecture",  1, DayOfWeek.MONDAY, "09:00", "10:00", bedfordA101);
        ClassOption candidate = buildOption("Tutorial", 1, DayOfWeek.MONDAY, "10:15", "11:15", cityTower201);
        assertTrue(ClashDetector.violatesHardConstraint(existing, candidate, ttNoOverlap));
    }

    @Test
    @Order(35)
    @DisplayName("TC-35: violatesHardConstraint — different days returns false")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc35_violatesHardConstraint_differentDays_returnsFalse() {
        ClassOption existing  = buildOption("Lecture",  1, DayOfWeek.MONDAY,  "09:00", "11:00", bedfordA101);
        ClassOption candidate = buildOption("Tutorial", 1, DayOfWeek.TUESDAY, "09:00", "11:00", bedfordA101);
        assertFalse(ClashDetector.violatesHardConstraint(existing, candidate, ttNoOverlap));
    }

    @Test
    @Order(36)
    @DisplayName("TC-36: violatesHardConstraint — both lectures with overlap allowed returns false")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc36_violatesHardConstraint_bothLectures_overlapAllowed_returnsFalse() {
        assumeTrue(ttAllowOverlap.isAllowLectureOverlap(),
                "Skipped: timetable does not have lecture overlap enabled");
        ClassOption existing  = buildOption("Lecture", 1, DayOfWeek.MONDAY, "09:00", "11:00", bedfordA101);
        ClassOption candidate = buildOption("Lecture", 2, DayOfWeek.MONDAY, "10:00", "12:00", bedfordA102);
        assertFalse(ClashDetector.violatesHardConstraint(existing, candidate, ttAllowOverlap));
    }
}

import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Scanner;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ClassDataService Tests")
// ClassDataService depends on Scanner (user input) and AppState (file I/O singleton).
// Tests here cover: constructor, static constants, FlatRecord, importFromCsv,
// browseAll/viewTopicCode/search early-exit paths, and data-traversal paths.
// Interactive methods (edit, delete, prompt helpers) require black-box testing.
class ClassDataServiceTest {

    private static Location bedford;
    private static Topic     topic;
    private static Availability av;
    private static ClassOption  option;

    private final PrintStream originalOutput = System.out;
    private ByteArrayOutputStream captureOutputStream;

    @BeforeAll
    static void setUpSuite() {
        bedford = new Location("Bedford Park", "101");

        ClassSession session = new ClassSession(
                LocalDate.of(2026, 2, 23), LocalDate.of(2026, 6, 1),
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), bedford);

        option = new ClassOption(new ClassOffering("Lecture", 1), session);

        av = Availability.parse("On Campus - City - S1 - 1");
        av.addClassOption(option);

        topic = Topic.parse("COMP9999 Test Topic");
        topic.addAvailability(av);

        // Add to AppState so service methods can find it
        AppState.getInstance().addTopic(topic);
    }

    @BeforeEach
    void setUpOutputCapture() {
        captureOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captureOutputStream));
    }

    @AfterEach
    void tearDownOutputCapture() {
        System.setOut(originalOutput);
    }

    private ClassDataService service() {
        return new ClassDataService(new Scanner("dummy\n"));
    }

    @Test @Order(1) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-CDS-01: Constructor — valid Scanner creates service")
    void tc_cds_01_constructor_valid() {
        Scanner scanner = new Scanner("dummy\n");
        assertDoesNotThrow(() -> new ClassDataService(scanner));
        scanner.close();
    }

    @Test @Order(2) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-02: Constructor — null Scanner throws NullPointerException")
    void tc_cds_02_constructor_null_throws() {
        assertThrows(NullPointerException.class, () -> new ClassDataService(null));
    }

    @Test @Order(3) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-03: Static constants — all search field names correct")
    void tc_cds_03_staticConstants() {
        assertAll("search field constants",
                () -> assertEquals("topicCode",          ClassDataService.S_TOPIC_CODE),
                () -> assertEquals("topicName",          ClassDataService.S_TOPIC_NAME),
                () -> assertEquals("attendanceMode",     ClassDataService.S_ATTEND_MODE),
                () -> assertEquals("campus",             ClassDataService.S_CAMPUS),
                () -> assertEquals("semester",           ClassDataService.S_SEMESTER),
                () -> assertEquals("availabilityNumber", ClassDataService.S_AVAIL_NUMBER),
                () -> assertEquals("className",          ClassDataService.S_CLASS_NAME),
                () -> assertEquals("classInstance",      ClassDataService.S_CLASS_INSTANCE),
                () -> assertEquals("firstDate",          ClassDataService.S_FIRST_DATE),
                () -> assertEquals("lastDate",           ClassDataService.S_LAST_DATE),
                () -> assertEquals("day",                ClassDataService.S_DAY),
                () -> assertEquals("startTime",          ClassDataService.S_START_TIME),
                () -> assertEquals("endTime",            ClassDataService.S_END_TIME),
                () -> assertEquals("building",           ClassDataService.S_BUILDING),
                () -> assertEquals("room",               ClassDataService.S_ROOM)
        );
    }

    @Test @Order(4) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-04: importFromCsv — non-existent path returns false")
    void tc_cds_04_importFromCsv_invalidPath_returnsFalse() {
        assertFalse(service().importFromCsv("nonexistent_file_xyz.csv"));
    }

    @Test @Order(5) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-05: importFromCsv — blank/spaces path throws InvalidPathException on Windows")
    void tc_cds_05_importFromCsv_blankPath_throwsOrReturnsFalse() {
        assertThrows(Exception.class, () -> service().importFromCsv("   "));
    }

    @Test @Order(6) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-CDS-06: FlatRecord — all fields populated correctly from topic/av/option")
    void tc_cds_06_flatRecord_fieldsPopulated() {
        ClassDataService.FlatRecord rec =
                new ClassDataService.FlatRecord(topic, av, option);
        assertAll("FlatRecord fields",
                () -> assertEquals("COMP9999",    rec.topicCode),
                () -> assertEquals("Test Topic",  rec.topicName),
                () -> assertEquals("On Campus",   rec.attendanceMode),
                () -> assertEquals("City",        rec.campusLocation),
                () -> assertEquals("S1",          rec.semesterN),
                () -> assertEquals("1",           rec.availabilityNumber),
                () -> assertEquals("Lecture",     rec.className),
                () -> assertEquals(1,             rec.classInstance),
                () -> assertEquals(DayOfWeek.MONDAY, rec.day),
                () -> assertEquals(LocalTime.of(9,  0), rec.startTime),
                () -> assertEquals(LocalTime.of(11, 0), rec.endTime),
                () -> assertEquals("Bedford Park", rec.building),
                () -> assertEquals("101",          rec.room),
                () -> assertSame(av,     rec.availability),
                () -> assertSame(option, rec.classOption)
        );
    }

    @Test @Order(7) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-07: FlatRecord.groupKey — returns correct pipe-separated key")
    void tc_cds_07_flatRecord_groupKey() {
        ClassDataService.FlatRecord rec =
                new ClassDataService.FlatRecord(topic, av, option);
        String key = rec.groupKey();
        assertAll("groupKey contains all fields",
                () -> assertTrue(key.contains("COMP9999")),
                () -> assertTrue(key.contains("Test Topic")),
                () -> assertTrue(key.contains("On Campus")),
                () -> assertTrue(key.contains("City")),
                () -> assertTrue(key.contains("S1")),
                () -> assertTrue(key.contains("Lecture")),
                () -> assertTrue(key.contains("|"))
        );
    }

    @Test @Order(8) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-08: browseAll — runs without exception when AppState has data")
    void tc_cds_08_browseAll_withData_noException() {
        assertDoesNotThrow(() -> service().browseAll());
    }

    @Test @Order(9) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-09: viewTopicCode — non-existent topic runs without exception")
    void tc_cds_09_viewTopicCode_notFound_noException() {
        assertDoesNotThrow(() -> service().viewTopicCode("XXXX0000"));
    }

    @Test @Order(10) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-10: viewTopicCode — existing topic runs without exception")
    void tc_cds_10_viewTopicCode_found_noException() {
        assertDoesNotThrow(() -> service().viewTopicCode("COMP9999"));
    }

    @Test @Order(11) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-11: search — empty criteria map runs without exception")
    void tc_cds_11_search_emptyCriteria_noException() {
        assertDoesNotThrow(() -> service().search(Map.of()));
    }

    @Test @Order(12) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-12: search — topicCode criteria matches existing topic")
    void tc_cds_12_search_topicCodeCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_TOPIC_CODE, "COMP9999")));
    }

    @Test @Order(13) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-13: search — no results criteria runs without exception")
    void tc_cds_13_search_noResults_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_TOPIC_CODE, "ZZZZZZZ")));
    }

    @Test @Order(14) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-14: search — campus criteria runs without exception")
    void tc_cds_14_search_campusCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_CAMPUS, "City")));
    }

    @Test @Order(15) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-15: search — day criteria runs without exception")
    void tc_cds_15_search_dayCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_DAY, "MONDAY")));
    }

    @Test @Order(16) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-16: search — className criteria runs without exception")
    void tc_cds_16_search_classNameCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_CLASS_NAME, "Lecture")));
    }

    @Test @Order(17) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-17: search — classInstance criteria runs without exception")
    void tc_cds_17_search_classInstanceCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_CLASS_INSTANCE, "1")));
    }

    @Test @Order(18) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-18: search — startTime criteria runs without exception")
    void tc_cds_18_search_startTimeCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_START_TIME, "09:00")));
    }

    @Test @Order(19) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-19: search — endTime criteria runs without exception")
    void tc_cds_19_search_endTimeCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_END_TIME, "11:00")));
    }

    @Test @Order(20) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-20: search — building criteria runs without exception")
    void tc_cds_20_search_buildingCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_BUILDING, "Bedford")));
    }

    @Test @Order(21) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-21: search — room criteria runs without exception")
    void tc_cds_21_search_roomCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_ROOM, "101")));
    }

    @Test @Order(22) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-22: search — semester criteria runs without exception")
    void tc_cds_22_search_semesterCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_SEMESTER, "S1")));
    }

    @Test @Order(23) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-23: search — topicName criteria runs without exception")
    void tc_cds_23_search_topicNameCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_TOPIC_NAME, "Test")));
    }

    @Test @Order(24) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-24: search — firstDate criteria runs without exception")
    void tc_cds_24_search_firstDateCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_FIRST_DATE, "2026")));
    }

    @Test @Order(25) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-25: search — lastDate criteria runs without exception")
    void tc_cds_25_search_lastDateCriteria_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_LAST_DATE, "2026")));
    }

    @Test @Order(26) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-26: search — availabilityNumber criteria runs without exception")
    void tc_cds_26_search_availabilityNumber_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of(ClassDataService.S_AVAIL_NUMBER, "1")));
    }

    @Test @Order(27) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-27: search — unknown field key is ignored gracefully")
    void tc_cds_27_search_unknownKey_noException() {
        assertDoesNotThrow(() -> service().search(
                Map.of("unknownField", "someValue")));
    }

    @Test @Order(28) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-28: search — null value in criteria is treated as blank")
    void tc_cds_28_search_nullValue_noException() {
        Map<String, String> criteria = new java.util.HashMap<>();
        criteria.put(ClassDataService.S_TOPIC_CODE, null);
        assertDoesNotThrow(() -> service().search(criteria));
    }

    @Test
    @Order(29)
    @Tag("Dass0027")
    @Tag("Additional")
    @DisplayName("Unknown search field should stop search")
    void UnknownSearchFieldShouldStopSearch() {
        ClassDataService classDataService = service();

        classDataService.search(Map.of("unknownField", "someValue"));

        String output = captureOutputStream.toString();

        assertFalse(output.contains("Search Results"),
                "Expected search to stop when search field is invalid, but actual output was: " + output);
    }
}

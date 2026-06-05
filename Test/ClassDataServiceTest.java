import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ClassDataService Tests")
// ClassDataService depends on Scanner (user input) and AppState (file I/O singleton).
// Tests here cover the constructor, static constants, and importFromCsv.
// Interactive methods (browse, edit, delete, search) require black-box testing.
class ClassDataServiceTest {

    // ── Constructor ───────────────────────────────────────────────────────────

    @Test @Order(1) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-CDS-01: Constructor — valid Scanner creates service without error")
    void tc_cds_01_constructor_validScanner() {
        Scanner scanner = new Scanner("dummy input\n");
        assertDoesNotThrow(() -> new ClassDataService(scanner));
        scanner.close();
    }

    @Test @Order(2) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-02: Constructor — null Scanner throws NullPointerException")
    void tc_cds_02_constructor_nullScanner_throws() {
        assertThrows(NullPointerException.class, () -> new ClassDataService(null));
    }

    // ── Static search field name constants ────────────────────────────────────

    @Test @Order(3) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-03: Static constants — all search field names are defined correctly")
    void tc_cds_03_staticConstants_allDefined() {
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

    // ── importFromCsv ─────────────────────────────────────────────────────────

    @Test @Order(4) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-04: importFromCsv — non-existent path returns false (no crash)")
    void tc_cds_04_importFromCsv_invalidPath_returnsFalse() {
        Scanner scanner = new Scanner("dummy\n");
        ClassDataService service = new ClassDataService(scanner);
        boolean result = service.importFromCsv("nonexistent_file_xyz.csv");
        assertFalse(result);
        scanner.close();
    }

    @Test @Order(5) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-CDS-05: importFromCsv — blank path returns false (no crash)")
    void tc_cds_05_importFromCsv_blankPath_returnsFalse() {
        Scanner scanner = new Scanner("dummy\n");
        ClassDataService service = new ClassDataService(scanner);
        boolean result = service.importFromCsv("   ");
        assertFalse(result);
        scanner.close();
    }
}

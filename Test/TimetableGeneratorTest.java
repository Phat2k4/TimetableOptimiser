import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("TimetableGenerator Tests")
class TimetableGeneratorTest {

    private static TimetableGenerator.GenerationSettings minimalSettings(String topicCode) {
        return new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of(topicCode)).semester("1").build();
    }

    private static void resetLastUsed() throws Exception {
        Field f = TimetableGenerator.class.getDeclaredField("lastUsedSettings");
        f.setAccessible(true);
        f.set(null, null);
    }

    // ── 1. GenerationSettings construction ───────────────────────────────────

    @Test @Order(1) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-01: null name is normalised to empty string")
    void tc_tg_01_nullNameBecomesEmpty() {
        var s = new TimetableGenerator.GenerationSettings(
                null, "1", List.of("COMP1234"), "", false, List.of());
        assertEquals("", s.name);
    }

    @Test @Order(2) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-02: name with surrounding whitespace is trimmed")
    void tc_tg_02_nameIsTrimmed() {
        var s = new TimetableGenerator.GenerationSettings(
                "  MyTT  ", "1", List.of("X"), "", false, List.of());
        assertEquals("MyTT", s.name);
    }

    @Test @Order(3) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-03: null semester defaults to 'both'")
    void tc_tg_03_nullSemesterDefaultsBoth() {
        var s = new TimetableGenerator.GenerationSettings(
                "T1", null, List.of("COMP1234"), "", false, List.of());
        assertEquals("both", s.semester);
    }

    @Test @Order(4) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-04: null topicCodes is treated as empty list")
    void tc_tg_04_nullTopicCodesBecomesEmpty() {
        var s = new TimetableGenerator.GenerationSettings(
                "T1", "1", null, "", false, List.of());
        assertTrue(s.topicCodes.isEmpty());
    }

    @Test @Order(5) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-05: null preferenceOrder is treated as empty list")
    void tc_tg_05_nullPreferenceOrderBecomesEmpty() {
        var s = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("X"), "", false, null);
        assertTrue(s.preferenceOrder.isEmpty());
    }

    @Test @Order(6) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-06: null preferredCampus is normalised to empty string")
    void tc_tg_06_nullPreferredCampusBecomesEmpty() {
        var s = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("X"), null, false, List.of());
        assertEquals("", s.preferredCampus);
    }

    @Test @Order(7) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-07: preferredCampus with surrounding whitespace is trimmed")
    void tc_tg_07_preferredCampusIsTrimmed() {
        var s = new TimetableGenerator.GenerationSettings(
                "", "1", List.of("X"), "  Tonsley  ", false, List.of());
        assertEquals("Tonsley", s.preferredCampus);
    }

    // ── 2. Immutability ───────────────────────────────────────────────────────

    @Test @Order(8) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-08: mutating original topicCodes list does not affect settings")
    void tc_tg_08_topicCodesDefensivelyCopied() {
        List<String> codes = new ArrayList<>(List.of("COMP1234"));
        var s = new TimetableGenerator.GenerationSettings(
                "T1", "1", codes, "", false, List.of());
        codes.add("COMP9999");
        assertEquals(1, s.topicCodes.size());
    }

    @Test @Order(9) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-09: topicCodes list on settings is unmodifiable")
    void tc_tg_09_topicCodesUnmodifiable() {
        var s = new TimetableGenerator.GenerationSettings(
                "T1", "1", new ArrayList<>(List.of("X")), "", false, List.of());
        assertThrows(UnsupportedOperationException.class, () -> s.topicCodes.add("Y"));
    }

    @Test @Order(10) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-10: mutating original preferenceOrder list does not affect settings")
    void tc_tg_10_preferenceOrderDefensivelyCopied() {
        List<String> prefs = new ArrayList<>(List.of("mornings"));
        var s = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("X"), "", false, prefs);
        prefs.add("afternoons");
        assertEquals(1, s.preferenceOrder.size());
    }

    @Test @Order(11) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-11: preferenceOrder list on settings is unmodifiable")
    void tc_tg_11_preferenceOrderUnmodifiable() {
        var s = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("X"), "", false, new ArrayList<>(List.of("mornings")));
        assertThrows(UnsupportedOperationException.class,
                () -> s.preferenceOrder.add("afternoons"));
    }

    // ── 3. Builder ────────────────────────────────────────────────────────────

    @Test @Order(12) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-12: builder defaults — semester=both, no overlap, empty lists")
    void tc_tg_12_builderDefaults() {
        var s = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("X")).build();
        assertAll(
                () -> assertEquals("both", s.semester),
                () -> assertEquals("",     s.name),
                () -> assertEquals("",     s.preferredCampus),
                () -> assertFalse(s.allowLectureOverlap),
                () -> assertTrue(s.preferenceOrder.isEmpty())
        );
    }

    @Test @Order(13) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-13: toBuilder round-trip preserves all fields")
    void tc_tg_13_toBuilderRoundTrip() {
        var original = new TimetableGenerator.GenerationSettings(
                "MyTT", "2", List.of("COMP1"), "Bedford Park", true, List.of("mornings"));
        var copy = original.toBuilder().build();
        assertAll(
                () -> assertEquals(original.name,                copy.name),
                () -> assertEquals(original.semester,            copy.semester),
                () -> assertEquals(original.topicCodes,          copy.topicCodes),
                () -> assertEquals(original.preferredCampus,     copy.preferredCampus),
                () -> assertEquals(original.allowLectureOverlap, copy.allowLectureOverlap),
                () -> assertEquals(original.preferenceOrder,     copy.preferenceOrder)
        );
    }

    @Test @Order(14) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-14: toBuilder partial override changes only the targeted field")
    void tc_tg_14_toBuilderPartialOverride() {
        var original = new TimetableGenerator.GenerationSettings(
                "MyTT", "1", List.of("COMP1"), "Bedford Park", false, List.of());
        var modified = original.toBuilder().semester("2").build();
        assertAll(
                () -> assertEquals("2",            modified.semester),
                () -> assertEquals("MyTT",         modified.name),
                () -> assertEquals("Bedford Park", modified.preferredCampus),
                () -> assertFalse(modified.allowLectureOverlap)
        );
    }

    @Test @Order(15) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-15: allowLectureOverlap true is preserved through builder")
    void tc_tg_15_lectureOverlapFlagPreserved() {
        var s = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("X")).allowLectureOverlap(true).build();
        assertTrue(s.allowLectureOverlap);
    }

    // ── 4. toString ───────────────────────────────────────────────────────────

    @Test @Order(16) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-16: toString contains all major field values")
    void tc_tg_16_toStringContainsKeyFields() {
        var s = new TimetableGenerator.GenerationSettings(
                "MyTT", "2", List.of("COMP3742"), "Bedford Park", true, List.of("mornings"));
        String str = s.toString();
        assertAll(
                () -> assertTrue(str.contains("MyTT"),         "name missing"),
                () -> assertTrue(str.contains("COMP3742"),     "topic code missing"),
                () -> assertTrue(str.contains("Bedford Park"), "campus missing"),
                () -> assertTrue(str.contains("true"),         "lectureOverlap missing"),
                () -> assertTrue(str.contains("mornings"),     "preference missing")
        );
    }

    @Test @Order(17) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-17: toString on default-built settings does not throw")
    void tc_tg_17_toStringDoesNotThrow() {
        var s = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("X")).build();
        assertDoesNotThrow(s::toString);
    }

    // ── 5. GenerationResult ───────────────────────────────────────────────────

    @Test @Order(18) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-18: fail() — success=false, timetable=null, errorMessage set")
    void tc_tg_18_failResultShape() {
        var result = TimetableGenerator.GenerationResult.fail("something went wrong");
        assertAll(
                () -> assertFalse(result.success),
                () -> assertNull(result.timetable),
                () -> assertEquals("something went wrong", result.errorMessage),
                () -> assertTrue(result.unmetPreferences.isEmpty())
        );
    }

    @Test @Order(19) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-19: ok() — success=true, errorMessage=null")
    void tc_tg_19_okResultShape() {
        var result = TimetableGenerator.GenerationResult.ok(null, List.of());
        assertAll(
                () -> assertTrue(result.success),
                () -> assertNull(result.errorMessage),
                () -> assertTrue(result.unmetPreferences.isEmpty())
        );
    }

    @Test @Order(20) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-20: ok() carries unmet preferences supplied to it")
    void tc_tg_20_okResultCarriesUnmetPrefs() {
        var result = TimetableGenerator.GenerationResult.ok(
                null, List.of("mornings", "monday"));
        assertEquals(List.of("mornings", "monday"), result.unmetPreferences);
    }

    @Test @Order(21) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-21: unmetPreferences list is unmodifiable")
    void tc_tg_21_unmetPreferencesUnmodifiable() {
        var result = TimetableGenerator.GenerationResult.ok(
                null, new ArrayList<>(List.of("mornings")));
        assertThrows(UnsupportedOperationException.class,
                () -> result.unmetPreferences.add("afternoons"));
    }

    @Test @Order(22) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-22: fail() unmetPreferences is empty not null")
    void tc_tg_22_failResultUnmetPrefsNotNull() {
        var result = TimetableGenerator.GenerationResult.fail("err");
        assertNotNull(result.unmetPreferences);
        assertTrue(result.unmetPreferences.isEmpty());
    }

    // ── 6. generate() early-exit paths ───────────────────────────────────────

    @Test @Order(23) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-23: null settings throws NullPointerException immediately")
    void tc_tg_23_nullSettingsThrowsNPE() {
        assertThrows(NullPointerException.class,
                () -> new TimetableGenerator().generate(null));
    }

    @Test @Order(24) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-24: empty topicCodes returns failure result")
    void tc_tg_24_emptyTopicCodesReturnsFail() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of()).semester("1").build();
        var result = new TimetableGenerator().generate(settings);
        assertFalse(result.success);
        assertNotNull(result.errorMessage);
    }

    @Test @Order(25) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-25: empty topicCodes error message mentions 'topic'")
    void tc_tg_25_emptyTopicCodesErrorMentionsTopic() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of()).build();
        var result = new TimetableGenerator().generate(settings);
        assertTrue(result.errorMessage.toLowerCase().contains("topic"));
    }

    @Test @Order(26) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-26: empty topicCodes result has null timetable")
    void tc_tg_26_emptyTopicCodesNullTimetable() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of()).build();
        assertNull(new TimetableGenerator().generate(settings).timetable);
    }

    // ── 7. lastUsedSettings ───────────────────────────────────────────────────

    @Test @Order(27) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-27: getLastUsedSettings() returns null before any generate() call")
    void tc_tg_27_initiallyNull() throws Exception {
        resetLastUsed();
        assertNull(TimetableGenerator.getLastUsedSettings());
    }

    @Test @Order(28) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-28: generate() stores settings even when generation fails")
    void tc_tg_28_settingsStoredOnFailure() throws Exception {
        resetLastUsed();
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of()).semester("1").build();
        new TimetableGenerator().generate(settings);
        assertSame(settings, TimetableGenerator.getLastUsedSettings());
    }

    // ── 8. Preference ordering ────────────────────────────────────────────────

    @Test @Order(29) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-29: preferenceOrder is stored in insertion order")
    void tc_tg_29_insertionOrderPreserved() {
        var prefs = List.of("mornings", "bedford park", "monday");
        var s = new TimetableGenerator.GenerationSettings(
                "", "1", List.of("X"), "", false, prefs);
        assertEquals(prefs, s.preferenceOrder);
    }

    @Test @Order(30) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-30: all distinct preferences are stored correctly")
    void tc_tg_30_allPreferencesStored() {
        var prefs = List.of("mornings", "afternoons", "monday", "compact", "evenly spread");
        var s = new TimetableGenerator.GenerationSettings(
                "", "1", List.of("X"), "", false, prefs);
        assertEquals(5, s.preferenceOrder.size());
    }

    // ── 9. Semester settings ──────────────────────────────────────────────────

    @Test @Order(31) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-31: semester '1' stored verbatim")
    void tc_tg_31_semester1() {
        assertEquals("1", minimalSettings("COMP1234").semester);
    }

    @Test @Order(32) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-32: semester '2' stored verbatim")
    void tc_tg_32_semester2() {
        var s = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("X")).semester("2").build();
        assertEquals("2", s.semester);
    }

    @Test @Order(33) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-33: semester defaults to 'both' when not set via builder")
    void tc_tg_33_semesterDefaultBoth() {
        var s = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("X")).build();
        assertEquals("both", s.semester);
    }
}

import model.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("TimetableGenerator Tests")
class TimetableGeneratorTest {

    private static TimetableGenerator.GenerationSettings minimalSettings(String topicCode) {
        return new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of(topicCode))
                .semester("1")
                .build();
    }

    private static void resetLastUsed() throws Exception {
        Field field = TimetableGenerator.class.getDeclaredField("lastUsedSettings");
        field.setAccessible(true);
        field.set(null, null);
    }

    private Object invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        TimetableGenerator generator = new TimetableGenerator();
        Method method = TimetableGenerator.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(generator, args);
    }

    private ClassSession session(DayOfWeek day, LocalTime start, LocalTime end, String building) {
        return new ClassSession(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 6, 1),
                day,
                start,
                end,
                new Location(building, "Room 1")
        );
    }

    private ClassOption option(String className, int instance, DayOfWeek day,
                               LocalTime start, LocalTime end, String building) {
        return new ClassOption(
                new ClassOffering(className, instance),
                session(day, start, end, building)
        );
    }

    private Availability availability(String campus, String semester) {
        return new Availability("On Campus", campus, semester, "1");
    }

    @Test @Order(1) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-01: null name is normalised to empty string")
    void tc_tg_01_nullNameBecomesEmpty() {
        var settings = new TimetableGenerator.GenerationSettings(
                null, "1", List.of("COMP1234"), "", false, List.of());

        assertEquals("", settings.name);
    }

    @Test @Order(2) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-02: name with whitespace is trimmed")
    void tc_tg_02_nameIsTrimmed() {
        var settings = new TimetableGenerator.GenerationSettings(
                "  MyTT  ", "1", List.of("COMP1234"), "", false, List.of());

        assertEquals("MyTT", settings.name);
    }

    @Test @Order(3) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-03: null semester defaults to both")
    void tc_tg_03_nullSemesterDefaultsBoth() {
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", null, List.of("COMP1234"), "", false, List.of());

        assertEquals("both", settings.semester);
    }

    @Test @Order(4) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-04: null topicCodes becomes empty")
    void tc_tg_04_nullTopicCodesBecomesEmpty() {
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", "1", null, "", false, List.of());

        assertTrue(settings.topicCodes.isEmpty());
    }

    @Test @Order(5) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-05: null preferenceOrder becomes empty")
    void tc_tg_05_nullPreferenceOrderBecomesEmpty() {
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("COMP1234"), "", false, null);

        assertTrue(settings.preferenceOrder.isEmpty());
    }

    @Test @Order(6) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-06: null preferredCampus becomes empty")
    void tc_tg_06_nullPreferredCampusBecomesEmpty() {
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("COMP1234"), null, false, List.of());

        assertEquals("", settings.preferredCampus);
    }

    @Test @Order(7) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-07: preferredCampus with whitespace is trimmed")
    void tc_tg_07_preferredCampusIsTrimmed() {
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("COMP1234"), "  Tonsley  ", false, List.of());

        assertEquals("Tonsley", settings.preferredCampus);
    }

    @Test @Order(8) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-08: topicCodes are defensively copied")
    void tc_tg_08_topicCodesDefensivelyCopied() {
        List<String> codes = new ArrayList<>(List.of("COMP1234"));
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", "1", codes, "", false, List.of());

        codes.add("COMP9999");

        assertEquals(1, settings.topicCodes.size());
    }

    @Test @Order(9) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-09: topicCodes list is unmodifiable")
    void tc_tg_09_topicCodesUnmodifiable() {
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("COMP1234"), "", false, List.of());

        assertThrows(UnsupportedOperationException.class,
                () -> settings.topicCodes.add("COMP9999"));
    }

    @Test @Order(10) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-10: preferenceOrder is defensively copied")
    void tc_tg_10_preferenceOrderDefensivelyCopied() {
        List<String> preferences = new ArrayList<>(List.of("mornings"));
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("COMP1234"), "", false, preferences);

        preferences.add("afternoons");

        assertEquals(1, settings.preferenceOrder.size());
    }

    @Test @Order(11) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-11: preferenceOrder list is unmodifiable")
    void tc_tg_11_preferenceOrderUnmodifiable() {
        var settings = new TimetableGenerator.GenerationSettings(
                "T1", "1", List.of("COMP1234"), "", false, List.of("mornings"));

        assertThrows(UnsupportedOperationException.class,
                () -> settings.preferenceOrder.add("afternoons"));
    }

    @Test @Order(12) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-12: builder default values are correct")
    void tc_tg_12_builderDefaults() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("COMP1234"))
                .build();

        assertEquals("both", settings.semester);
        assertEquals("", settings.name);
        assertEquals("", settings.preferredCampus);
        assertFalse(settings.allowLectureOverlap);
        assertTrue(settings.preferenceOrder.isEmpty());
    }

    @Test @Order(13) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-13: toBuilder preserves all fields")
    void tc_tg_13_toBuilderRoundTrip() {
        var original = new TimetableGenerator.GenerationSettings(
                "MyTT", "2", List.of("COMP1234"), "Bedford Park", true, List.of("mornings"));

        var copy = original.toBuilder().build();

        assertEquals(original.name, copy.name);
        assertEquals(original.semester, copy.semester);
        assertEquals(original.topicCodes, copy.topicCodes);
        assertEquals(original.preferredCampus, copy.preferredCampus);
        assertEquals(original.allowLectureOverlap, copy.allowLectureOverlap);
        assertEquals(original.preferenceOrder, copy.preferenceOrder);
    }

    @Test @Order(14) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-14: toBuilder partial override changes only selected field")
    void tc_tg_14_toBuilderPartialOverride() {
        var original = new TimetableGenerator.GenerationSettings(
                "MyTT", "1", List.of("COMP1234"), "Bedford Park", false, List.of());

        var modified = original.toBuilder().semester("2").build();

        assertEquals("2", modified.semester);
        assertEquals("MyTT", modified.name);
        assertEquals("Bedford Park", modified.preferredCampus);
        assertFalse(modified.allowLectureOverlap);
    }

    @Test @Order(15) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-15: builder preserves lecture overlap flag")
    void tc_tg_15_lectureOverlapFlagPreserved() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("COMP1234"))
                .allowLectureOverlap(true)
                .build();

        assertTrue(settings.allowLectureOverlap);
    }

    @Test @Order(16) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-16: toString contains important values")
    void tc_tg_16_toStringContainsKeyFields() {
        var settings = new TimetableGenerator.GenerationSettings(
                "MyTT", "2", List.of("COMP3742"), "Bedford Park", true, List.of("mornings"));

        String text = settings.toString();

        assertTrue(text.contains("MyTT"));
        assertTrue(text.contains("COMP3742"));
        assertTrue(text.contains("Bedford Park"));
        assertTrue(text.contains("true"));
        assertTrue(text.contains("mornings"));
    }

    @Test @Order(17) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-17: toString does not throw")
    void tc_tg_17_toStringDoesNotThrow() {
        var settings = minimalSettings("COMP1234");

        assertDoesNotThrow(settings::toString);
    }

    @Test @Order(18) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-18: fail result shape is correct")
    void tc_tg_18_failResultShape() {
        var result = TimetableGenerator.GenerationResult.fail("something went wrong");

        assertFalse(result.success);
        assertNull(result.timetable);
        assertEquals("something went wrong", result.errorMessage);
        assertTrue(result.unmetPreferences.isEmpty());
    }

    @Test @Order(19) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-19: ok result shape is correct")
    void tc_tg_19_okResultShape() {
        var result = TimetableGenerator.GenerationResult.ok(null, List.of());

        assertTrue(result.success);
        assertNull(result.errorMessage);
        assertTrue(result.unmetPreferences.isEmpty());
    }

    @Test @Order(20) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-20: ok result stores unmet preferences")
    void tc_tg_20_okResultCarriesUnmetPrefs() {
        var result = TimetableGenerator.GenerationResult.ok(null, List.of("mornings", "monday"));

        assertEquals(List.of("mornings", "monday"), result.unmetPreferences);
    }

    @Test @Order(21) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-21: unmet preferences list is unmodifiable")
    void tc_tg_21_unmetPreferencesUnmodifiable() {
        var result = TimetableGenerator.GenerationResult.ok(null, List.of("mornings"));

        assertThrows(UnsupportedOperationException.class,
                () -> result.unmetPreferences.add("afternoons"));
    }

    @Test @Order(22) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-22: fail result unmet preferences is not null")
    void tc_tg_22_failResultUnmetPrefsNotNull() {
        var result = TimetableGenerator.GenerationResult.fail("err");

        assertNotNull(result.unmetPreferences);
        assertTrue(result.unmetPreferences.isEmpty());
    }

    @Test @Order(23) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-23: null settings throws NullPointerException")
    void tc_tg_23_nullSettingsThrowsNPE() {
        assertThrows(NullPointerException.class,
                () -> new TimetableGenerator().generate(null));
    }

    @Test @Order(24) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-24: empty topicCodes returns failure")
    void tc_tg_24_emptyTopicCodesReturnsFail() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of())
                .semester("1")
                .build();

        var result = new TimetableGenerator().generate(settings);

        assertFalse(result.success);
        assertNotNull(result.errorMessage);
    }

    @Test @Order(25) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-25: empty topicCodes error mentions topic")
    void tc_tg_25_emptyTopicCodesErrorMentionsTopic() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of())
                .build();

        var result = new TimetableGenerator().generate(settings);

        assertTrue(result.errorMessage.toLowerCase().contains("topic"));
    }

    @Test @Order(26) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-TG-26: empty topicCodes has null timetable")
    void tc_tg_26_emptyTopicCodesNullTimetable() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of())
                .build();

        assertNull(new TimetableGenerator().generate(settings).timetable);
    }

    @Test @Order(27) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-27: getLastUsedSettings is null before generation")
    void tc_tg_27_initiallyNull() throws Exception {
        resetLastUsed();

        assertNull(TimetableGenerator.getLastUsedSettings());
    }

    @Test @Order(28) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-TG-28: generate stores settings even on failure")
    void tc_tg_28_settingsStoredOnFailure() throws Exception {
        resetLastUsed();

        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of())
                .semester("1")
                .build();

        new TimetableGenerator().generate(settings);

        assertNotNull(TimetableGenerator.getLastUsedSettings());
        assertEquals(settings.topicCodes, TimetableGenerator.getLastUsedSettings().topicCodes);
        assertEquals(settings.semester, TimetableGenerator.getLastUsedSettings().semester);
    }

    @Test @Order(29) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-29: preference order preserves insertion order")
    void tc_tg_29_insertionOrderPreserved() {
        List<String> preferences = List.of("mornings", "bedford park", "monday");
        var settings = new TimetableGenerator.GenerationSettings(
                "", "1", List.of("COMP1234"), "", false, preferences);

        assertEquals(preferences, settings.preferenceOrder);
    }

    @Test @Order(30) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-30: all distinct preferences are stored")
    void tc_tg_30_allPreferencesStored() {
        List<String> preferences = List.of("mornings", "afternoons", "monday", "compact", "evenly spread");
        var settings = new TimetableGenerator.GenerationSettings(
                "", "1", List.of("COMP1234"), "", false, preferences);

        assertEquals(5, settings.preferenceOrder.size());
    }

    @Test @Order(31) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-31: semester 1 is stored")
    void tc_tg_31_semester1() {
        assertEquals("1", minimalSettings("COMP1234").semester);
    }

    @Test @Order(32) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-32: semester 2 is stored")
    void tc_tg_32_semester2() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("COMP1234"))
                .semester("2")
                .build();

        assertEquals("2", settings.semester);
    }

    @Test @Order(33) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-33: semester defaults to both")
    void tc_tg_33_semesterDefaultBoth() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("COMP1234"))
                .build();

        assertEquals("both", settings.semester);
    }

    @Test @Order(34) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-34: builder stores preferred campus")
    void tc_tg_34_builderStoresPreferredCampus() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of("COMP1234"))
                .preferredCampus("Tonsley")
                .build();

        assertEquals("Tonsley", settings.preferredCampus);
    }

    @Test @Order(35) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-35: builder stores timetable name")
    void tc_tg_35_builderStoresName() {
        var settings = new TimetableGenerator.GenerationSettings.Builder()
                .name("My Timetable")
                .topicCodes(List.of("COMP1234"))
                .build();

        assertEquals("My Timetable", settings.name);
    }

    @Test @Order(36) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-36: isCityCampus recognises Flinders City Campus")
    void tc_tg_36_isCityCampusRecognisesCityCampus() throws Exception {
        assertTrue((boolean) invokePrivate(
                "isCityCampus",
                new Class<?>[]{String.class},
                "Flinders City Campus"));
    }

    @Test @Order(37) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-37: isCityCampus recognises lowercase city")
    void tc_tg_37_isCityCampusLowercase() throws Exception {
        assertTrue((boolean) invokePrivate(
                "isCityCampus",
                new Class<?>[]{String.class},
                "city"));
    }

    @Test @Order(38) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-38: isCityCampus rejects Bedford Park")
    void tc_tg_38_isCityCampusRejectsBedfordPark() throws Exception {
        assertFalse((boolean) invokePrivate(
                "isCityCampus",
                new Class<?>[]{String.class},
                "Bedford Park"));
    }

    @Test @Order(39) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-39: isCityCampus rejects Tonsley")
    void tc_tg_39_isCityCampusRejectsTonsley() throws Exception {
        assertFalse((boolean) invokePrivate(
                "isCityCampus",
                new Class<?>[]{String.class},
                "Tonsley"));
    }

    @Test @Order(40) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-40: allSameCampus returns true for empty list")
    void tc_tg_40_allSameCampusReturnsTrueWhenEmpty() throws Exception {
        assertTrue((boolean) invokePrivate(
                "allSameCampus",
                new Class<?>[]{List.class},
                List.of()));
    }

    @Test @Order(41) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-41: allSameCampus returns true for same building")
    void tc_tg_41_allSameCampusReturnsTrueForSameBuilding() throws Exception {
        List<ClassSession> sessions = List.of(
                session(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"),
                session(DayOfWeek.TUESDAY, LocalTime.of(11, 0), LocalTime.of(12, 0), "Tonsley")
        );

        assertTrue((boolean) invokePrivate(
                "allSameCampus",
                new Class<?>[]{List.class},
                sessions));
    }

    @Test @Order(42) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-42: allSameCampus returns false for mixed buildings")
    void tc_tg_42_allSameCampusReturnsFalseForMixedBuildings() throws Exception {
        List<ClassSession> sessions = List.of(
                session(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"),
                session(DayOfWeek.TUESDAY, LocalTime.of(11, 0), LocalTime.of(12, 0), "Bedford Park")
        );

        assertFalse((boolean) invokePrivate(
                "allSameCampus",
                new Class<?>[]{List.class},
                sessions));
    }

    @Test @Order(43) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-43: prefSatisfied recognises mornings")
    void tc_tg_43_prefSatisfiedMornings() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley")
        );

        assertTrue((boolean) invokePrivate(
                "prefSatisfied",
                new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class},
                combo,
                "mornings",
                minimalSettings("COMP1234")));
    }

    @Test @Order(44) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-44: prefSatisfied rejects mornings for afternoon class")
    void tc_tg_44_prefSatisfiedMorningsFalse() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(14, 0), "Tonsley")
        );

        assertFalse((boolean) invokePrivate(
                "prefSatisfied",
                new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class},
                combo,
                "mornings",
                minimalSettings("COMP1234")));
    }

    @Test @Order(45) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-45: prefSatisfied recognises afternoons")
    void tc_tg_45_prefSatisfiedAfternoons() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(14, 0), "Tonsley")
        );

        assertTrue((boolean) invokePrivate(
                "prefSatisfied",
                new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class},
                combo,
                "afternoons",
                minimalSettings("COMP1234")));
    }

    @Test @Order(46) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-46: prefSatisfied recognises Bedford Park campus")
    void tc_tg_46_prefSatisfiedBedfordParkCampus() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Bedford Park")
        );

        assertTrue((boolean) invokePrivate(
                "prefSatisfied",
                new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class},
                combo,
                "bedford park",
                minimalSettings("COMP1234")));
    }

    @Test @Order(47) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-47: prefSatisfied recognises Tonsley campus")
    void tc_tg_47_prefSatisfiedTonsleyCampus() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley")
        );

        assertTrue((boolean) invokePrivate(
                "prefSatisfied",
                new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class},
                combo,
                "tonsley",
                minimalSettings("COMP1234")));
    }

    @Test @Order(48) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-48: prefSatisfied recognises city campus")
    void tc_tg_48_prefSatisfiedCityCampus() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Flinders City Campus")
        );

        assertTrue((boolean) invokePrivate(
                "prefSatisfied",
                new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class},
                combo,
                "city",
                minimalSettings("COMP1234")));
    }

    @Test @Order(49) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-49: prefSatisfied recognises all at same campus")
    void tc_tg_49_prefSatisfiedAllSameCampus() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"),
                option("Tutorial", 1, DayOfWeek.TUESDAY, LocalTime.of(11, 0), LocalTime.of(12, 0), "Tonsley")
        );

        assertTrue((boolean) invokePrivate(
                "prefSatisfied",
                new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class},
                combo,
                "all at same campus",
                minimalSettings("COMP1234")));
    }

    @Test @Order(50) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-50: prefSatisfied recognises weekdays")
    void tc_tg_50_prefSatisfiedWeekdays() throws Exception {
        List<ClassOption> mondayCombo = List.of(option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));
        List<ClassOption> tuesdayCombo = List.of(option("Lecture", 1, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));
        List<ClassOption> wednesdayCombo = List.of(option("Lecture", 1, DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));
        List<ClassOption> thursdayCombo = List.of(option("Lecture", 1, DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));
        List<ClassOption> fridayCombo = List.of(option("Lecture", 1, DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));

        var settings = minimalSettings("COMP1234");

        assertTrue((boolean) invokePrivate("prefSatisfied", new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class}, mondayCombo, "monday", settings));
        assertTrue((boolean) invokePrivate("prefSatisfied", new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class}, tuesdayCombo, "tuesday", settings));
        assertTrue((boolean) invokePrivate("prefSatisfied", new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class}, wednesdayCombo, "wednesday", settings));
        assertTrue((boolean) invokePrivate("prefSatisfied", new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class}, thursdayCombo, "thursday", settings));
        assertTrue((boolean) invokePrivate("prefSatisfied", new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class}, fridayCombo, "friday", settings));
    }

    @Test @Order(51) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-51: prefSatisfied recognises compact and evenly spread")
    void tc_tg_51_prefSatisfiedCompactAndEvenlySpread() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley")
        );

        var settings = minimalSettings("COMP1234");

        assertTrue((boolean) invokePrivate("prefSatisfied", new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class}, combo, "compact", settings));
        assertTrue((boolean) invokePrivate("prefSatisfied", new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class}, combo, "evenly spread", settings));
    }

    @Test @Order(52) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-52: prefSatisfied returns false for unknown preference")
    void tc_tg_52_prefSatisfiedUnknownPreference() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley")
        );

        assertFalse((boolean) invokePrivate(
                "prefSatisfied",
                new Class<?>[]{List.class, String.class, TimetableGenerator.GenerationSettings.class},
                combo,
                "unknown preference",
                minimalSettings("COMP1234")));
    }

    @Test @Order(53) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-53: scoreCombo gives score for satisfied preferences")
    void tc_tg_53_scoreComboPositiveForSatisfiedPreferences() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley")
        );
        var settings = new TimetableGenerator.GenerationSettings(
                "", "1", List.of("COMP1234"), "", false, List.of("mornings", "monday"));

        int score = (int) invokePrivate(
                "scoreCombo",
                new Class<?>[]{List.class, TimetableGenerator.GenerationSettings.class},
                combo,
                settings);

        assertEquals(3, score);
    }

    @Test @Order(54) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-54: scoreCombo returns zero for no preferences")
    void tc_tg_54_scoreComboZeroForNoPreferences() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley")
        );
        var settings = minimalSettings("COMP1234");

        int score = (int) invokePrivate(
                "scoreCombo",
                new Class<?>[]{List.class, TimetableGenerator.GenerationSettings.class},
                combo,
                settings);

        assertEquals(0, score);
    }

    @Test @Order(55) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-55: findUnmetPreferences returns unmet preferences")
    void tc_tg_55_findUnmetPreferencesReturnsUnmet() throws Exception {
        List<ClassOption> chosen = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(14, 0), "Tonsley")
        );
        var settings = new TimetableGenerator.GenerationSettings(
                "", "1", List.of("COMP1234"), "", false, List.of("mornings", "monday"));

        Object result = invokePrivate(
                "findUnmetPreferences",
                new Class<?>[]{List.class, TimetableGenerator.GenerationSettings.class},
                chosen,
                settings);

        assertEquals(List.of("mornings"), result);
    }

    @Test @Order(56) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-56: hasHardConflict returns false when chosen is empty")
    void tc_tg_56_hasHardConflictFalseWhenChosenEmpty() throws Exception {
        List<ClassOption> combo = List.of(
                option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley")
        );
        Timetable timetable = new Timetable("Table", "TT001", "S1", false);

        boolean result = (boolean) invokePrivate(
                "hasHardConflict",
                new Class<?>[]{List.class, List.class, Timetable.class},
                List.of(),
                combo,
                timetable);

        assertFalse(result);
    }

    @Test @Order(57) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-57: hasHardConflict detects clash inside combo")
    void tc_tg_57_hasHardConflictDetectsInternalClash() throws Exception {
        List<ClassOption> combo = List.of(
                option("Tutorial", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"),
                option("Workshop", 1, DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley")
        );
        Timetable timetable = new Timetable("Table", "TT001", "S1", false);

        boolean result = (boolean) invokePrivate(
                "hasHardConflict",
                new Class<?>[]{List.class, List.class, Timetable.class},
                List.of(),
                combo,
                timetable);

        assertTrue(result);
    }

    @Test @Order(58) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-58: hasHardConflict detects clash with existing option")
    void tc_tg_58_hasHardConflictDetectsExistingClash() throws Exception {
        List<ClassOption> chosen = List.of(
                option("Tutorial", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley")
        );
        List<ClassOption> combo = List.of(
                option("Workshop", 1, DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley")
        );
        Timetable timetable = new Timetable("Table", "TT001", "S1", false);

        boolean result = (boolean) invokePrivate(
                "hasHardConflict",
                new Class<?>[]{List.class, List.class, Timetable.class},
                chosen,
                combo,
                timetable);

        assertTrue(result);
    }

    @Test @Order(59) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-59: filterBySemester returns all when semester is both")
    void tc_tg_59_filterBySemesterBothReturnsAll() throws Exception {
        List<Availability> availabilities = List.of(
                availability("Tonsley", "S1"),
                availability("Tonsley", "S2")
        );

        Object result = invokePrivate(
                "filterBySemester",
                new Class<?>[]{List.class, String.class},
                availabilities,
                "both");

        assertEquals(availabilities, result);
    }

    @Test @Order(60) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-60: filterBySemester filters S1")
    void tc_tg_60_filterBySemesterFiltersS1() throws Exception {
        Availability s1 = availability("Tonsley", "S1");
        Availability s2 = availability("Tonsley", "S2");

        Object result = invokePrivate(
                "filterBySemester",
                new Class<?>[]{List.class, String.class},
                List.of(s1, s2),
                "1");

        assertEquals(List.of(s1), result);
    }

    @Test @Order(61) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-61: campusAllowed accepts blank preference")
    void tc_tg_61_campusAllowedBlankPreference() throws Exception {
        Availability availability = availability("Tonsley", "S1");

        assertTrue((boolean) invokePrivate(
                "campusAllowed",
                new Class<?>[]{Availability.class, String.class},
                availability,
                ""));
    }

    @Test @Order(62) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-62: campusAllowed accepts city preference for city campus")
    void tc_tg_62_campusAllowedCityPreference() throws Exception {
        Availability availability = availability("Flinders City Campus", "S1");

        assertTrue((boolean) invokePrivate(
                "campusAllowed",
                new Class<?>[]{Availability.class, String.class},
                availability,
                "city"));
    }

    @Test @Order(63) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-63: campusAllowed rejects city preference for non-city campus")
    void tc_tg_63_campusAllowedRejectsWrongCityPreference() throws Exception {
        Availability availability = availability("Tonsley", "S1");

        assertFalse((boolean) invokePrivate(
                "campusAllowed",
                new Class<?>[]{Availability.class, String.class},
                availability,
                "city"));
    }

    @Test @Order(64) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-64: campusAllowed accepts matching non-city campus")
    void tc_tg_64_campusAllowedMatchingNonCity() throws Exception {
        Availability availability = availability("Tonsley", "S1");

        assertTrue((boolean) invokePrivate(
                "campusAllowed",
                new Class<?>[]{Availability.class, String.class},
                availability,
                "Tonsley"));
    }

    @Test @Order(65) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-65: buildClassTypeCombinations returns cartesian product")
    void tc_tg_65_buildClassTypeCombinationsReturnsCartesianProduct() throws Exception {
        Availability availability = availability("Tonsley", "S1");
        Topic topic = new Topic("COMP1234", "Programming");

        availability.addClassOption(option("Lecture", 1, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));
        availability.addClassOption(option("Lecture", 2, DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(12, 0), "Tonsley"));
        availability.addClassOption(option("Tutorial", 1, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));
        availability.addClassOption(option("Tutorial", 2, DayOfWeek.TUESDAY, LocalTime.of(11, 0), LocalTime.of(12, 0), "Tonsley"));

        Object result = invokePrivate(
                "buildClassTypeCombinations",
                new Class<?>[]{Availability.class, Topic.class},
                availability,
                topic);

        List<?> combinations = (List<?>) result;
        assertEquals(4, combinations.size());
    }

    @Test @Order(66) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-66: buildClassTypeCombinations returns empty when no options")
    void tc_tg_66_buildClassTypeCombinationsEmptyWhenNoOptions() throws Exception {
        Availability availability = availability("Tonsley", "S1");
        Topic topic = new Topic("COMP1234", "Programming");

        Object result = invokePrivate(
                "buildClassTypeCombinations",
                new Class<?>[]{Availability.class, Topic.class},
                availability,
                topic);

        assertEquals(List.of(), result);
    }

    @Test @Order(67) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-67: resolveName returns blank for auto name")
    void tc_tg_67_resolveNameReturnsBlankForAutoName() throws Exception {
        Object result = invokePrivate(
                "resolveName",
                new Class<?>[]{String.class, AppState.class},
                "",
                AppState.getInstance());

        assertEquals("", result);
    }

    @Test @Order(68) @Tag("Nguy1687") @Tag("additional")
    @DisplayName("TC-TG-68: resolveName trims custom name")
    void tc_tg_68_resolveNameTrimsCustomName() throws Exception {
        Object result = invokePrivate(
                "resolveName",
                new Class<?>[]{String.class, AppState.class},
                "  Custom Name  ",
                AppState.getInstance());

        assertEquals("Custom Name", result);
    }
}

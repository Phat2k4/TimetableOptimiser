import model.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("AbhiyanShakya")
class TimetableGeneratorTest {

    private static TimetableGenerator.GenerationSettings minimalSettings(String topicCode) {
        return new TimetableGenerator.GenerationSettings.Builder()
                .topicCodes(List.of(topicCode))
                .semester("1")
                .build();
    }

    private static void resetLastUsed() throws Exception {
        Field f = TimetableGenerator.class.getDeclaredField("lastUsedSettings");
        f.setAccessible(true);
        f.set(null, null);
    }

    //  1. GenerationSettings — construction & normalisation

    @Nested
    @DisplayName("GenerationSettings — construction")
    @Tag("Core")
    class GenerationSettingsConstructionTests {

        @Test
        @Tag("Critical")
        @DisplayName("null name is normalised to empty string")
        void nullNameBecomesEmpty() {
            var s = new TimetableGenerator.GenerationSettings(
                    null, "1", List.of("COMP1234"), "", false, List.of());
            assertEquals("", s.name);
        }

        @Test
        @Tag("Core")
        @DisplayName("name with surrounding whitespace is trimmed")
        void nameIsTrimmed() {
            var s = new TimetableGenerator.GenerationSettings(
                    "  MyTT  ", "1", List.of("X"), "", false, List.of());
            assertEquals("MyTT", s.name);
        }

        @Test
        @Tag("Critical")
        @DisplayName("null semester defaults to 'both'")
        void nullSemesterDefaultsBoth() {
            var s = new TimetableGenerator.GenerationSettings(
                    "T1", null, List.of("COMP1234"), "", false, List.of());
            assertEquals("both", s.semester);
        }

        @Test
        @Tag("Critical")
        @DisplayName("null topicCodes is treated as empty list")
        void nullTopicCodesBecomesEmpty() {
            var s = new TimetableGenerator.GenerationSettings(
                    "T1", "1", null, "", false, List.of());
            assertTrue(s.topicCodes.isEmpty());
        }

        @Test
        @Tag("Critical")
        @DisplayName("null preferenceOrder is treated as empty list")
        void nullPreferenceOrderBecomesEmpty() {
            var s = new TimetableGenerator.GenerationSettings(
                    "T1", "1", List.of("X"), "", false, null);
            assertTrue(s.preferenceOrder.isEmpty());
        }

        @Test
        @Tag("Critical")
        @DisplayName("null preferredCampus is normalised to empty string")
        void nullPreferredCampusBecomesEmpty() {
            var s = new TimetableGenerator.GenerationSettings(
                    "T1", "1", List.of("X"), null, false, List.of());
            assertEquals("", s.preferredCampus);
        }

        @Test
        @Tag("Core")
        @DisplayName("preferredCampus with surrounding whitespace is trimmed")
        void preferredCampusIsTrimmed() {
            var s = new TimetableGenerator.GenerationSettings(
                    "", "1", List.of("X"), "  Tonsley  ", false, List.of());
            assertEquals("Tonsley", s.preferredCampus);
        }
    }

    //  2. GenerationSettings — defensive copies (immutability)

    @Nested
    @DisplayName("GenerationSettings — immutability")
    @Tag("Core")
    class GenerationSettingsImmutabilityTests {

        @Test
        @Tag("Core")
        @DisplayName("mutating the original topicCodes list does not affect settings")
        void topicCodesDefensivelyCopied() {
            List<String> codes = new ArrayList<>(List.of("COMP1234"));
            var s = new TimetableGenerator.GenerationSettings(
                    "T1", "1", codes, "", false, List.of());
            codes.add("COMP9999");
            assertEquals(1, s.topicCodes.size());
        }

        @Test
        @Tag("Core")
        @DisplayName("topicCodes list on settings object is unmodifiable")
        void topicCodesUnmodifiable() {
            var s = new TimetableGenerator.GenerationSettings(
                    "T1", "1", new ArrayList<>(List.of("X")), "", false, List.of());
            assertThrows(UnsupportedOperationException.class,
                    () -> s.topicCodes.add("Y"));
        }

        @Test
        @Tag("Core")
        @DisplayName("mutating the original preferenceOrder list does not affect settings")
        void preferenceOrderDefensivelyCopied() {
            List<String> prefs = new ArrayList<>(List.of("mornings"));
            var s = new TimetableGenerator.GenerationSettings(
                    "T1", "1", List.of("X"), "", false, prefs);
            prefs.add("afternoons");
            assertEquals(1, s.preferenceOrder.size());
        }

        @Test
        @Tag("Core")
        @DisplayName("preferenceOrder list on settings object is unmodifiable")
        void preferenceOrderUnmodifiable() {
            var s = new TimetableGenerator.GenerationSettings(
                    "T1", "1", List.of("X"), "", false, new ArrayList<>(List.of("mornings")));
            assertThrows(UnsupportedOperationException.class,
                    () -> s.preferenceOrder.add("afternoons"));
        }
    }

    //  3. GenerationSettings.Builder

    @Nested
    @DisplayName("GenerationSettings.Builder")
    @Tag("Core")
    class BuilderTests {

        @Test
        @Tag("Core")
        @DisplayName("builder defaults: semester='both', no lecture overlap, empty lists")
        void builderDefaults() {
            var s = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of("X"))
                    .build();
            assertAll(
                    () -> assertEquals("both", s.semester),
                    () -> assertEquals("",     s.name),
                    () -> assertEquals("",     s.preferredCampus),
                    () -> assertFalse(s.allowLectureOverlap),
                    () -> assertTrue(s.preferenceOrder.isEmpty())
            );
        }

        @Test
        @Tag("Core")
        @DisplayName("toBuilder round-trip preserves all fields exactly")
        void toBuilderRoundTrip() {
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

        @Test
        @Tag("Core")
        @DisplayName("toBuilder partial override changes only the targeted field")
        void toBuilderPartialOverride() {
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

        @Test
        @Tag("Core")
        @DisplayName("allowLectureOverlap=true is preserved through builder")
        void lectureOverlapFlagPreserved() {
            var s = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of("X"))
                    .allowLectureOverlap(true)
                    .build();
            assertTrue(s.allowLectureOverlap);
        }
    }

    //  4. GenerationSettings.toString()

    @Nested
    @DisplayName("GenerationSettings.toString()")
    @Tag("Additional")
    class ToStringTests {

        @Test
        @Tag("Additional")
        @DisplayName("toString contains all major field values")
        void toStringContainsKeyFields() {
            var s = new TimetableGenerator.GenerationSettings(
                    "MyTT", "2", List.of("COMP3742"), "Bedford Park", true, List.of("mornings"));
            String str = s.toString();
            assertAll(
                    () -> assertTrue(str.contains("MyTT"),         "name missing"),
                    () -> assertTrue(str.contains("COMP3742"),     "topic code missing"),
                    () -> assertTrue(str.contains("Bedford Park"), "campus missing"),
                    () -> assertTrue(str.contains("true"),         "lectureOverlap flag missing"),
                    () -> assertTrue(str.contains("mornings"),     "preference missing")
            );
        }

        @Test
        @Tag("Additional")
        @DisplayName("toString on default-built settings does not throw")
        void toStringDoesNotThrow() {
            var s = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of("X"))
                    .build();
            assertDoesNotThrow(s::toString);
        }
    }

    //  5. GenerationResult — value-object contract

    @Nested
    @DisplayName("GenerationResult — value-object contract")
    @Tag("Core")
    class GenerationResultTests {

        @Test
        @Tag("Critical")
        @DisplayName("fail() result: success=false, timetable=null, errorMessage set")
        void failResultShape() {
            var result = TimetableGenerator.GenerationResult.fail("something went wrong");
            assertAll(
                    () -> assertFalse(result.success),
                    () -> assertNull(result.timetable),
                    () -> assertEquals("something went wrong", result.errorMessage),
                    () -> assertTrue(result.unmetPreferences.isEmpty())
            );
        }

        @Test
        @Tag("Core")
        @DisplayName("ok() result: success=true, errorMessage=null")
        void okResultShape() {
            var result = TimetableGenerator.GenerationResult.ok(null, List.of());
            assertAll(
                    () -> assertTrue(result.success),
                    () -> assertNull(result.errorMessage),
                    () -> assertTrue(result.unmetPreferences.isEmpty())
            );
        }

        @Test
        @Tag("Core")
        @DisplayName("ok() result carries unmet preferences supplied to it")
        void okResultCarriesUnmetPrefs() {
            var result = TimetableGenerator.GenerationResult.ok(null, List.of("mornings", "monday"));
            assertEquals(List.of("mornings", "monday"), result.unmetPreferences);
        }

        @Test
        @Tag("Core")
        @DisplayName("unmetPreferences list is unmodifiable")
        void unmetPreferencesUnmodifiable() {
            var result = TimetableGenerator.GenerationResult.ok(
                    null, new ArrayList<>(List.of("mornings")));
            assertThrows(UnsupportedOperationException.class,
                    () -> result.unmetPreferences.add("afternoons"));
        }

        @Test
        @Tag("Critical")
        @DisplayName("fail() result has empty (not null) unmetPreferences")
        void failResultUnmetPrefsNotNull() {
            var result = TimetableGenerator.GenerationResult.fail("err");
            assertNotNull(result.unmetPreferences);
            assertTrue(result.unmetPreferences.isEmpty());
        }
    }

    //  6. generate() — early-exit paths (no AppState required)

    @Nested
    @DisplayName("generate() — early-exit validation")
    @Tag("Critical")
    class GenerateEarlyExitTests {

        private final TimetableGenerator generator = new TimetableGenerator();

        @Test
        @Tag("Critical")
        @DisplayName("null settings throws NullPointerException immediately")
        void nullSettingsThrowsNPE() {
            assertThrows(NullPointerException.class, () -> generator.generate(null));
        }

        @Test
        @Tag("Critical")
        @DisplayName("empty topicCodes list returns failure without touching AppState")
        void emptyTopicCodesReturnsFail() {
            var settings = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of())
                    .semester("1")
                    .build();
            var result = generator.generate(settings);
            assertFalse(result.success);
            assertNotNull(result.errorMessage);
        }

        @Test
        @Tag("Critical")
        @DisplayName("empty topicCodes error message mentions 'topic'")
        void emptyTopicCodesErrorMentionsTopic() {
            var settings = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of())
                    .build();
            var result = generator.generate(settings);
            assertTrue(result.errorMessage.toLowerCase().contains("topic"),
                    "Expected error to mention 'topic', got: " + result.errorMessage);
        }

        @Test
        @Tag("Critical")
        @DisplayName("empty topicCodes result has null timetable")
        void emptyTopicCodesNullTimetable() {
            var settings = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of())
                    .build();
            var result = generator.generate(settings);
            assertNull(result.timetable);
        }
    }

    //  7. lastUsedSettings static field

    @Nested
    @DisplayName("lastUsedSettings")
    @Tag("Core")
    class LastUsedSettingsTests {

        @BeforeEach
        void clearLastUsed() throws Exception {
            resetLastUsed();
        }

        @Test
        @Tag("Core")
        @DisplayName("getLastUsedSettings() returns null before any generate() call")
        void initiallyNull() {
            assertNull(TimetableGenerator.getLastUsedSettings());
        }

        @Test
        @Tag("Core")
        @DisplayName("generate() stores settings even when it fails (e.g. empty topics)")
        void settingsStoredOnFailure() {
            var settings = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of())
                    .semester("1")
                    .build();
            new TimetableGenerator().generate(settings);
            assertSame(settings, TimetableGenerator.getLastUsedSettings());
        }
    }

    //  8. Preference ordering — settings contract

    @Nested
    @DisplayName("Preference ordering — settings contract")
    @Tag("Additional")
    class PreferenceOrderingTests {

        @Test
        @Tag("Additional")
        @DisplayName("preferenceOrder is stored in insertion order")
        void insertionOrderPreserved() {
            var prefs = List.of("mornings", "bedford park", "monday");
            var s = new TimetableGenerator.GenerationSettings(
                    "", "1", List.of("X"), "", false, prefs);
            assertEquals(prefs, s.preferenceOrder);
        }

        @Test
        @Tag("Additional")
        @DisplayName("multiple distinct preferences are all stored")
        void allPreferencesStored() {
            var prefs = List.of("mornings", "afternoons", "monday", "compact", "evenly spread");
            var s = new TimetableGenerator.GenerationSettings(
                    "", "1", List.of("X"), "", false, prefs);
            assertEquals(5, s.preferenceOrder.size());
        }

        @Test
        @Tag("Additional")
        @DisplayName("empty preferenceOrder is valid")
        void emptyPrefsValid() {
            var s = new TimetableGenerator.GenerationSettings(
                    "", "1", List.of("X"), "", false, List.of());
            assertTrue(s.preferenceOrder.isEmpty());
        }
    }

    //  9. Semester settings contract

    @Nested
    @DisplayName("Semester settings contract")
    @Tag("Additional")
    class SemesterTests {

        @Test
        @Tag("Additional")
        @DisplayName("semester '1' is stored verbatim")
        void semester1() {
            assertEquals("1", minimalSettings("COMP1234").semester);
        }

        @Test
        @Tag("Additional")
        @DisplayName("semester '2' is stored verbatim")
        void semester2() {
            var s = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of("X")).semester("2").build();
            assertEquals("2", s.semester);
        }

        @Test
        @Tag("Additional")
        @DisplayName("semester 'both' is stored verbatim")
        void semesterBoth() {
            var s = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of("X")).semester("both").build();
            assertEquals("both", s.semester);
        }

        @Test
        @Tag("Additional")
        @DisplayName("semester defaults to 'both' when not set via builder")
        void semesterDefaultBoth() {
            var s = new TimetableGenerator.GenerationSettings.Builder()
                    .topicCodes(List.of("X")).build();
            assertEquals("both", s.semester);
        }
    }
}
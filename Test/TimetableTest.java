package model;

import org.junit.jupiter.api.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TimetableTest {

    private Timetable timetable() {
        return new Timetable(" My Timetable ", " TT001 ", " S1 ", false);
    }

    private Topic topic(String code, String name) {
        return new Topic(code, name);
    }

    private ClassSession session(LocalTime start, LocalTime end, String building) {
        return new ClassSession(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 6, 1),
                DayOfWeek.MONDAY,
                start,
                end,
                new Location(building, "Room 1")
        );
    }

    private ClassOption option(String className, LocalTime start, LocalTime end, String building) {
        return new ClassOption(
                new ClassOffering(className, 1),
                session(start, end, building)
        );
    }

    private TimetableEntry entry(String code, String name, ClassOption option) {
        TimetableEntry entry = new TimetableEntry(topic(code, name));
        entry.addChosenOption(option);
        return entry;
    }

    @Test
    @Order(1)
    @DisplayName("Timetable - constructor trims and sets default values")
    @Tag("binn0049")
    @Tag("core")
    void tc001_constructorShouldSetValues() {
        Timetable timetable = timetable();

        assertEquals("My Timetable", timetable.getTimetableName());
        assertEquals("TT001", timetable.getTimetableId());
        assertEquals("S1", timetable.getSemesterN());
        assertFalse(timetable.isAllowLectureOverlap());
        assertTrue(timetable.getEntries().isEmpty());
        assertTrue(timetable.getPreferences().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("Timetable - constructor rejects null timetable name")
    @Tag("binn0049")
    @Tag("core")
    void tc002_constructorShouldRejectNullName() {
        assertThrows(NullPointerException.class,
                () -> new Timetable(null, "TT001", "S1", false));
    }

    @Test
    @Order(3)
    @DisplayName("Timetable - constructor rejects null timetable id")
    @Tag("binn0049")
    @Tag("core")
    void tc003_constructorShouldRejectNullId() {
        assertThrows(NullPointerException.class,
                () -> new Timetable("Table", null, "S1", false));
    }

    @Test
    @Order(4)
    @DisplayName("Timetable - constructor rejects null semester")
    @Tag("binn0049")
    @Tag("core")
    void tc004_constructorShouldRejectNullSemester() {
        assertThrows(NullPointerException.class,
                () -> new Timetable("Table", "TT001", null, false));
    }

    @Test
    @Order(5)
    @DisplayName("Timetable - generateAutoName returns correct format")
    @Tag("binn0049")
    @Tag("core")
    void tc005_generateAutoNameShouldReturnCorrectFormat() {
        assertEquals("Timetable_5", Timetable.generateAutoName(5));
    }

    @Test
    @Order(6)
    @DisplayName("Timetable - addEntry adds timetable entry")
    @Tag("binn0049")
    @Tag("core")
    void tc006_addEntryShouldAddEntry() {
        Timetable timetable = timetable();
        TimetableEntry entry = new TimetableEntry(topic("COMP1001", "Programming"));

        timetable.addEntry(entry);

        assertEquals(1, timetable.getEntries().size());
        assertTrue(timetable.getEntries().contains(entry));
    }

    @Test
    @Order(7)
    @DisplayName("Timetable - addEntry rejects null")
    @Tag("binn0049")
    @Tag("core")
    void tc007_addEntryShouldRejectNull() {
        Timetable timetable = timetable();

        assertThrows(NullPointerException.class,
                () -> timetable.addEntry(null));
    }

    @Test
    @Order(8)
    @DisplayName("Timetable - removeEntry removes existing entry")
    @Tag("binn0049")
    @Tag("core")
    void tc008_removeEntryShouldRemoveEntry() {
        Timetable timetable = timetable();
        TimetableEntry entry = new TimetableEntry(topic("COMP1001", "Programming"));

        timetable.addEntry(entry);

        assertTrue(timetable.removeEntry(entry));
        assertTrue(timetable.getEntries().isEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("Timetable - removeEntry returns false for missing entry")
    @Tag("binn0049")
    @Tag("core")
    void tc009_removeEntryShouldReturnFalseForMissingEntry() {
        Timetable timetable = timetable();
        TimetableEntry entry = new TimetableEntry(topic("COMP1001", "Programming"));

        assertFalse(timetable.removeEntry(entry));
    }

    @Test
    @Order(10)
    @DisplayName("Timetable - findEntryForTopic returns matching entry")
    @Tag("binn0049")
    @Tag("core")
    void tc010_findEntryForTopicShouldReturnMatchingEntry() {
        Timetable timetable = timetable();
        Topic topic = topic("COMP1001", "Programming");
        TimetableEntry entry = new TimetableEntry(topic);

        timetable.addEntry(entry);

        assertEquals(entry, timetable.findEntryForTopic(new Topic("comp1001", "Other Name")));
    }

    @Test
    @Order(11)
    @DisplayName("Timetable - findEntryForTopic returns null when topic missing")
    @Tag("binn0049")
    @Tag("core")
    void tc011_findEntryForTopicShouldReturnNullWhenMissing() {
        Timetable timetable = timetable();

        assertNull(timetable.findEntryForTopic(topic("COMP9999", "Missing")));
    }

    @Test
    @Order(12)
    @DisplayName("Timetable - addPreference adds preference")
    @Tag("binn0049")
    @Tag("core")
    void tc012_addPreferenceShouldAddPreference() {
        Timetable timetable = timetable();
        Preference preference = new Preference(Preference.TYPE_NO_MORNINGS, 1);

        timetable.addPreference(preference);

        assertEquals(1, timetable.getPreferences().size());
        assertTrue(timetable.getPreferences().contains(preference));
    }

    @Test
    @Order(13)
    @DisplayName("Timetable - addPreference rejects null")
    @Tag("binn0049")
    @Tag("core")
    void tc013_addPreferenceShouldRejectNull() {
        Timetable timetable = timetable();

        assertThrows(NullPointerException.class,
                () -> timetable.addPreference(null));
    }

    @Test
    @Order(14)
    @DisplayName("Timetable - removePreference removes existing preference")
    @Tag("binn0049")
    @Tag("core")
    void tc014_removePreferenceShouldRemovePreference() {
        Timetable timetable = timetable();
        Preference preference = new Preference(Preference.TYPE_NO_MORNINGS, 1);

        timetable.addPreference(preference);

        assertTrue(timetable.removePreference(preference));
        assertTrue(timetable.getPreferences().isEmpty());
    }

    @Test
    @Order(15)
    @DisplayName("Timetable - resolveSpreadVsCompact returns null when neither exists")
    @Tag("binn0049")
    @Tag("core")
    void tc015_resolveSpreadVsCompactShouldReturnNullWhenNeitherExists() {
        Timetable timetable = timetable();

        assertNull(timetable.resolveSpreadVsCompact());
    }

    @Test
    @Order(16)
    @DisplayName("Timetable - resolveSpreadVsCompact returns compact only")
    @Tag("binn0049")
    @Tag("core")
    void tc016_resolveSpreadVsCompactShouldReturnCompactOnly() {
        Timetable timetable = timetable();
        Preference compact = new Preference(Preference.TYPE_COMPACT, 1);

        timetable.addPreference(compact);

        assertEquals(compact, timetable.resolveSpreadVsCompact());
    }

    @Test
    @Order(17)
    @DisplayName("Timetable - resolveSpreadVsCompact returns evenly spread only")
    @Tag("binn0049")
    @Tag("core")
    void tc017_resolveSpreadVsCompactShouldReturnSpreadOnly() {
        Timetable timetable = timetable();
        Preference spread = new Preference(Preference.TYPE_EVENLY_SPREAD, 1);

        timetable.addPreference(spread);

        assertEquals(spread, timetable.resolveSpreadVsCompact());
    }

    @Test
    @Order(18)
    @DisplayName("Timetable - resolveSpreadVsCompact returns higher priority winner")
    @Tag("binn0049")
    @Tag("core")
    void tc018_resolveSpreadVsCompactShouldReturnHigherPriorityWinner() {
        Timetable timetable = timetable();
        Preference spread = new Preference(Preference.TYPE_EVENLY_SPREAD, 2);
        Preference compact = new Preference(Preference.TYPE_COMPACT, 1);

        timetable.addPreference(spread);
        timetable.addPreference(compact);

        assertEquals(compact, timetable.resolveSpreadVsCompact());
    }

    @Test
    @Order(19)
    @DisplayName("Timetable - getEffectivePreferences sorts by priority and excludes lower spread compact")
    @Tag("binn0049")
    @Tag("core")
    void tc019_getEffectivePreferencesShouldSortAndRemoveLowerConflict() {
        Timetable timetable = timetable();

        Preference noFridays = new Preference(Preference.TYPE_NO_FRIDAYS, 3);
        Preference spread = new Preference(Preference.TYPE_EVENLY_SPREAD, 2);
        Preference compact = new Preference(Preference.TYPE_COMPACT, 1);

        timetable.addPreference(noFridays);
        timetable.addPreference(spread);
        timetable.addPreference(compact);

        assertEquals(2, timetable.getEffectivePreferences().size());
        assertEquals(compact, timetable.getEffectivePreferences().get(0));
        assertEquals(noFridays, timetable.getEffectivePreferences().get(1));
        assertFalse(timetable.getEffectivePreferences().contains(spread));
    }

    @Test
    @Order(20)
    @DisplayName("Timetable - entries list is unmodifiable")
    @Tag("binn0049")
    @Tag("core")
    void tc020_entriesListShouldBeUnmodifiable() {
        Timetable timetable = timetable();

        assertThrows(UnsupportedOperationException.class,
                () -> timetable.getEntries().add(new TimetableEntry(topic("COMP1001", "Programming"))));
    }

    @Test
    @Order(21)
    @DisplayName("Timetable - preferences list is unmodifiable")
    @Tag("binn0049")
    @Tag("core")
    void tc021_preferencesListShouldBeUnmodifiable() {
        Timetable timetable = timetable();

        assertThrows(UnsupportedOperationException.class,
                () -> timetable.getPreferences().add(new Preference(Preference.TYPE_NO_MORNINGS, 1)));
    }

    @Test
    @Order(22)
    @DisplayName("Timetable - effective preferences list is unmodifiable")
    @Tag("binn0049")
    @Tag("core")
    void tc022_effectivePreferencesListShouldBeUnmodifiable() {
        Timetable timetable = timetable();
        timetable.addPreference(new Preference(Preference.TYPE_NO_MORNINGS, 1));

        assertThrows(UnsupportedOperationException.class,
                () -> timetable.getEffectivePreferences().add(new Preference(Preference.TYPE_NO_FRIDAYS, 2)));
    }

    @Test
    @Order(23)
    @DisplayName("Timetable - setTimetableName trims and updates name")
    @Tag("binn0049")
    @Tag("core")
    void tc023_setTimetableNameShouldTrimAndUpdateName() {
        Timetable timetable = timetable();

        timetable.setTimetableName(" New Name ");

        assertEquals("New Name", timetable.getTimetableName());
    }

    @Test
    @Order(24)
    @DisplayName("Timetable - setTimetableName rejects null")
    @Tag("binn0049")
    @Tag("core")
    void tc024_setTimetableNameShouldRejectNull() {
        Timetable timetable = timetable();

        assertThrows(NullPointerException.class,
                () -> timetable.setTimetableName(null));
    }

    @Test
    @Order(25)
    @DisplayName("Timetable - setAllowLectureOverlap updates flag")
    @Tag("binn0049")
    @Tag("core")
    void tc025_setAllowLectureOverlapShouldUpdateFlag() {
        Timetable timetable = timetable();

        timetable.setAllowLectureOverlap(true);

        assertTrue(timetable.isAllowLectureOverlap());
    }

    @Test
    @Order(26)
    @DisplayName("Timetable - recomputeWarnings sets clash warning for same campus overlap")
    @Tag("binn0049")
    @Tag("core")
    void tc026_recomputeWarningsShouldSetClashWarning() {
        Timetable timetable = timetable();

        TimetableEntry entry1 = entry("COMP1001", "Programming",
                option("Tutorial", LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));

        TimetableEntry entry2 = entry("COMP2001", "Database",
                option("Workshop", LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley"));

        timetable.addEntry(entry1);
        timetable.addEntry(entry2);

        timetable.recomputeWarnings();

        assertTrue(entry1.isClashWarning());
        assertTrue(entry2.isClashWarning());
        assertFalse(entry1.isCommuteWarning());
        assertFalse(entry2.isCommuteWarning());
    }

    @Test
    @Order(27)
    @DisplayName("Timetable - recomputeWarnings sets commute warning for different campus short gap")
    @Tag("binn0049")
    @Tag("core")
    void tc027_recomputeWarningsShouldSetCommuteWarning() {
        Timetable timetable = timetable();

        TimetableEntry entry1 = entry("COMP1001", "Programming",
                option("Tutorial", LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));

        TimetableEntry entry2 = entry("COMP2001", "Database",
                option("Workshop", LocalTime.of(10, 10), LocalTime.of(11, 0), "Bedford Park"));

        timetable.addEntry(entry1);
        timetable.addEntry(entry2);

        timetable.recomputeWarnings();

        assertTrue(entry1.isCommuteWarning());
        assertTrue(entry2.isCommuteWarning());
        assertFalse(entry1.isClashWarning());
        assertFalse(entry2.isClashWarning());
    }

    @Test
    @Order(28)
    @DisplayName("Timetable - recomputeWarnings clears old warnings when no clash remains")
    @Tag("binn0049")
    @Tag("core")
    void tc028_recomputeWarningsShouldClearOldWarnings() {
        Timetable timetable = timetable();

        TimetableEntry entry1 = entry("COMP1001", "Programming",
                option("Tutorial", LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));

        TimetableEntry entry2 = entry("COMP2001", "Database",
                option("Workshop", LocalTime.of(11, 0), LocalTime.of(12, 0), "Tonsley"));

        entry1.setClashWarning(true);
        entry2.setCommuteWarning(true);

        timetable.addEntry(entry1);
        timetable.addEntry(entry2);

        timetable.recomputeWarnings();

        assertFalse(entry1.isClashWarning());
        assertFalse(entry2.isClashWarning());
        assertFalse(entry1.isCommuteWarning());
        assertFalse(entry2.isCommuteWarning());
    }

    @Test
    @Order(29)
    @DisplayName("Timetable - recomputeWarnings allows lecture overlap when flag enabled")
    @Tag("binn0049")
    @Tag("core")
    void tc029_recomputeWarningsShouldAllowLectureOverlap() {
        Timetable timetable = new Timetable("Table", "TT002", "S1", true);

        TimetableEntry entry1 = entry("COMP1001", "Programming",
                option("Lecture", LocalTime.of(9, 0), LocalTime.of(10, 0), "Tonsley"));

        TimetableEntry entry2 = entry("COMP2001", "Database",
                option("Lecture", LocalTime.of(9, 30), LocalTime.of(10, 30), "Tonsley"));

        timetable.addEntry(entry1);
        timetable.addEntry(entry2);

        timetable.recomputeWarnings();

        assertFalse(entry1.isClashWarning());
        assertFalse(entry2.isClashWarning());
    }

    @Test
    @Order(30)
    @DisplayName("Timetable - equals returns true for same timetable id")
    @Tag("binn0049")
    @Tag("core")
    void tc030_equalsShouldReturnTrueForSameId() {
        Timetable t1 = new Timetable("One", "TT001", "S1", false);
        Timetable t2 = new Timetable("Two", "TT001", "S2", true);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    @Order(31)
    @DisplayName("Timetable - equals returns false for different timetable id")
    @Tag("binn0049")
    @Tag("core")
    void tc031_equalsShouldReturnFalseForDifferentId() {
        Timetable t1 = new Timetable("One", "TT001", "S1", false);
        Timetable t2 = new Timetable("Two", "TT002", "S1", false);

        assertNotEquals(t1, t2);
    }

    @Test
    @Order(32)
    @DisplayName("Timetable - equals returns true when compared to itself")
    @Tag("binn0049")
    @Tag("core")
    void tc032_equalsShouldReturnTrueForSameObject() {
        Timetable timetable = timetable();

        assertEquals(timetable, timetable);
    }

    @Test
    @Order(33)
    @DisplayName("Timetable - equals returns false for different object type")
    @Tag("binn0049")
    @Tag("core")
    void tc033_equalsShouldReturnFalseForDifferentType() {
        Timetable timetable = timetable();

        assertNotEquals(timetable, "not a timetable");
    }

    @Test
    @Order(34)
    @DisplayName("Timetable - toString contains name id semester and entry count")
    @Tag("binn0049")
    @Tag("core")
    void tc034_toStringShouldContainMainDetails() {
        Timetable timetable = timetable();
        timetable.addEntry(new TimetableEntry(topic("COMP1001", "Programming")));

        String text = timetable.toString();

        assertTrue(text.contains("My Timetable"));
        assertTrue(text.contains("TT001"));
        assertTrue(text.contains("S1"));
        assertTrue(text.contains("1 entries"));
    }
}

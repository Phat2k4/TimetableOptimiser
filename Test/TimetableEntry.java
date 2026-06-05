package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimetableEntryTest {

    private Topic topic() {
        return new Topic("COMP1001", "Programming");
    }

    private ClassSession session() {
        return new ClassSession(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 6, 1),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                new Location("Tonsley", "Room 1")
        );
    }

    private ClassOption option() {
        ClassOffering offering = new ClassOffering("Lecture", 1);
        return new ClassOption(offering, session());
    }

    @Test
    @Order(1)
    @DisplayName("TimetableEntry - constructor sets topic and default warning values")
    @Tag("binn0049")
    @Tag("core")
    void constructorShouldSetTopicAndDefaultValues() {
        Topic topic = topic();
        TimetableEntry entry = new TimetableEntry(topic);

        assertEquals(topic, entry.getTopic());
        assertFalse(entry.isClashWarning());
        assertFalse(entry.isCommuteWarning());
        assertTrue(entry.getChosenOptions().isEmpty());
        assertTrue(entry.getAllSessions().isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("TimetableEntry - throws exception for null object")
    @Tag("binn0049")
    @Tag("core")
    void constructorShouldRejectNullTopic() {
        assertThrows(NullPointerException.class, () -> new TimetableEntry(null));
    }

    @Test
    @Order(3)
    @DisplayName("TimetableEntry - addchosenoption adds a valid option")
    @Tag("binn0049")
    @Tag("core")
    void addChosenOptionShouldAddOption() {
        TimetableEntry entry = new TimetableEntry(topic());
        ClassOption option = option();

        entry.addChosenOption(option);

        assertEquals(1, entry.getChosenOptions().size());
        assertTrue(entry.getChosenOptions().contains(option));
    }

    @Test
    @Order(4)
    @DisplayName("TimetableEntry - addchosen option throws exception for null option")
    @Tag("binn0049")
    @Tag("core")
    void addChosenOptionShouldRejectNullOption() {
        TimetableEntry entry = new TimetableEntry(topic());

        assertThrows(NullPointerException.class, () -> entry.addChosenOption(null));
    }

    @Test
    @Order(5)
    @DisplayName("TimetableEntry - removeChosenOption removes existing option")
    @Tag("binn0049")
    @Tag("core")
    void removeChosenOptionShouldRemoveExistingOption() {
        TimetableEntry entry = new TimetableEntry(topic());
        ClassOption option = option();

        entry.addChosenOption(option);

        assertTrue(entry.removeChosenOption(option));
        assertTrue(entry.getChosenOptions().isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("TimetableEntry - removeChosenOption returns false for missing option")
    @Tag("binn0049")
    @Tag("core")
    void removeChosenOptionShouldReturnFalseWhenOptionDoesNotExist() {
        TimetableEntry entry = new TimetableEntry(topic());

        assertFalse(entry.removeChosenOption(option()));
    }

    @Test
    @Order(7)
    @DisplayName("TimetableEntry - getAllSessions returns sessions from chosen options")
    @Tag("binn0049")
    @Tag("core")
    void getAllSessionsShouldReturnSessionsFromChosenOptions() {
        TimetableEntry entry = new TimetableEntry(topic());
        ClassOption option = option();

        entry.addChosenOption(option);

        List<ClassSession> sessions = entry.getAllSessions();

        assertEquals(1, sessions.size());
        assertEquals(option.getClassSession(), sessions.get(0));
    }

    @Test
    @Order(8)
    @DisplayName("TimetableEntry - TimetableEntry - getChosenOptions returns unmodifiable list")
    @Tag("binn0049")
    @Tag("core")
    void getChosenOptionsShouldBeUnmodifiable() {
        TimetableEntry entry = new TimetableEntry(topic());
        ClassOption option = option();

        entry.addChosenOption(option);

        assertThrows(UnsupportedOperationException.class,
                () -> entry.getChosenOptions().add(option));
    }

    @Test
    @Order(9)
    @DisplayName("TimetableEntry - TimetableEntry - getAllSessions returns unmodifiable list")
    @Tag("binn0049")
    @Tag("core")
    void getAllSessionsShouldBeUnmodifiable() {
        TimetableEntry entry = new TimetableEntry(topic());
        ClassOption option = option();

        entry.addChosenOption(option);

        assertThrows(UnsupportedOperationException.class,
                () -> entry.getAllSessions().add(option.getClassSession()));
    }

    @Test
    @Order(10)
    @DisplayName("TimetableEntry -clash warning can be set")
    @Tag("binn0049")
    @Tag("core")
    void setClashWarningShouldUpdateValue() {
        TimetableEntry entry = new TimetableEntry(topic());

        entry.setClashWarning(true);

        assertTrue(entry.isClashWarning());
    }

    @Test
    @Order(11)
    @DisplayName("TimetableEntry - commute warning can be set")
    @Tag("binn0049")
    @Tag("core")
    void setCommuteWarningShouldUpdateValue() {
        TimetableEntry entry = new TimetableEntry(topic());

        entry.setCommuteWarning(true);

        assertTrue(entry.isCommuteWarning());
    }

    @Test
    @Order(12)
    @DisplayName("TimetableEntry -  equals returns true for same topic and options")
    @Tag("binn0049")
    @Tag("core")
    void sameEntriesShouldBeEqualAndHaveSameHashCode() {
        TimetableEntry entry1 = new TimetableEntry(topic());
        TimetableEntry entry2 = new TimetableEntry(topic());

        ClassOption option = option();

        entry1.addChosenOption(option);
        entry2.addChosenOption(option);

        assertEquals(entry1, entry2);
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    @Order(13)
    @DisplayName("TimetableEntry - equals returns false for different topics")
    @Tag("binn0049")
    @Tag("core")
    void entriesWithDifferentTopicsShouldNotBeEqual() {
        TimetableEntry entry1 = new TimetableEntry(new Topic("COMP1001", "Programming"));
        TimetableEntry entry2 = new TimetableEntry(new Topic("COMP2001", "Database"));

        assertNotEquals(entry1, entry2);
    }

    @Test
    @Order(14)
    @DisplayName("TimetableEntry - equals returns false for different options")
    @Tag("binn0049")
    @Tag("core")
    void entriesWithDifferentOptionsShouldNotBeEqual() {
        TimetableEntry entry1 = new TimetableEntry(topic());
        TimetableEntry entry2 = new TimetableEntry(topic());

        entry1.addChosenOption(option());

        assertNotEquals(entry1, entry2);
    }

    @Test
    @Order(15)
    @DisplayName("TimetableEntry - toString contains topic code, option count and warnings")
    @Tag("binn0049")
    @Tag("core")
    void entryShouldEqualItself() {
        TimetableEntry entry = new TimetableEntry(topic());

        assertEquals(entry, entry);
    }

    @Test
    @Order(16)
    @DisplayName("TimetableEntry - equals returns true when compared to itself")
    @Tag("binn0049")
    @Tag("core")
    void entryShouldNotEqualDifferentObjectType() {
        TimetableEntry entry = new TimetableEntry(topic());

        assertNotEquals(entry, "not an entry");
    }

    @Test
    @Order(17)
    @DisplayName("TimetableEntry - equals returns false for different object type")
    @Tag("binn0049")
    @Tag("core")
    void toStringShouldContainTopicCodeOptionsAndWarnings() {
        TimetableEntry entry = new TimetableEntry(topic());

        entry.setClashWarning(true);
        entry.setCommuteWarning(true);

        String text = entry.toString();

        assertTrue(text.contains("COMP1001"));
        assertTrue(text.contains("options=0"));
        assertTrue(text.contains("clash=true"));
        assertTrue(text.contains("commute=true"));
    }
}

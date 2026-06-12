import model.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;


class AppStateTest {

    private AppState app;

    @BeforeEach
    void setup() throws Exception {
        Files.deleteIfExists(Path.of("appstate.json"));

        Field instance = AppState.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        app = AppState.getInstance();
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(Path.of("appstate.json"));

        Field instance = AppState.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    private ClassOption option() {
        return new ClassOption(
                new ClassOffering("Lecture", 1),
                new ClassSession(
                        LocalDate.of(2026, 2, 23),
                        LocalDate.of(2026, 6, 1),
                        DayOfWeek.MONDAY,
                        LocalTime.of(9, 0),
                        LocalTime.of(11, 0),
                        new Location("Bedford", "101")
                )
        );
    }

    @Test
    void addFindAndListTopic() {
        Topic topic = new Topic("comp101", "Programming");

        app.addTopic(topic);

        assertTrue(app.findTopic("COMP101").isPresent());
        assertEquals(1, app.allTopics().size());
    }

    @Test
    void addNullTopicThrowsException() {
        assertThrows(NullPointerException.class, () -> app.addTopic(null));
    }

    @Test
    void timetableMethodsWork() {
        Timetable tt = new Timetable("My Timetable", "TT-1", "S1", false);

        app.addTimetable(tt);

        assertTrue(app.findTimetable("TT-1").isPresent());
        assertEquals(1, app.allTimetables().size());
        assertTrue(app.removeTimetable("TT-1"));
        assertFalse(app.removeTimetable("missing"));
    }

    @Test
    void addNullTimetableThrowsException() {
        assertThrows(NullPointerException.class, () -> app.addTimetable(null));
    }

    @Test
    void nextTimetableIdAndNameWork() {
        assertEquals("TT-1", app.nextTimetableId());
        assertNotNull(app.nextAutoName());
    }

    @Test
    void saveAndLoadFullState() throws Exception {
        Topic topic = new Topic("COMP101", "Programming");
        Availability av = new Availability("Internal", "Bedford", "S1", "1");
        av.addClassOption(option());
        topic.addAvailability(av);
        app.addTopic(topic);

        Timetable tt = new Timetable("Main", "TT-1", "S1", true);
        tt.addPreference(new Preference("Morning", 1));

        TimetableEntry entry = new TimetableEntry(topic);
        entry.setClashWarning(true);
        entry.setCommuteWarning(true);
        entry.addChosenOption(option());
        tt.addEntry(entry);

        app.addTimetable(tt);
        app.save();

        Field instance = AppState.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        AppState loaded = AppState.getInstance();

        assertTrue(loaded.findTopic("COMP101").isPresent());
        assertTrue(loaded.findTimetable("TT-1").isPresent());
        assertEquals(1, loaded.allTopics().size());
        assertEquals(1, loaded.allTimetables().size());
    }

    @Test
    void loadWithInvalidJsonDoesNotCrash() throws Exception {
        Files.writeString(Path.of("appstate.json"), "{ bad json");

        assertDoesNotThrow(() -> app.load());
    }

    @Test
    @Order(200)
    @Tag("Dulina")
    @Tag("Core")
    @DisplayName("Corrupted app state should recover automatically")
    void CorruptedAppStateShouldRecoverAutomatically() throws Exception {
        Files.writeString(Path.of("appstate.json"), "{ bad json");

        String output = captureOutput(() -> {
            app.load();
        });

        assertTrue(output.contains("State recovered"),
                "Expected corrupted state to recover automatically, but actual output was: " + output);
    }

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(outputStream));
            action.run();
        } finally {
            System.setOut(originalOut);
        }

        return outputStream.toString();
    }
}

import model.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class TimetableServiceTest {

    @BeforeEach
    void resetBefore() throws Exception {
        resetAppState();
    }

    @AfterEach
    void resetAfter() throws Exception {
        resetAppState();
        Files.deleteIfExists(Path.of("appstate.json"));
    }

    private void resetAppState() throws Exception {
        Field instance = AppState.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    private ClassOption makeOption(String name, int instance, String start, String end) {
        return new ClassOption(
                new ClassOffering(name, instance),
                new ClassSession(
                        LocalDate.of(2026, 2, 23),
                        LocalDate.of(2026, 6, 1),
                        DayOfWeek.MONDAY,
                        LocalTime.parse(start),
                        LocalTime.parse(end),
                        new Location("Bedford", "101")
                )
        );
    }

    private void makeTimetable() {
        Topic topic = new Topic("COMP101", "Programming");

        Availability availability =
                new Availability("Internal", "Bedford", "S1", "1");

        ClassOption lecture1 = makeOption("Lecture", 1, "09:00", "10:00");
        ClassOption lecture2 = makeOption("Lecture", 2, "11:00", "12:00");

        availability.addClassOption(lecture1);
        availability.addClassOption(lecture2);
        topic.addAvailability(availability);

        TimetableEntry entry = new TimetableEntry(topic);
        entry.addChosenOption(lecture1);

        Timetable timetable =
                new Timetable("Test Timetable", "TT-1", "S1", false);

        timetable.addPreference(new Preference("Morning", 1));
        timetable.addEntry(entry);

        AppState.getInstance().addTopic(topic);
        AppState.getInstance().addTimetable(timetable);
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void createNullSettings() {
        TimetableService service = new TimetableService(new Scanner(""));

        assertThrows(
                NullPointerException.class,
                () -> service.create(null)
        );
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void viewExistingTimetable() {
        makeTimetable();

        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.view("TT-1"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void viewMissingTimetable() {
        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.view("missing"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void browseAllWithTimetable() {
        makeTimetable();

        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(service::browseAll);
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void browseAllEmpty() {
        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(service::browseAll);
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void deleteConfirmed() {
        makeTimetable();

        TimetableService service = new TimetableService(new Scanner("y\n"));

        service.delete("TT-1");

        assertTrue(AppState.getInstance().findTimetable("TT-1").isEmpty());
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void deleteCancelled() {
        makeTimetable();

        TimetableService service = new TimetableService(new Scanner("n\n"));

        service.delete("TT-1");

        assertTrue(AppState.getInstance().findTimetable("TT-1").isPresent());
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void deleteMissingTimetable() {
        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.delete("missing"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void exportTxt() {
        makeTimetable();

        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.export("TT-1", "txt"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void exportCsv() {
        makeTimetable();

        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.export("TT-1", "csv"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void exportBadFormat() {
        makeTimetable();

        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.export("TT-1", "pdf"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void exportMissingTimetable() {
        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.export("missing", "txt"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void exportNullFormatDefaultsToTxt() {
        makeTimetable();

        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.export("TT-1", null));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void editMissingTimetable() {
        TimetableService service = new TimetableService(new Scanner(""));

        assertDoesNotThrow(() -> service.edit("missing"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void editExistingTimetableSwapOption() {
        makeTimetable();

        String input =
                "1\n" +   // select topic
                        "1\n" +   // select current option
                        "1\n";    // select replacement

        TimetableService service = new TimetableService(new Scanner(input));

        assertDoesNotThrow(() -> service.edit("TT-1"));
    }

    @Test
    @Tag("DavidHarms")
    @Tag("core")
    void constructorNullScannerThrows() {
        assertThrows(
                NullPointerException.class,
                () -> new TimetableService(null)
        );
    }
}
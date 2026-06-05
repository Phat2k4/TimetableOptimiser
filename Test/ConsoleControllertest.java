import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Console controller tests")
class ConsoleControllerTest {

    private final InputStream originalInput = System.in;
    private final PrintStream originalOutput = System.out;
    private ByteArrayOutputStream captureOutputStream;

    @BeforeEach
    void SetUp() {
        captureOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captureOutputStream));
    }

    @AfterEach
    void TearDown() {
        System.setIn(originalInput);
        System.setOut(originalOutput);
    }

    private void ProvideInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    @Order(235)
    @DisplayName("Create console controller")
    @Tag("dass0027")
    @Tag("critical")
    void CreateConsoleController() {
        ProvideInput("");

        ConsoleController controller = new ConsoleController();

        assertNotNull(controller);
    }

    @Test
    @Order(236)
    @DisplayName("Display show menu prints menu")
    @Tag("dass0027")
    @Tag("critical")
    void DisplayShowMenuPrintsMenu() {
        ProvideInput("");
        ConsoleController controller = new ConsoleController();

        controller.showMenu();

        String output = captureOutputStream.toString();

        assertAll("main menu output",
                () -> assertTrue(output.contains("MAIN MENU")),
                () -> assertTrue(output.contains("CLASS DATA")),
                () -> assertTrue(output.contains("TIMETABLES")),
                () -> assertTrue(output.contains("Import classes from CSV")),
                () -> assertTrue(output.contains("Browse all classes")),
                () -> assertTrue(output.contains("Generate new timetable")),
                () -> assertTrue(output.contains("Export a timetable")),
                () -> assertTrue(output.contains("Exit"))
        );
    }

    @Test
    @Order(237)
    @DisplayName("Display unknown command prints error")
    @Tag("dass0027")
    @Tag("core")
    void DisplayUnknownCommandPrintsError() {
        ProvideInput("");
        ConsoleController controller = new ConsoleController();

        controller.dispatch(99);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Unknown command: 99"));
    }

    @Test
    @Order(238)
    @DisplayName("Read command returns valid choice")
    @Tag("dass0027")
    @Tag("critical")
    void ReadCommandReturnsValidChoice() {
        ProvideInput("7" + System.lineSeparator());
        ConsoleController controller = new ConsoleController();

        int result = controller.readCommand();

        assertEquals(7, result);
    }

    @Test
    @Order(239)
    @DisplayName("Read command accepts lower boundary")
    @Tag("dass0027")
    @Tag("core")
    void ReadCommandAcceptsLowerBoundary() {
        ProvideInput("1" + System.lineSeparator());
        ConsoleController controller = new ConsoleController();

        int result = controller.readCommand();

        assertEquals(1, result);
    }

    @Test
    @Order(240)
    @DisplayName("Read command accepts upper boundary")
    @Tag("dass0027")
    @Tag("core")
    void ReadCommandAcceptsUpperBoundary() {
        ProvideInput("13" + System.lineSeparator());
        ConsoleController controller = new ConsoleController();

        int result = controller.readCommand();

        assertEquals(13, result);
    }

    @Test
    @Order(241)
    @DisplayName("Read command handles non numeric input")
    @Tag("dass0027")
    @Tag("core")
    void ReadCommandHandlesNonNumericInput() {
        ProvideInput(
                "abc" + System.lineSeparator() +
                        "3" + System.lineSeparator()
        );

        ConsoleController controller = new ConsoleController();

        int result = controller.readCommand();

        String output = captureOutputStream.toString();

        assertAll("non numeric then valid",
                () -> assertEquals(3, result),
                () -> assertTrue(output.contains("not a valid number"))
        );
    }

    @Test
    @Order(242)
    @DisplayName("Read command handles out of range input")
    @Tag("dass0027")
    @Tag("core")
    void ReadCommandHandlesOutOfRangeInput() {
        ProvideInput(
                "14" + System.lineSeparator() +
                        "4" + System.lineSeparator()
        );

        ConsoleController controller = new ConsoleController();

        int result = controller.readCommand();

        String output = captureOutputStream.toString();

        assertAll("out of range then valid",
                () -> assertEquals(4, result),
                () -> assertTrue(output.contains("out of range"))
        );
    }

    @Test
    @Order(243)
    @DisplayName("Display import classes can be cancelled")
    @Tag("dass0027")
    @Tag("core")
    void DisplayImportClassesCanBeCancelled() {
        ProvideInput("cancel" + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(1);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Import Classes from CSV"));
    }

    @Test
    @Order(244)
    @DisplayName("Display import missing file shows error")
    @Tag("dass0027")
    @Tag("core")
    void DisplayImportMissingFileShowsError() {
        ProvideInput(
                "missing-file.csv" + System.lineSeparator() +
                        "cancel" + System.lineSeparator()
        );

        ConsoleController controller = new ConsoleController();

        controller.dispatch(1);

        String output = captureOutputStream.toString();

        assertAll("missing file then cancel",
                () -> assertTrue(output.contains("Import Classes from CSV")),
                () -> assertTrue(output.contains("File not found"))
        );
    }

    @Test
    @Order(245)
    @DisplayName("Display import empty file reaches importer")
    @Tag("dass0027")
    @Tag("additional")
    void DisplayImportEmptyFileReachesImporter() throws Exception {
        Path tempFile = Files.createTempFile("empty-console-import", ".csv");

        ProvideInput(tempFile.toString() + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(1);

        String output = captureOutputStream.toString();

        assertAll("empty file import",
                () -> assertTrue(output.contains("Import Classes from CSV")),
                () -> assertTrue(output.contains("Importing from")),
                () -> assertTrue(output.contains("CSV file is empty") || output.contains("no header row"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(246)
    @DisplayName("Display browse classes prints section")
    @Tag("dass0027")
    @Tag("core")
    void DisplayBrowseClassesPrintsSection() {
        ProvideInput("");

        ConsoleController controller = new ConsoleController();

        controller.dispatch(2);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Browse All Classes"));
    }

    @Test
    @Order(247)
    @DisplayName("Display view topic can be cancelled")
    @Tag("dass0027")
    @Tag("core")
    void DisplayViewTopicCanBeCancelled() {
        ProvideInput("cancel" + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(3);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("View Classes by Topic Code"));
    }

    @Test
    @Order(248)
    @DisplayName("Display search prompts criteria")
    @Tag("dass0027")
    @Tag("core")
    void DisplaySearchPromptsCriteria() {
        ProvideInput(
                System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator()
        );

        ConsoleController controller = new ConsoleController();

        controller.dispatch(4);

        String output = captureOutputStream.toString();

        assertAll("search prompts",
                () -> assertTrue(output.contains("Search Classes")),
                () -> assertTrue(output.contains("Enter search criteria")),
                () -> assertTrue(output.contains("Topic code")),
                () -> assertTrue(output.contains("Topic name")),
                () -> assertTrue(output.contains("Attendance mode")),
                () -> assertTrue(output.contains("Campus")),
                () -> assertTrue(output.contains("Semester")),
                () -> assertTrue(output.contains("Class type")),
                () -> assertTrue(output.contains("Room"))
        );
    }

    @Test
    @Order(249)
    @DisplayName("Display edit class can be cancelled")
    @Tag("dass0027")
    @Tag("core")
    void DisplayEditClassCanBeCancelled() {
        ProvideInput("cancel" + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(5);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Edit Class Record"));
    }

    @Test
    @Order(250)
    @DisplayName("Display delete class can be cancelled")
    @Tag("dass0027")
    @Tag("core")
    void DisplayDeleteClassCanBeCancelled() {
        ProvideInput("cancel" + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(6);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Delete Class Record"));
    }

    @Test
    @Order(251)
    @DisplayName("Display browse timetables prints section")
    @Tag("dass0027")
    @Tag("core")
    void DisplayBrowseTimetablesPrintsSection() {
        ProvideInput("");

        ConsoleController controller = new ConsoleController();

        controller.dispatch(8);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Browse All Timetables"));
    }

    @Test
    @Order(252)
    @DisplayName("Display search with topic code completes")
    @Tag("dass0027")
    @Tag("additional")
    void DisplaySearchWithTopicCodeCompletes() {
        ProvideInput(
                "COMP1102" + System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator() +
                        System.lineSeparator()
        );

        ConsoleController controller = new ConsoleController();

        controller.dispatch(4);

        String output = captureOutputStream.toString();

        assertAll("search with topic code",
                () -> assertTrue(output.contains("Search Classes")),
                () -> assertTrue(output.contains("Topic code"))
        );
    }

    @Test
    @Order(253)
    @DisplayName("Display generate timetable can be cancelled")
    @Tag("dass0027")
    @Tag("critical")
    void DisplayGenerateTimetableCanBeCancelled() {
        ProvideInput(
                System.lineSeparator() +
                        "both" + System.lineSeparator() +
                        "cancel" + System.lineSeparator()
        );

        ConsoleController controller = new ConsoleController();

        controller.dispatch(7);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Generate New Timetable"));
    }

    @Test
    @Order(254)
    @DisplayName("Display view timetable can be cancelled")
    @Tag("dass0027")
    @Tag("core")
    void DisplayViewTimetableCanBeCancelled() {
        ProvideInput("cancel" + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(9);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("View Timetable"));
    }

    @Test
    @Order(255)
    @DisplayName("Display edit timetable can be cancelled")
    @Tag("dass0027")
    @Tag("core")
    void DisplayEditTimetableCanBeCancelled() {
        ProvideInput("cancel" + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(10);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Edit Timetable"));
    }

    @Test
    @Order(256)
    @DisplayName("Display delete timetable can be cancelled")
    @Tag("dass0027")
    @Tag("core")
    void DisplayDeleteTimetableCanBeCancelled() {
        ProvideInput("cancel" + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(11);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Delete Timetable"));
    }

    @Test
    @Order(257)
    @DisplayName("Display export timetable can be cancelled")
    @Tag("dass0027")
    @Tag("core")
    void DisplayExportTimetableCanBeCancelled() {
        ProvideInput("cancel" + System.lineSeparator());

        ConsoleController controller = new ConsoleController();

        controller.dispatch(12);

        String output = captureOutputStream.toString();

        assertTrue(output.contains("Export Timetable"));
    }
}

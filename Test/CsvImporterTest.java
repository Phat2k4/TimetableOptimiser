import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Csv importer tests")
class CsvImporterTest {

    private final PrintStream originalOutput = System.out;
    private ByteArrayOutputStream captureOutputStream;

    @BeforeEach
    void SetUp() {
        captureOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captureOutputStream));
    }

    @AfterEach
    void TearDown() {
        System.setOut(originalOutput);
    }

    private Path CreateTempCsv(String content) throws Exception {
        Path tempFile = Files.createTempFile("csv-importer-test", ".csv");
        Files.writeString(tempFile, content);
        return tempFile;
    }

    private String Header() {
        return "Topic,Availability,Class,Class instance,Date,Day,Time,Location\n";
    }

    private String ValidRow(String topicCode) {
        return topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n";
    }

    private String UniqueTopicCode() {
        return "DAS" + Long.toString(System.nanoTime()).substring(0, 8);
    }

    @Test
    @Order(258)
    @DisplayName("Display split csv standard line")
    @Tag("nguy1687")
    @Tag("critical")
    void DisplaySplitCsvStandardLine() {
        String[] result = CsvImporter.splitCsv(
                "COMP1102 Programming,On Campus - City - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,Festival Tower"
        );

        assertAll("standard csv split",
                () -> assertEquals(8, result.length),
                () -> assertEquals("COMP1102 Programming", result[0]),
                () -> assertEquals("Festival Tower", result[7])
        );
    }

    @Test
    @Order(259)
    @DisplayName("Display split csv quoted comma")
    @Tag("nguy1687")
    @Tag("critical")
    void DisplaySplitCsvQuotedComma() {
        String[] result = CsvImporter.splitCsv("COMP1102,\"Festival Tower, Level 8\",101");

        assertAll("quoted comma",
                () -> assertEquals(3, result.length),
                () -> assertEquals("Festival Tower, Level 8", result[1])
        );
    }

    @Test
    @Order(260)
    @DisplayName("Display split csv escaped quote")
    @Tag("nguy1687")
    @Tag("core")
    void DisplaySplitCsvEscapedQuote() {
        String[] result = CsvImporter.splitCsv("COMP1102,\"Room \"\"801\"\"\",S1");

        assertAll("escaped quote",
                () -> assertEquals(3, result.length),
                () -> assertEquals("Room \"801\"", result[1])
        );
    }

    @Test
    @Order(261)
    @DisplayName("Display split csv empty line")
    @Tag("nguy1687")
    @Tag("additional")
    void DisplaySplitCsvEmptyLine() {
        String[] result = CsvImporter.splitCsv("");

        assertAll("empty line",
                () -> assertEquals(1, result.length),
                () -> assertEquals("", result[0])
        );
    }

    @Test
    @Order(262)
    @DisplayName("Display split csv trailing comma")
    @Tag("nguy1687")
    @Tag("core")
    void DisplaySplitCsvTrailingComma() {
        String[] result = CsvImporter.splitCsv("COMP1102,Lecture,");

        assertAll("trailing comma",
                () -> assertEquals(3, result.length),
                () -> assertEquals("", result[2])
        );
    }

    @Test
    @Order(263)
    @DisplayName("Display import missing file returns false")
    @Tag("nguy1687")
    @Tag("critical")
    void DisplayImportMissingFileReturnsFalse() {
        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile("missing-file.csv");

        String output = captureOutputStream.toString();

        assertAll("missing file",
                () -> assertFalse(result),
                () -> assertTrue(output.contains("File not found"))
        );
    }

    @Test
    @Order(264)
    @DisplayName("Display import directory returns false")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportDirectoryReturnsFalse() throws Exception {
        Path tempDirectory = Files.createTempDirectory("csv-importer-directory");

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempDirectory.toString());

        String output = captureOutputStream.toString();

        assertAll("directory path",
                () -> assertFalse(result),
                () -> assertTrue(output.contains("not a regular file"))
        );

        Files.deleteIfExists(tempDirectory);
    }

    @Test
    @Order(265)
    @DisplayName("Display import empty file returns false")
    @Tag("nguy1687")
    @Tag("critical")
    void DisplayImportEmptyFileReturnsFalse() throws Exception {
        Path tempFile = CreateTempCsv("");

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("empty file",
                () -> assertFalse(result),
                () -> assertTrue(output.contains("CSV file is empty") || output.contains("no header row"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(266)
    @DisplayName("Display import blank header returns false")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportBlankHeaderReturnsFalse() throws Exception {
        Path tempFile = CreateTempCsv("\n");

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("blank header",
                () -> assertFalse(result),
                () -> assertTrue(output.contains("CSV file is empty") || output.contains("no header row"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(267)
    @DisplayName("Display import missing header column returns false")
    @Tag("nguy1687")
    @Tag("critical")
    void DisplayImportMissingHeaderColumnReturnsFalse() throws Exception {
        Path tempFile = CreateTempCsv(
                "Topic,Availability,Class,Class instance,Date,Day,Time\n" +
                        "COMP1102 Programming,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("missing header",
                () -> assertFalse(result),
                () -> assertTrue(output.contains("missing required column")),
                () -> assertTrue(output.contains("location"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(268)
    @DisplayName("Display import valid csv imports one record")
    @Tag("nguy1687")
    @Tag("critical")
    void DisplayImportValidCsvImportsOneRecord() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(Header() + ValidRow(topicCode));

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("valid csv",
                () -> assertTrue(result),
                () -> assertEquals(1, importer.getImported()),
                () -> assertEquals(0, importer.getUpdated()),
                () -> assertEquals(0, importer.getSkipped()),
                () -> assertTrue(output.contains("Import complete"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(269)
    @DisplayName("Display import csv ignores blank row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportCsvIgnoresBlankRow() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        "\n" +
                        ValidRow(topicCode)
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        assertAll("blank row ignored",
                () -> assertTrue(result),
                () -> assertEquals(1, importer.getImported()),
                () -> assertEquals(0, importer.getSkipped())
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(270)
    @DisplayName("Display import room header alias works")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportRoomHeaderAliasWorks() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                "Topic,Availability,Class,Class instance,Date,Day,Time,Room\n" +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("room alias",
                () -> assertTrue(result),
                () -> assertEquals(1, importer.getImported()),
                () -> assertTrue(output.contains("recognised as 'location'"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(271)
    @DisplayName("Display import venue header alias works")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportVenueHeaderAliasWorks() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                "Topic,Availability,Class,Class instance,Date,Day,Time,Venue\n" +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        assertAll("venue alias",
                () -> assertTrue(result),
                () -> assertEquals(1, importer.getImported()),
                () -> assertEquals(0, importer.getSkipped())
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(272)
    @DisplayName("Display import missing topic skips row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportMissingTopicSkipsRow() throws Exception {
        Path tempFile = CreateTempCsv(
                Header() +
                        ",On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("missing topic",
                () -> assertTrue(result),
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(1, importer.getSkipped()),
                () -> assertTrue(output.contains("Missing required field 'Topic'"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(273)
    @DisplayName("Display import missing location skips row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportMissingLocationSkipsRow() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("missing location",
                () -> assertTrue(result),
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(1, importer.getSkipped()),
                () -> assertTrue(output.contains("Missing required field 'Location'"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(274)
    @DisplayName("Display import invalid instance skips row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportInvalidInstanceSkipsRow() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,abc,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("invalid instance",
                () -> assertTrue(result),
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(1, importer.getSkipped()),
                () -> assertTrue(output.contains("Class instance is not a valid integer"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(275)
    @DisplayName("Display import invalid date format skips row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportInvalidDateFormatSkipsRow() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,bad date,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("invalid date format",
                () -> assertTrue(result),
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(1, importer.getSkipped()),
                () -> assertTrue(output.contains("Invalid Date format"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(276)
    @DisplayName("Display import invalid date value skips row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportInvalidDateValueSkipsRow() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,99 Mar - 31 Oct,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("invalid date value",
                () -> assertTrue(result),
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(1, importer.getSkipped()),
                () -> assertTrue(output.contains("Cannot parse date"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(277)
    @DisplayName("Display import invalid time format skips row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportInvalidTimeFormatSkipsRow() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,bad time,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("invalid time format",
                () -> assertTrue(result),
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(1, importer.getSkipped()),
                () -> assertTrue(output.contains("Invalid Time format"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(278)
    @DisplayName("Display import invalid time value skips row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportInvalidTimeValueSkipsRow() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,25:00 - 26:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("invalid time value",
                () -> assertTrue(result),
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(1, importer.getSkipped()),
                () -> assertTrue(output.contains("Cannot parse time"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(279)
    @DisplayName("Display import invalid day skips row")
    @Tag("nguy1687")
    @Tag("core")
    void DisplayImportInvalidDaySkipsRow() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,NOTDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        String output = captureOutputStream.toString();

        assertAll("invalid day",
                () -> assertTrue(result),
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(1, importer.getSkipped()),
                () -> assertTrue(output.contains("Unknown day value"))
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(280)
    @DisplayName("Display import day with qualifier works")
    @Tag("nguy1687")
    @Tag("additional")
    void DisplayImportDayWithQualifierWorks() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,\"MONDAY (once-only)\",09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        assertAll("day qualifier",
                () -> assertTrue(result),
                () -> assertEquals(1, importer.getImported()),
                () -> assertEquals(0, importer.getSkipped())
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(281)
    @DisplayName("Display import year wrap date works")
    @Tag("nguy1687")
    @Tag("additional")
    void DisplayImportYearWrapDateWorks() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,31 Dec - 2 Jan,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        assertAll("year wrap",
                () -> assertTrue(result),
                () -> assertEquals(1, importer.getImported()),
                () -> assertEquals(0, importer.getSkipped())
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(282)
    @DisplayName("Display import duplicate row updates record")
    @Tag("nguy1687")
    @Tag("critical")
    void DisplayImportDuplicateRowUpdatesRecord() throws Exception {
        String topicCode = UniqueTopicCode();
        Path tempFile = CreateTempCsv(
                Header() +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n" +
                        topicCode + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,12:00 - 14:00,\"Bedford Park, Room 2\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean result = importer.importFile(tempFile.toString());

        assertAll("duplicate update",
                () -> assertTrue(result),
                () -> assertEquals(1, importer.getImported()),
                () -> assertEquals(1, importer.getUpdated()),
                () -> assertEquals(0, importer.getSkipped())
        );

        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(283)
    @DisplayName("Display import resets counters between files")
    @Tag("nguy1687")
    @Tag("additional")
    void DisplayImportResetsCountersBetweenFiles() throws Exception {
        Path firstFile = CreateTempCsv(
                Header() +
                        "RESETONE" + System.nanoTime() + " Test Topic,On Campus - Bedford Park - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,\"Bedford Park, Room 1\"\n"
        );

        Path secondFile = CreateTempCsv(
                Header() +
                        "RESETTWO" + System.nanoTime() + " Test Topic,On Campus - Bedford Park - S1 - 2,Tutorial,2,4 Mar - 1 Nov,TUESDAY,12:00 - 14:00,\"Tonsley, Room 2\"\n"
        );

        CsvImporter importer = new CsvImporter();

        boolean firstResult = importer.importFile(firstFile.toString());
        assertTrue(firstResult);
        assertEquals(1, importer.getImported());

        boolean secondResult = importer.importFile(secondFile.toString());

        assertAll("counters reset",
                () -> assertTrue(secondResult),
                () -> assertEquals(1, importer.getImported() + importer.getUpdated()),
                () -> assertEquals(0, importer.getSkipped())
        );

        Files.deleteIfExists(firstFile);
        Files.deleteIfExists(secondFile);
    }

    @Test
    @Order(283)
    @DisplayName("Display importer counters start at zero")
    @Tag("nguy1687")
    @Tag("additional")
    void DisplayImporterCountersStartAtZero() {
        CsvImporter importer = new CsvImporter();

        assertAll("initial counters",
                () -> assertEquals(0, importer.getImported()),
                () -> assertEquals(0, importer.getUpdated()),
                () -> assertEquals(0, importer.getSkipped())
        );
    }
}

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("CsvImporter Tests")
//does the program correctly split each line by commas?
// What if a field has a comma inside quotes — does it handle that correctly?
class CsvImporterTest {

    @Test
    @Order(47)
    @DisplayName("TC-47: splitCsv — standard comma-separated line splits correctly")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc47_splitCsv_standardLine_splitsCorrectly() {
        String[] result = CsvImporter.splitCsv(
                "COMP1102 Programming,On Campus - City - S1 - 1,Lecture,1,3 Mar - 31 Oct,MONDAY,09:00 - 11:00,Festival Tower");
        assertAll("standard split",
                () -> assertEquals(8,                      result.length),
                () -> assertEquals("COMP1102 Programming", result[0]),
                () -> assertEquals("Festival Tower",       result[7])
        );
    }

    @Test
    @Order(48)
    @DisplayName("TC-48: splitCsv — quoted field containing comma is not split")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc48_splitCsv_quotedFieldWithComma_notSplit() {
        String[] result = CsvImporter.splitCsv("COMP1102,\"Festival Tower, Level 8\",101");
        assertAll("quoted comma field",
                () -> assertEquals(3,                         result.length),
                () -> assertEquals("Festival Tower, Level 8", result[1])
        );
    }

    @Test
    @Order(49)
    @DisplayName("TC-49: splitCsv — escaped double quotes inside quoted field handled correctly")
    @Tag("Nguy1687")
    @Tag("core")
    void tc49_splitCsv_escapedQuotes_handledCorrectly() {
        String[] result = CsvImporter.splitCsv("COMP1102,\"Room \"\"801\"\"\",S1");
        assertAll("escaped quotes",
                () -> assertEquals(3,              result.length),
                () -> assertEquals("Room \"801\"", result[1])
        );
    }

    @RepeatedTest(value = 3, name = "TC-50 — repeat {currentRepetition}/{totalRepetitions}: splitCsv empty line")
    @Order(50)
    @DisplayName("TC-50: splitCsv — empty string returns single empty element (repeated)")
    @Tag("Nguy1687")
    @Tag("additional")
    void tc50_splitCsv_emptyString_returnsSingleEmptyElement() {
        String[] result = CsvImporter.splitCsv("");
        assertAll("empty string split",
                () -> assertEquals(1,  result.length),
                () -> assertEquals("", result[0])
        );
    }
}

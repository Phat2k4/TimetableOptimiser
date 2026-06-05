import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("InputValidator Tests")
// user input is correct. For example — if a student types a topic code like COMP1102,
// we check the format is right. If they type something wrong, the program should give an error
class InputValidatorTest {

    // ── validateMenuChoice ────────────────────────────────────────────────────

    @Test @Order(42) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-42: validateMenuChoice — valid input within range returns integer")
    void tc42_validateMenuChoice_validInput_returnsInteger()
            throws InputValidator.ValidationException {
        assertAll("valid menu choices",
                () -> assertEquals(1,  InputValidator.validateMenuChoice("1",  1, 13)),
                () -> assertEquals(7,  InputValidator.validateMenuChoice("7",  1, 13)),
                () -> assertEquals(13, InputValidator.validateMenuChoice("13", 1, 13))
        );
    }

    @Test @Order(43) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-43: validateMenuChoice — out of range throws ValidationException")
    void tc43_validateMenuChoice_outOfRange_throwsValidationException() {
        assertAll("out of range inputs",
                () -> assertThrows(InputValidator.ValidationException.class,
                        () -> InputValidator.validateMenuChoice("0",  1, 13)),
                () -> assertThrows(InputValidator.ValidationException.class,
                        () -> InputValidator.validateMenuChoice("14", 1, 13))
        );
    }

    @Test @Order(44) @Tag("Nguy1687") @Tag("critical")
    @DisplayName("TC-44: validateTopicCode — valid codes are normalised to upper case")
    void tc44_validateTopicCode_validCodes_normalisedToUpperCase()
            throws InputValidator.ValidationException {
        assertAll("valid topic codes",
                () -> assertEquals("COMP1234", InputValidator.validateTopicCode("comp1234")),
                () -> assertEquals("NURS101",  InputValidator.validateTopicCode("nurs101")),
                () -> assertEquals("COMP1102", InputValidator.validateTopicCode("COMP1102"))
        );
    }

    @ParameterizedTest(name = "TC-45 [{index}]: invalid topic code ''{0}'' should throw")
    @Order(45) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-45: validateTopicCode — invalid codes throw ValidationException")
    @ValueSource(strings = { "", "1234COMP", "C1", "TOOLONGCODE1234", "COMP", "   " })
    void tc45_validateTopicCode_invalidCodes_throwValidationException(String code) {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateTopicCode(code));
    }

    @Test @Order(46) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-46: validateSemester — valid inputs return normalised values")
    void tc46_validateSemester_validInputs_returnNormalisedValues()
            throws InputValidator.ValidationException {
        assertAll("valid semester inputs",
                () -> assertEquals("1",    InputValidator.validateSemester("1")),
                () -> assertEquals("2",    InputValidator.validateSemester("2")),
                () -> assertEquals("both", InputValidator.validateSemester("both")),
                () -> assertEquals("both", InputValidator.validateSemester("BOTH")),
                () -> assertEquals("both", InputValidator.validateSemester("12"))
        );
    }

    // ── validateMenuChoice extra branches ─────────────────────────────────────

    @Test @Order(47) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-47: validateMenuChoice — null input throws ValidationException")
    void tc_iv_47_menuChoice_null_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateMenuChoice(null, 1, 13));
    }

    @Test @Order(48) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-48: validateMenuChoice — blank input throws ValidationException")
    void tc_iv_48_menuChoice_blank_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateMenuChoice("   ", 1, 13));
    }

    @Test @Order(49) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-49: validateMenuChoice — non-numeric input throws ValidationException")
    void tc_iv_49_menuChoice_nonNumeric_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateMenuChoice("abc", 1, 13));
    }

    // ── validateFilePath ──────────────────────────────────────────────────────

    @Test @Order(50) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-50: validateFilePath — null path throws ValidationException")
    void tc_iv_50_filePath_null_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateFilePath(null));
    }

    @Test @Order(51) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-51: validateFilePath — blank path throws ValidationException")
    void tc_iv_51_filePath_blank_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateFilePath("   "));
    }

    @Test @Order(52) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-52: validateFilePath — non-existent file throws ValidationException")
    void tc_iv_52_filePath_nonExistent_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateFilePath("does_not_exist_xyz.csv"));
    }

    @Test @Order(53) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-53: validateFilePath — valid existing file returns path")
    void tc_iv_53_filePath_validFile_returnsPath() throws Exception {
        Path tmp = Files.createTempFile("test_", ".csv");
        try {
            String result = InputValidator.validateFilePath(tmp.toString());
            assertEquals(tmp.toString(), result);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    // ── validateName ──────────────────────────────────────────────────────────

    @Test @Order(54) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-54: validateName — null input returns empty string")
    void tc_iv_54_name_null_returnsEmpty() throws Exception {
        assertEquals("", InputValidator.validateName(null));
    }

    @Test @Order(55) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-55: validateName — blank input returns empty string")
    void tc_iv_55_name_blank_returnsEmpty() throws Exception {
        assertEquals("", InputValidator.validateName("   "));
    }

    @Test @Order(56) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-56: validateName — name longer than 60 chars throws ValidationException")
    void tc_iv_56_name_tooLong_throws() {
        String longName = "A".repeat(61);
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateName(longName));
    }

    // ── validateExistingTimetableName ─────────────────────────────────────────

    @Test @Order(57) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-57: validateExistingTimetableName — blank throws ValidationException")
    void tc_iv_57_existingName_blank_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateExistingTimetableName("   "));
    }

    @Test @Order(58) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-58: validateExistingTimetableName — null throws ValidationException")
    void tc_iv_58_existingName_null_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateExistingTimetableName(null));
    }

    @Test @Order(59) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-59: validateExistingTimetableName — valid input returns trimmed value")
    void tc_iv_59_existingName_valid_returnsTrimmed() throws Exception {
        assertEquals("TT-01", InputValidator.validateExistingTimetableName("  TT-01  "));
    }

    // ── validateNonBlank ──────────────────────────────────────────────────────

    @Test @Order(60) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-60: validateNonBlank — blank throws ValidationException")
    void tc_iv_60_nonBlank_blank_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateNonBlank("   ", "Search query"));
    }

    @Test @Order(61) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-61: validateNonBlank — null throws ValidationException")
    void tc_iv_61_nonBlank_null_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateNonBlank(null, "Field"));
    }

    @Test @Order(62) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-62: validateNonBlank — valid input returns trimmed value")
    void tc_iv_62_nonBlank_valid_returnsTrimmed() throws Exception {
        assertEquals("hello", InputValidator.validateNonBlank("  hello  ", "Field"));
    }

    // ── validateYesNo ─────────────────────────────────────────────────────────

    @Test @Order(63) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-63: validateYesNo — 'y' and 'yes' return true")
    void tc_iv_63_yesNo_yes_returnsTrue() throws Exception {
        assertAll("yes responses",
                () -> assertTrue(InputValidator.validateYesNo("y")),
                () -> assertTrue(InputValidator.validateYesNo("yes")),
                () -> assertTrue(InputValidator.validateYesNo("YES"))
        );
    }

    @Test @Order(64) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-64: validateYesNo — 'n' and 'no' return false")
    void tc_iv_64_yesNo_no_returnsFalse() throws Exception {
        assertAll("no responses",
                () -> assertFalse(InputValidator.validateYesNo("n")),
                () -> assertFalse(InputValidator.validateYesNo("no")),
                () -> assertFalse(InputValidator.validateYesNo("NO"))
        );
    }

    @Test @Order(65) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-65: validateYesNo — null throws ValidationException")
    void tc_iv_65_yesNo_null_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateYesNo(null));
    }

    @Test @Order(66) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-66: validateYesNo — invalid input throws ValidationException")
    void tc_iv_66_yesNo_invalid_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateYesNo("maybe"));
    }

    // ── validateSemester extra ────────────────────────────────────────────────

    @Test @Order(67) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-67: validateSemester — null throws ValidationException")
    void tc_iv_67_semester_null_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateSemester(null));
    }

    @Test @Order(68) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-68: validateSemester — invalid value throws ValidationException")
    void tc_iv_68_semester_invalid_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateSemester("3"));
    }

    // ── validateCampus ────────────────────────────────────────────────────────

    @Test @Order(69) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-69: validateCampus — blank/null returns empty string (no preference)")
    void tc_iv_69_campus_blank_returnsEmpty() throws Exception {
        assertAll("no preference",
                () -> assertEquals("", InputValidator.validateCampus(null)),
                () -> assertEquals("", InputValidator.validateCampus("   "))
        );
    }

    @Test @Order(70) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-70: validateCampus — Bedford Park variants return canonical name")
    void tc_iv_70_campus_bedfordPark() throws Exception {
        assertAll("Bedford Park",
                () -> assertEquals("Bedford Park", InputValidator.validateCampus("1")),
                () -> assertEquals("Bedford Park", InputValidator.validateCampus("bedford park")),
                () -> assertEquals("Bedford Park", InputValidator.validateCampus("bp"))
        );
    }

    @Test @Order(71) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-71: validateCampus — Tonsley variants return canonical name")
    void tc_iv_71_campus_tonsley() throws Exception {
        assertAll("Tonsley",
                () -> assertEquals("Tonsley", InputValidator.validateCampus("2")),
                () -> assertEquals("Tonsley", InputValidator.validateCampus("tonsley"))
        );
    }

    @Test @Order(72) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-72: validateCampus — City variants return canonical name")
    void tc_iv_72_campus_city() throws Exception {
        assertAll("City campus",
                () -> assertEquals("Flinders City Campus", InputValidator.validateCampus("3")),
                () -> assertEquals("Flinders City Campus", InputValidator.validateCampus("city")),
                () -> assertEquals("Flinders City Campus", InputValidator.validateCampus("fcc"))
        );
    }

    @Test @Order(73) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-73: validateCampus — 'none'/'any' returns empty string")
    void tc_iv_73_campus_none_returnsEmpty() throws Exception {
        assertAll("no campus",
                () -> assertEquals("", InputValidator.validateCampus("0")),
                () -> assertEquals("", InputValidator.validateCampus("none")),
                () -> assertEquals("", InputValidator.validateCampus("any"))
        );
    }

    @Test @Order(74) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-74: validateCampus — unknown campus throws ValidationException")
    void tc_iv_74_campus_unknown_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateCampus("mars"));
    }

    // ── validateExportFormat ──────────────────────────────────────────────────

    @Test @Order(75) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-75: validateExportFormat — 'txt' and 'text' return 'txt'")
    void tc_iv_75_exportFormat_txt() throws Exception {
        assertAll("txt format",
                () -> assertEquals("txt", InputValidator.validateExportFormat("txt")),
                () -> assertEquals("txt", InputValidator.validateExportFormat("text")),
                () -> assertEquals("txt", InputValidator.validateExportFormat("TXT"))
        );
    }

    @Test @Order(76) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-76: validateExportFormat — 'csv' returns 'csv'")
    void tc_iv_76_exportFormat_csv() throws Exception {
        assertEquals("csv", InputValidator.validateExportFormat("csv"));
    }

    @Test @Order(77) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-77: validateExportFormat — blank throws ValidationException")
    void tc_iv_77_exportFormat_blank_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateExportFormat("   "));
    }

    @Test @Order(78) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-78: validateExportFormat — unknown format throws ValidationException")
    void tc_iv_78_exportFormat_unknown_throws() {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateExportFormat("pdf"));
    }

    // ── ValidationException ───────────────────────────────────────────────────

    @Test @Order(79) @Tag("Nguy1687") @Tag("core")
    @DisplayName("TC-IV-79: ValidationException — message is stored correctly")
    void tc_iv_79_validationException_message() {
        InputValidator.ValidationException ex =
                new InputValidator.ValidationException("test error message");
        assertEquals("test error message", ex.getMessage());
    }
}

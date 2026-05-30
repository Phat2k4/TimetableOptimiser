import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("InputValidator Tests")
// user input is correct. For example — if a student types a topic code like COMP1102,
// we check the format is right. If they type something wrong, the program should give an error
class InputValidatorTest {

    @Test
    @Order(42)
    @DisplayName("TC-42: validateMenuChoice — valid input within range returns integer")
    @Tag("student2")
    @Tag("critical")
    void tc42_validateMenuChoice_validInput_returnsInteger()
            throws InputValidator.ValidationException {
        assertAll("valid menu choices",
                () -> assertEquals(1,  InputValidator.validateMenuChoice("1",  1, 13)),
                () -> assertEquals(7,  InputValidator.validateMenuChoice("7",  1, 13)),
                () -> assertEquals(13, InputValidator.validateMenuChoice("13", 1, 13))
        );
    }

    @Test
    @Order(43)
    @DisplayName("TC-43: validateMenuChoice — out of range throws ValidationException")
    @Tag("student2")
    @Tag("critical")
    void tc43_validateMenuChoice_outOfRange_throwsValidationException() {
        assertAll("out of range inputs",
                () -> assertThrows(InputValidator.ValidationException.class,
                        () -> InputValidator.validateMenuChoice("0",  1, 13)),
                () -> assertThrows(InputValidator.ValidationException.class,
                        () -> InputValidator.validateMenuChoice("14", 1, 13))
        );
    }

    @Test
    @Order(44)
    @DisplayName("TC-44: validateTopicCode — valid codes are normalised to upper case")
    @Tag("student2")
    @Tag("critical")
    void tc44_validateTopicCode_validCodes_normalisedToUpperCase()
            throws InputValidator.ValidationException {
        assertAll("valid topic codes",
                () -> assertEquals("COMP1234", InputValidator.validateTopicCode("comp1234")),
                () -> assertEquals("NURS101",  InputValidator.validateTopicCode("nurs101")),
                () -> assertEquals("COMP1102", InputValidator.validateTopicCode("COMP1102"))
        );
    }

    @ParameterizedTest(name = "TC-45 [{index}]: invalid topic code ''{0}'' should throw")
    @Order(45)
    @DisplayName("TC-45: validateTopicCode — invalid codes throw ValidationException")
    @Tag("student2")
    @Tag("core")
    @ValueSource(strings = {
            "",
            "1234COMP",
            "C1",
            "TOOLONGCODE1234",
            "COMP",
            "   "
    })
    void tc45_validateTopicCode_invalidCodes_throwValidationException(String code) {
        assertThrows(InputValidator.ValidationException.class,
                () -> InputValidator.validateTopicCode(code));
    }

    @Test
    @Order(46)
    @DisplayName("TC-46: validateSemester — valid inputs return normalised values")
    @Tag("student2")
    @Tag("core")
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
}

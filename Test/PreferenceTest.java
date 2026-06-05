import model.Preference;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Preference Tests")
//does the program correctly know which preference has higher priority? Does it reject an invalid priority number?
class PreferenceTest {

    private Preference pref1;
    private Preference pref2;

    @BeforeEach
    void setUpPreferences() {
        pref1 = new Preference("Compact", 1);
        pref2 = new Preference("Evenly Spread", 2);
    }

    @AfterEach
    void tearDownPreferences() {
        pref1 = null;
        pref2 = null;
    }

    @Test
    @Order(11)
    @DisplayName("TC-11: Preference — isCompact returns true for Compact type")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc11_isCompact_returnsTrue() {
        assertTrue(pref1.isCompact());
    }

    @Test
    @Order(12)
    @DisplayName("TC-12: Preference — isEvenlySpread returns true for Evenly Spread type")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc12_isEvenlySpread_returnsTrue() {
        assertTrue(pref2.isEvenlySpread());
    }

    @Test
    @Order(13)
    @DisplayName("TC-13: Preference — isType is case-insensitive")
    @Tag("Nguy1687")
    @Tag("core")
    void tc13_isType_caseInsensitive() {
        assertAll("case-insensitive type check",
                () -> assertTrue(pref1.isType("compact")),
                () -> assertTrue(pref1.isType("COMPACT")),
                () -> assertTrue(pref1.isType("Compact"))
        );
    }

    @Test
    @Order(14)
    @DisplayName("TC-14: Preference — hasHigherPriorityThan returns true when priorityOrder is lower")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc14_hasHigherPriorityThan_lowerOrderWins() {
        assertAll("priority comparison",
                () -> assertTrue(pref1.hasHigherPriorityThan(pref2)),
                () -> assertFalse(pref2.hasHigherPriorityThan(pref1))
        );
    }

    @Test
    @Order(15)
    @DisplayName("TC-15: Preference — invalid priorityOrder less than 1 throws IllegalArgumentException")
    @Tag("Nguy1687")
    @Tag("core")
    void tc15_invalidPriorityOrder_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Preference("Compact", 0));
    }
}

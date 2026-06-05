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

    // ── Original TC-11 to TC-15 ───────────────────────────────────────────────

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

    // ── Additional tests for 100% coverage ───────────────────────────────────

    @Test
    @Order(16)
    @DisplayName("TC-P-16: Constructor — null preferenceType throws NullPointerException")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_16_nullType_throws() {
        assertThrows(NullPointerException.class, () -> new Preference(null, 1));
    }

    @Test
    @Order(17)
    @DisplayName("TC-P-17: isNoMornings — returns true for No Mornings type")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_17_isNoMornings_returnsTrue() {
        Preference p = new Preference("No Mornings", 1);
        assertTrue(p.isNoMornings());
    }

    @Test
    @Order(18)
    @DisplayName("TC-P-18: isNoAfternoons — returns true for No Afternoons type")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_18_isNoAfternoons_returnsTrue() {
        Preference p = new Preference("No Afternoons", 1);
        assertTrue(p.isNoAfternoons());
    }

    @Test
    @Order(19)
    @DisplayName("TC-P-19: isNoFridays — returns true for No Fridays type")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_19_isNoFridays_returnsTrue() {
        Preference p = new Preference("No Fridays", 1);
        assertTrue(p.isNoFridays());
    }

    @Test
    @Order(20)
    @DisplayName("TC-P-20: isNoMornings — returns false for Compact type")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_20_isNoMornings_returnsFalse() {
        assertFalse(pref1.isNoMornings());
    }

    @Test
    @Order(21)
    @DisplayName("TC-P-21: isNoAfternoons — returns false for Compact type")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_21_isNoAfternoons_returnsFalse() {
        assertFalse(pref1.isNoAfternoons());
    }

    @Test
    @Order(22)
    @DisplayName("TC-P-22: isNoFridays — returns false for Compact type")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_22_isNoFridays_returnsFalse() {
        assertFalse(pref1.isNoFridays());
    }

    @Test
    @Order(23)
    @DisplayName("TC-P-23: getPreferenceType — returns correct type string")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_23_getPreferenceType() {
        assertEquals("Compact", pref1.getPreferenceType());
    }

    @Test
    @Order(24)
    @DisplayName("TC-P-24: getPriorityOrder — returns correct order")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_24_getPriorityOrder() {
        assertAll("priority orders",
                () -> assertEquals(1, pref1.getPriorityOrder()),
                () -> assertEquals(2, pref2.getPriorityOrder())
        );
    }

    @Test
    @Order(25)
    @DisplayName("TC-P-25: equals — same type and order returns true")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_25_equals_same() {
        Preference other = new Preference("Compact", 1);
        assertEquals(pref1, other);
    }

    @Test
    @Order(26)
    @DisplayName("TC-P-26: equals — same object reference returns true")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_26_equals_sameReference() {
        assertEquals(pref1, pref1);
    }

    @Test
    @Order(27)
    @DisplayName("TC-P-27: equals — different type returns false")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_27_equals_differentType() {
        assertNotEquals(pref1, pref2);
    }

    @Test
    @Order(28)
    @DisplayName("TC-P-28: equals — null returns false")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_28_equals_null() {
        assertNotEquals(null, pref1);
    }

    @Test
    @Order(29)
    @DisplayName("TC-P-29: equals — different object type returns false")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_29_equals_differentObjectType() {
        assertNotEquals("Compact", pref1);
    }

    @Test
    @Order(30)
    @DisplayName("TC-P-30: equals — case-insensitive type match returns true")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_30_equals_caseInsensitive() {
        Preference upper = new Preference("COMPACT", 1);
        assertEquals(pref1, upper);
    }

    @Test
    @Order(31)
    @DisplayName("TC-P-31: hashCode — equal preferences have equal hashCodes")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_31_hashCode_equal() {
        Preference other = new Preference("Compact", 1);
        assertEquals(pref1.hashCode(), other.hashCode());
    }

    @Test
    @Order(32)
    @DisplayName("TC-P-32: toString — returns correct format")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_32_toString() {
        assertEquals("Compact (priority 1)", pref1.toString());
    }

    @Test
    @Order(33)
    @DisplayName("TC-P-33: TYPE constants — correct string values")
    @Tag("Nguy1687")
    @Tag("core")
    void tc_p_33_typeConstants() {
        assertAll("type constants",
                () -> assertEquals("No Mornings",   Preference.TYPE_NO_MORNINGS),
                () -> assertEquals("No Afternoons", Preference.TYPE_NO_AFTERNOONS),
                () -> assertEquals("Evenly Spread", Preference.TYPE_EVENLY_SPREAD),
                () -> assertEquals("Compact",       Preference.TYPE_COMPACT),
                () -> assertEquals("No Fridays",    Preference.TYPE_NO_FRIDAYS)
        );
    }
}

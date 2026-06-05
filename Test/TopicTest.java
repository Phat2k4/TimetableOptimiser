import model.Availability;
import model.Topic;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Topic Tests")
//can the program split a topic code like COMP1102 from its name? What if we only give a code with no name?
class TopicTest {

    @Test
    @Order(6)
    @DisplayName("TC-06: Topic.parse — splits code and name on first space")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc06_parse_splitsCodeAndName() {
        Topic t = Topic.parse("COMP1102 Programming Fundamentals");
        assertAll("topic parse",
                () -> assertEquals("COMP1102",                 t.getTopicCode()),
                () -> assertEquals("Programming Fundamentals", t.getTopicName())
        );
    }

    @Test
    @Order(7)
    @DisplayName("TC-07: Topic.parse — multi-word name preserved after first space")
    @Tag("Nguy1687")
    @Tag("core")
    void tc07_parse_multiWordNamePreserved() {
        Topic t = Topic.parse("NURS1001 Introduction to Nursing Practice");
        assertEquals("Introduction to Nursing Practice", t.getTopicName());
    }

    @Test
    @Order(8)
    @DisplayName("TC-08: Topic.parse — code only (no space) sets empty name")
    @Tag("Nguy1687")
    @Tag("core")
    void tc08_parse_codeOnly_emptyName() {
        Topic t = Topic.parse("COMP1102");
        assertAll("code-only parse",
                () -> assertEquals("COMP1102", t.getTopicCode()),
                () -> assertEquals("",         t.getTopicName())
        );
    }

    @Test
    @Order(9)
    @DisplayName("TC-09: Topic.parse — null input throws NullPointerException")
    @Tag("Nguy1687")
    @Tag("core")
    void tc09_parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Topic.parse(null));
    }

    @Test
    @Order(10)
    @DisplayName("TC-10: Topic.getFullName — returns code + name when name present")
    @Tag("Nguy1687")
    @Tag("additional")
    void tc10_getFullName_returnsCodeAndName() {
        Topic t = Topic.parse("COMP1002 Fundamentals of Artificial Intelligence");
        assertEquals("COMP1002 Fundamentals of Artificial Intelligence", t.getFullName());
    }

    @Test
    @Order(11)
    @DisplayName("TC-11: Topic.getFullName — returns only code when name is blank")
    @Tag("Nguy1687")
    @Tag("core")
    void tc11_getFullName_blankName_returnsCodeOnly() {
        Topic t = Topic.parse("COMP1102");
        assertEquals("COMP1102", t.getFullName());
    }

    @Test
    @Order(12)
    @DisplayName("TC-12: Topic.addAvailability — availability is added and retrievable")
    @Tag("Nguy1687")
    @Tag("core")
    void tc12_addAvailability_retrievable() {
        Topic t = Topic.parse("COMP1102 Programming");
        Availability av = Availability.parse("On Campus - City - S1 - 1");
        t.addAvailability(av);
        assertAll("after add",
                () -> assertEquals(1, t.getAvailabilities().size()),
                () -> assertTrue(t.getAvailabilities().contains(av))
        );
    }

    @Test
    @Order(13)
    @DisplayName("TC-13: Topic.addAvailability — null throws NullPointerException")
    @Tag("Nguy1687")
    @Tag("core")
    void tc13_addAvailability_null_throws() {
        Topic t = Topic.parse("COMP1102 Programming");
        assertThrows(NullPointerException.class, () -> t.addAvailability(null));
    }

    @Test
    @Order(14)
    @DisplayName("TC-14: Topic.findOrCreateAvailability — creates new when not found")
    @Tag("Nguy1687")
    @Tag("core")
    void tc14_findOrCreateAvailability_createsNew() {
        Topic t = Topic.parse("COMP1102 Programming");
        Availability result = t.findOrCreateAvailability("On Campus - City - S1 - 1");
        assertAll("new availability created",
                () -> assertNotNull(result),
                () -> assertEquals(1, t.getAvailabilities().size()),
                () -> assertEquals("On Campus", result.getAttendanceMode())
        );
    }

    @Test
    @Order(15)
    @DisplayName("TC-15: Topic.findOrCreateAvailability — returns existing when already present")
    @Tag("Nguy1687")
    @Tag("core")
    void tc15_findOrCreateAvailability_returnsExisting() {
        Topic t = Topic.parse("COMP1102 Programming");
        Availability first  = t.findOrCreateAvailability("On Campus - City - S1 - 1");
        Availability second = t.findOrCreateAvailability("On Campus - City - S1 - 1");
        assertAll("same object returned",
                () -> assertSame(first, second),
                () -> assertEquals(1, t.getAvailabilities().size())
        );
    }

    @Test
    @Order(16)
    @DisplayName("TC-16: Topic.getAvailabilities — returns unmodifiable list")
    @Tag("Nguy1687")
    @Tag("core")
    void tc16_getAvailabilities_unmodifiable() {
        Topic t = Topic.parse("COMP1102 Programming");
        t.findOrCreateAvailability("On Campus - City - S1 - 1");
        assertEquals(1, t.getAvailabilities().size());
        assertThrows(UnsupportedOperationException.class,
                () -> t.getAvailabilities().clear());
    }

    @Test
    @Order(17)
    @DisplayName("TC-17: Topic.equals — same topic code returns true")
    @Tag("Nguy1687")
    @Tag("core")
    void tc17_equals_sameCode_returnsTrue() {
        Topic a = Topic.parse("COMP1102 Programming Fundamentals");
        Topic b = Topic.parse("COMP1102 Different Name");
        assertEquals(a, b);
    }

    @Test
    @Order(18)
    @DisplayName("TC-18: Topic.equals — same object reference returns true")
    @Tag("Nguy1687")
    @Tag("core")
    void tc18_equals_sameReference_returnsTrue() {
        Topic t = Topic.parse("COMP1102 Programming");
        assertEquals(t, t);
    }

    @Test
    @Order(19)
    @DisplayName("TC-19: Topic.equals — different topic code returns false")
    @Tag("Nguy1687")
    @Tag("core")
    void tc19_equals_differentCode_returnsFalse() {
        Topic a = Topic.parse("COMP1102 Programming");
        Topic b = Topic.parse("NURS1001 Nursing");
        assertNotEquals(a, b);
    }

    @Test
    @Order(20)
    @DisplayName("TC-20: Topic.equals — null returns false")
    @Tag("Nguy1687")
    @Tag("core")
    void tc20_equals_null_returnsFalse() {
        Topic t = Topic.parse("COMP1102 Programming");
        assertNotEquals(null, t);
    }

    @Test
    @Order(21)
    @DisplayName("TC-21: Topic.equals — different type returns false")
    @Tag("Nguy1687")
    @Tag("core")
    void tc21_equals_differentType_returnsFalse() {
        Topic t = Topic.parse("COMP1102 Programming");
        assertNotEquals("COMP1102", t);
    }

    @Test
    @Order(22)
    @DisplayName("TC-22: Topic.hashCode — equal topics have equal hashCodes")
    @Tag("Nguy1687")
    @Tag("core")
    void tc22_hashCode_equalTopics_equalHashCodes() {
        Topic a = Topic.parse("COMP1102 Programming");
        Topic b = Topic.parse("COMP1102 Different Name");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @Order(23)
    @DisplayName("TC-23: Topic.toString — returns full name")
    @Tag("Nguy1687")
    @Tag("core")
    void tc23_toString_returnsFullName() {
        Topic t = Topic.parse("COMP1102 Programming Fundamentals");
        assertEquals("COMP1102 Programming Fundamentals", t.toString());
    }
}

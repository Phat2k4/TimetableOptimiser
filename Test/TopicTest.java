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
    @Tag("student1")
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
    @Tag("student1")
    @Tag("core")
    void tc07_parse_multiWordNamePreserved() {
        Topic t = Topic.parse("NURS1001 Introduction to Nursing Practice");
        assertEquals("Introduction to Nursing Practice", t.getTopicName());
    }

    @Test
    @Order(8)
    @DisplayName("TC-08: Topic.parse — code only (no space) sets empty name")
    @Tag("student1")
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
    @Tag("student1")
    @Tag("core")
    void tc09_parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Topic.parse(null));
    }

    @Test
    @Order(10)
    @DisplayName("TC-10: Topic.getFullName — returns code + name when name present")
    @Tag("student1")
    @Tag("additional")
    void tc10_getFullName_returnsCodeAndName() {
        Topic t = Topic.parse("COMP1002 Fundamentals of Artificial Intelligence");
        assertEquals("COMP1002 Fundamentals of Artificial Intelligence", t.getFullName());
    }
}

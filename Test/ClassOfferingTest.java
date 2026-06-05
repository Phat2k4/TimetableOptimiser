import model.ClassOffering;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ClassOffering Tests")
class ClassOfferingTest {

    private ClassOffering lecture;
    private ClassOffering tutorial;
    private ClassOffering workshop;

    @BeforeEach
    void setUp() {
        lecture  = new ClassOffering("Lecture",  1);
        tutorial = new ClassOffering("Tutorial", 2);
        workshop = new ClassOffering("Workshop", 1);
    }

    @Test
    @Order(1)
    @DisplayName("TC-CO-01: Constructor — getClassName returns trimmed name")
    void tc_co_01_getClassName() {
        assertEquals("Lecture", lecture.getClassName());
    }

    @Test
    @Order(2)
    @DisplayName("TC-CO-02: Constructor — getClassInstance returns correct instance")
    void tc_co_02_getClassInstance() {
        assertAll("class instances",
                () -> assertEquals(1, lecture.getClassInstance()),
                () -> assertEquals(2, tutorial.getClassInstance())
        );
    }

    @Test
    @Order(3)
    @DisplayName("TC-CO-03: Constructor — null className throws NullPointerException")
    void tc_co_03_nullClassName_throws() {
        assertThrows(NullPointerException.class, () -> new ClassOffering(null, 1));
    }

    @Test
    @Order(4)
    @DisplayName("TC-CO-04: isLecture — returns true for Lecture")
    void tc_co_04_isLecture_true() {
        assertTrue(lecture.isLecture());
    }

    @Test
    @Order(5)
    @DisplayName("TC-CO-05: isLecture — returns true case-insensitive (LECTURE)")
    void tc_co_05_isLecture_caseInsensitive() {
        ClassOffering upper = new ClassOffering("LECTURE 1", 1);
        assertTrue(upper.isLecture());
    }

    @Test
    @Order(6)
    @DisplayName("TC-CO-06: isLecture — returns false for Tutorial and Workshop")
    void tc_co_06_isLecture_false() {
        assertAll("non-lectures",
                () -> assertFalse(tutorial.isLecture()),
                () -> assertFalse(workshop.isLecture())
        );
    }

    @Test
    @Order(7)
    @DisplayName("TC-CO-07: equals — same name and instance returns true")
    void tc_co_07_equals_same() {
        ClassOffering other = new ClassOffering("Lecture", 1);
        assertEquals(lecture, other);
    }

    @Test
    @Order(8)
    @DisplayName("TC-CO-08: equals — same name case-insensitive returns true")
    void tc_co_08_equals_caseInsensitive() {
        ClassOffering upper = new ClassOffering("LECTURE", 1);
        assertEquals(lecture, upper);
    }

    @Test
    @Order(9)
    @DisplayName("TC-CO-09: equals — different instance returns false")
    void tc_co_09_equals_differentInstance() {
        ClassOffering other = new ClassOffering("Lecture", 2);
        assertNotEquals(lecture, other);
    }

    @Test
    @Order(10)
    @DisplayName("TC-CO-10: toString — returns correct format")
    void tc_co_10_toString() {
        assertEquals("Lecture (Instance 1)", lecture.toString());
    }
}

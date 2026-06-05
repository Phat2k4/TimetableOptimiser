import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("ClassOption Tests")
class ClassOptionTest {

    private static Location bedford;
    private ClassOffering lectureOffering;
    private ClassOffering tutorialOffering;
    private ClassSession  session;
    private ClassOption   lectureOption;
    private ClassOption   tutorialOption;

    @BeforeAll
    static void setUpSuite() {
        bedford = new Location("Bedford Park", "101");
    }

    @BeforeEach
    void setUp() {
        lectureOffering  = new ClassOffering("Lecture",  1);
        tutorialOffering = new ClassOffering("Tutorial", 1);
        session          = new ClassSession(
                LocalDate.of(2026, 2, 23), LocalDate.of(2026, 6, 1),
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), bedford);
        lectureOption  = new ClassOption(lectureOffering,  session);
        tutorialOption = new ClassOption(tutorialOffering, session);
    }

    @Test
    @Order(1)
    @DisplayName("TC-COP-01: Constructor — null offering throws NullPointerException")
    void tc_cop_01_nullOffering_throws() {
        assertThrows(NullPointerException.class, () -> new ClassOption(null, session));
    }

    @Test
    @Order(2)
    @DisplayName("TC-COP-02: Constructor — null session throws NullPointerException")
    void tc_cop_02_nullSession_throws() {
        assertThrows(NullPointerException.class, () -> new ClassOption(lectureOffering, null));
    }

    @Test
    @Order(3)
    @DisplayName("TC-COP-03: getClassOffering — returns correct offering")
    void tc_cop_03_getClassOffering() {
        assertEquals(lectureOffering, lectureOption.getClassOffering());
    }

    @Test
    @Order(4)
    @DisplayName("TC-COP-04: getClassSession — returns correct session")
    void tc_cop_04_getClassSession() {
        assertEquals(session, lectureOption.getClassSession());
    }

    @Test
    @Order(5)
    @DisplayName("TC-COP-05: isLecture — returns true for lecture option")
    void tc_cop_05_isLecture_true() {
        assertTrue(lectureOption.isLecture());
    }

    @Test
    @Order(6)
    @DisplayName("TC-COP-06: isLecture — returns false for tutorial option")
    void tc_cop_06_isLecture_false() {
        assertFalse(tutorialOption.isLecture());
    }

    @Test
    @Order(7)
    @DisplayName("TC-COP-07: getClassName — returns class name from offering")
    void tc_cop_07_getClassName() {
        assertEquals("Lecture", lectureOption.getClassName());
    }

    @Test
    @Order(8)
    @DisplayName("TC-COP-08: getClassInstance — returns instance number from offering")
    void tc_cop_08_getClassInstance() {
        assertEquals(1, lectureOption.getClassInstance());
    }

    @Test
    @Order(9)
    @DisplayName("TC-COP-09: equals — same offering and session returns true")
    void tc_cop_09_equals_same() {
        ClassOption other = new ClassOption(new ClassOffering("Lecture", 1), session);
        assertEquals(lectureOption, other);
    }

    @Test
    @Order(10)
    @DisplayName("TC-COP-10: equals — different offering returns false")
    void tc_cop_10_equals_differentOffering() {
        assertNotEquals(lectureOption, tutorialOption);
    }
}

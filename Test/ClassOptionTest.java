import model.*;
import org.junit.jupiter.api.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ClassOption Tests")
class ClassOptionTest {

    private ClassOffering lectureOffering;
    private ClassOffering tutorialOffering;
    private ClassSession mondaySession;
    private ClassSession tuesdaySession;

    @BeforeEach
    void setUp() {
        lectureOffering = new ClassOffering("Lecture", 1);
        tutorialOffering = new ClassOffering("Tutorial", 2);

        mondaySession = new ClassSession(
                LocalDate.of(2026, 2, 23),
                LocalDate.of(2026, 6, 1),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                new Location("Bedford Park", "101")
        );

        tuesdaySession = new ClassSession(
                LocalDate.of(2026, 2, 23),
                LocalDate.of(2026, 6, 1),
                DayOfWeek.TUESDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                new Location("Bedford Park", "102")
        );
    }

    @Test
    @Order(200)
    @DisplayName("ClassOption — constructor stores class offering and class session")
    @Tag("dass0027")
    @Tag("critical")
    void constructorstoresOfferingAndSession() {
        ClassOption option = new ClassOption(lectureOffering, mondaySession);

        assertAll("stored fields",
                () -> assertSame(lectureOffering, option.getClassOffering()),
                () -> assertSame(mondaySession, option.getClassSession())
        );
    }

    @Test
    @Order(201)
    @DisplayName("ClassOption — constructor rejects null class offering")
    @Tag("dass0027")
    @Tag("core")
    void constructor_nullClassOffering_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new ClassOption(null, mondaySession));
    }

    @Test
    @Order(202)
    @DisplayName("ClassOption — constructor rejects null class session")
    @Tag("dass0027")
    @Tag("core")
    void constructor_nullClassSession_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new ClassOption(lectureOffering, null));
    }

    @Test
    @Order(203)
    @DisplayName("ClassOption — isLecture returns true for lecture offering")
    @Tag("dass0027")
    @Tag("critical")
    void lectureOfferingreturnsTrue() {
        ClassOption option = new ClassOption(lectureOffering, mondaySession);

        assertTrue(option.isLecture());
    }

    @Test
    @Order(204)
    @DisplayName("Lecture returns false for tutorial offering")
    @Tag("dass0027")
    @Tag("critical")
    void LecturetutorialOfferingreturnsFalse() {
        ClassOption option = new ClassOption(tutorialOffering, mondaySession);

        assertFalse(option.isLecture());
    }

    @Test
    @Order(205)
    @DisplayName("ClassOptiongetClassName delegates to ClassOffering")
    @Tag("dass0027")
    @Tag("critical")
    void getClassNamedelegatesToClassOffering() {
        ClassOption option = new ClassOption(tutorialOffering, mondaySession);

        assertEquals("Tutorial", option.getClassName());
    }

    @Test
    @Order(206)
    @DisplayName("getClassInstance delegates to ClassOffering")
    @Tag("dass0027")
    @Tag("critical")
    void getClassInstance_delegatesToClassOffering() {
        ClassOption option = new ClassOption(tutorialOffering, mondaySession);

        assertEquals(2, option.getClassInstance());
    }

    @Test
    @Order(207)
    @DisplayName("ClassOptionequals returns true for same object")
    @Tag("dass0027")
    @Tag("core")
    void equalssameObjectreturnsTrue() {
        ClassOption option = new ClassOption(lectureOffering, mondaySession);

        assertEquals(option, option);
    }

    @Test
    @Order(208)
    @DisplayName("equals returns true for same offering and same session")
    @Tag("dass0027")
    @Tag("critical")
    void equalssameOfferingAndSessionreturnsTrue() {
        ClassOption optionA = new ClassOption(lectureOffering, mondaySession);
        ClassOption optionB = new ClassOption(new ClassOffering("Lecture", 1), mondaySession);

        assertEquals(optionA, optionB);
    }

    @Test
    @Order(209)
    @DisplayName("equals returns false for different offering")
    @Tag("dass0027")
    @Tag("core")
    void equalsdifferentOfferingreturnsFalse() {
        ClassOption optionA = new ClassOption(lectureOffering, mondaySession);
        ClassOption optionB = new ClassOption(tutorialOffering, mondaySession);

        assertNotEquals(optionA, optionB);
    }

    @Test
    @Order(210)
    @DisplayName("equals returns false for different session")
    @Tag("dass0027")
    @Tag("core")
    void equalsdifferentSession_returnsFalse() {
        ClassOption optionA = new ClassOption(lectureOffering, mondaySession);
        ClassOption optionB = new ClassOption(lectureOffering, tuesdaySession);

        assertNotEquals(optionA, optionB);
    }

    @Test
    @Order(211)
    @DisplayName("equals returns false for null")
    @Tag("dass0027")
    @Tag("core")
    void equalsnullreturnsFalse() {
        ClassOption option = new ClassOption(lectureOffering, mondaySession);

        assertNotEquals(null, option);
    }

    @Test
    @Order(212)
    @DisplayName("equals returns false for different object type")
    @Tag("dass0027")
    @Tag("additional")
    void equalsdifferentObjectType_returnsFalse() {
        ClassOption option = new ClassOption(lectureOffering, mondaySession);

        assertNotEquals("Lecture option", option);
    }

    @Test
    @Order(213)
    @DisplayName("ClassOptionequal objects have same hashCode")
    @Tag("dass0027")
    @Tag("core")
    void hashCodeequalObjectshaveSameHashCode() {
        ClassOption optionA = new ClassOption(lectureOffering, mondaySession);
        ClassOption optionB = new ClassOption(new ClassOffering("Lecture", 1), mondaySession);

        assertEquals(optionA.hashCode(), optionB.hashCode());
    }

    @Test
    @Order(214)
    @DisplayName("different objects usually have different hashCode")
    @Tag("dass0027")
    @Tag("additional")
    void hashCodedifferentObjectsnotEqual() {
        ClassOption optionA = new ClassOption(lectureOffering, mondaySession);
        ClassOption optionB = new ClassOption(tutorialOffering, tuesdaySession);

        assertNotEquals(optionA.hashCode(), optionB.hashCode());
    }

    @Test
    @Order(215)
    @DisplayName("toString contains class offering and class session")
    @Tag("dass0027")
    @Tag("additional")
    void toStringcontainsOfferingAndSession() {
        ClassOption option = new ClassOption(lectureOffering, mondaySession);

        String text = option.toString();

        assertAll("toString content",
                () -> assertTrue(text.contains("Lecture")),
                () -> assertTrue(text.contains("Instance 1")),
                () -> assertTrue(text.contains("MONDAY")),
                () -> assertTrue(text.contains("09:00")),
                () -> assertTrue(text.contains("11:00")),
                () -> assertTrue(text.contains("Bedford Park"))
        );
    }
}

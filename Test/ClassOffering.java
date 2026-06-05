import model.ClassOffering;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ClassOffering Tests")
class ClassOfferingTest {

    @Test
    @Order(216)
    @DisplayName("constructor stores class name and instance")
    @Tag("dass0027")
    @Tag("critical")
    void constructorstoresClassNameAndInstance() {
        ClassOffering offering = new ClassOffering("Lecture", 1);

        assertAll("stored fields",
                () -> assertEquals("Lecture", offering.getClassName()),
                () -> assertEquals(1, offering.getClassInstance())
        );
    }

    @Test
    @Order(217)
    @DisplayName("constructor trims class name")
    @Tag("dass0027")
    @Tag("core")
    void constructortrimsClassName() {
        ClassOffering offering = new ClassOffering("  Tutorial  ", 2);

        assertEquals("Tutorial", offering.getClassName());
    }

    @Test
    @Order(218)
    @DisplayName("constructor rejects null class name")
    @Tag("dass0027")
    @Tag("core")
    void constructornullClassNamethrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new ClassOffering(null, 1));
    }

    @Test
    @Order(219)
    @DisplayName("ClassOfferingisLecture returns true for Lecture")
    @Tag("dass0027")
    @Tag("critical")
    void isLecturelecturereturnsTrue() {
        ClassOffering offering = new ClassOffering("Lecture", 1);

        assertTrue(offering.isLecture());
    }

    @Test
    @Order(220)
    @DisplayName("ClassOffering — isLecture is case-insensitive")
    @Tag("dass0027")
    @Tag("core")
    void isLecturecaseInsensitivereturnsTrue() {
        ClassOffering offering = new ClassOffering("lecture", 1);

        assertTrue(offering.isLecture());
    }

    @Test
    @Order(221)
    @DisplayName("isLecture returns true when class name starts with Lecture")
    @Tag("dass0027")
    @Tag("core")
    void isLectureclassNameStartsWithLecturereturnsTrue() {
        ClassOffering offering = new ClassOffering("Lecture Online", 1);

        assertTrue(offering.isLecture());
    }

    @Test
    @Order(222)
    @DisplayName("ClassOffering isLecture returns false for Tutorial")
    @Tag("dass0027")
    @Tag("critical")
    void isLecturetutorialreturnsFalse() {
        ClassOffering offering = new ClassOffering("Tutorial", 1);

        assertFalse(offering.isLecture());
    }

    @Test
    @Order(223)
    @DisplayName("isLecture returns false for Workshop")
    @Tag("dass0027")
    @Tag("critical")
    void isLectureworkshopreturnsFalse() {
        ClassOffering offering = new ClassOffering("Workshop", 1);

        assertFalse(offering.isLecture());
    }

    @Test
    @Order(224)
    @DisplayName("equals returns true for same object")
    @Tag("dass0027")
    @Tag("core")
    void equalssameObjectreturnsTrue() {
        ClassOffering offering = new ClassOffering("Lecture", 1);

        assertEquals(offering, offering);
    }

    @Test
    @Order(225)
    @DisplayName("equals returns true for same class name and instance")
    @Tag("dass0027")
    @Tag("critical")
    void equalssameClassNameAndInstancereturnsTrue() {
        ClassOffering offeringA = new ClassOffering("Lecture", 1);
        ClassOffering offeringB = new ClassOffering("Lecture", 1);

        assertEquals(offeringA, offeringB);
    }

    @Test
    @Order(226)
    @DisplayName("equals ignores class name case")
    @Tag("dass0027")
    @Tag("core")
    void equalssameNameDifferentCasereturnsTrue() {
        ClassOffering offeringA = new ClassOffering("Lecture", 1);
        ClassOffering offeringB = new ClassOffering("lecture", 1);

        assertEquals(offeringA, offeringB);
    }

    @Test
    @Order(227)
    @DisplayName("equals returns false for different class name")
    @Tag("dass0027")
    @Tag("core")
    void equalsdifferentClassNamereturnsFalse() {
        ClassOffering offeringA = new ClassOffering("Lecture", 1);
        ClassOffering offeringB = new ClassOffering("Tutorial", 1);

        assertNotEquals(offeringA, offeringB);
    }

    @Test
    @Order(228)
    @DisplayName("equals returns false for different instance")
    @Tag("dass0027")
    @Tag("core")
    void equalsdifferentInstancereturnsFalse() {
        ClassOffering offeringA = new ClassOffering("Lecture", 1);
        ClassOffering offeringB = new ClassOffering("Lecture", 2);

        assertNotEquals(offeringA, offeringB);
    }

    @Test
    @Order(229)
    @DisplayName("equals returns false for null")
    @Tag("dass0027")
    @Tag("core")
    void equalsnullreturnsFalse() {
        ClassOffering offering = new ClassOffering("Lecture", 1);

        assertNotEquals(null, offering);
    }

    @Test
    @Order(230)
    @DisplayName("equals returns false for different object type")
    @Tag("dass0027")
    @Tag("additional")
    void equalsdifferentObjectTypereturnsFalse() {
        ClassOffering offering = new ClassOffering("Lecture", 1);

        assertNotEquals("Lecture", offering);
    }

    @Test
    @Order(231)
    @DisplayName("ClassOffering equal objects have same hashCode")
    @Tag("dass0027")
    @Tag("core")
    void hashCodeequalObjectshaveSameHashCode() {
        ClassOffering offeringA = new ClassOffering("Lecture", 1);
        ClassOffering offeringB = new ClassOffering("Lecture", 1);

        assertEquals(offeringA.hashCode(), offeringB.hashCode());
    }

    @Test
    @Order(232)
    @DisplayName("ClassOffering different objects usually have different hashCode")
    @Tag("dass0027")
    @Tag("additional")
    void hashCodedifferentObjectsnotEqual() {
        ClassOffering offeringA = new ClassOffering("Lecture", 1);
        ClassOffering offeringB = new ClassOffering("Tutorial", 2);

        assertNotEquals(offeringA.hashCode(), offeringB.hashCode());
    }

    @Test
    @Order(233)
    @DisplayName("ClassOffering toString contains class name and instance")
    @Tag("dass0027")
    @Tag("additional")
    void toStringcontainsClassNameAndInstance() {
        ClassOffering offering = new ClassOffering("Workshop", 4);

        String text = offering.toString();

        assertAll("toString content",
                () -> assertTrue(text.contains("Workshop")),
                () -> assertTrue(text.contains("Instance 4"))
        );
    }
}

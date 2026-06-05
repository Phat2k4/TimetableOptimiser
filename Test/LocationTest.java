import model.Location;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Location Tests")
// can the program correctly read a building name and room number?
// What happens if we give it nothing — does it return 'Unknown' safely?
class LocationTest {

    private static Location bedfordA101;
    private static Location cityTower201;
    private static Location tonsleyB301;

    @BeforeAll
    static void setUpSuite() {
        bedfordA101  = new Location("Bedford Park", "101");
        cityTower201 = new Location("Flinders City Campus", "201");
        tonsleyB301  = new Location("Tonsley", "301");
    }

    @Test
    @Order(1)
    @DisplayName("TC-01: Location.parse — valid 'Building, Room' string")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc01_parse_validBuildingRoomString() {
        Location loc = Location.parse("Festival Tower, 801");
        assertAll("parsed location fields",
                () -> assertEquals("Festival Tower", loc.getBuilding()),
                () -> assertEquals("801",            loc.getRoom())
        );
    }

    @Test
    @Order(2)
    @DisplayName("TC-02: Location.parse — no room delimiter uses whole string as building")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc02_parse_noDelimiter_wholeStringIsBuilding() {
        Location loc = Location.parse("Festival Tower");
        assertAll("no-delimiter parse",
                () -> assertEquals("Festival Tower", loc.getBuilding()),
                () -> assertEquals("",               loc.getRoom())
        );
    }

    @Test
    @Order(3)
    @DisplayName("TC-03: Location.parse — null or blank input returns Unknown")
    @Tag("Nguy1687")
    @Tag("core")
    void tc03_parse_nullOrBlank_returnsUnknown() {
        assertAll("null and blank input",
                () -> assertEquals("Unknown", Location.parse(null).getBuilding()),
                () -> assertEquals("Unknown", Location.parse("").getBuilding()),
                () -> assertEquals("Unknown", Location.parse("   ").getBuilding())
        );
    }

    @Test
    @Order(4)
    @DisplayName("TC-04: Location.isSameCampusAs — same building case-insensitive returns true")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc04_isSameCampusAs_caseInsensitive_returnsTrue() {
        Location a = new Location("Bedford Park", "101");
        Location b = new Location("BEDFORD PARK", "999");
        assertTrue(a.isSameCampusAs(b));
    }

    @Test
    @Order(5)
    @DisplayName("TC-05: Location.isSameCampusAs — different buildings returns false")
    @Tag("Nguy1687")
    @Tag("critical")
    void tc05_isSameCampusAs_differentBuildings_returnsFalse() {
        assertAll("different campus checks",
                () -> assertFalse(bedfordA101.isSameCampusAs(cityTower201)),
                () -> assertFalse(bedfordA101.isSameCampusAs(tonsleyB301)),
                () -> assertFalse(bedfordA101.isSameCampusAs(null))
        );
    }
}

package model;

import java.util.Objects;

/**
 * Represents a physical location where a class session is held.
 * Immutable value object.
 */
public final class Location {

    private final String building;
    private final String room;

    public Location(String building, String room) {
        this.building = Objects.requireNonNull(building, "building must not be null").trim();
        this.room     = Objects.requireNonNull(room,     "room must not be null").trim();
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    /**
     * Parses a location string in the format "Building, Room".
     * If the delimiter is absent the whole string becomes the building
     * and room is set to an empty string.
     */
    public static Location parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new Location("Unknown", "");
        }
        int idx = raw.indexOf(", ");
        if (idx < 0) {
            return new Location(raw.trim(), "");
        }
        return new Location(raw.substring(0, idx).trim(),
                            raw.substring(idx + 2).trim());
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String getBuilding() { return building; }
    public String getRoom()     { return room; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** True when both locations share the same campus building (case-insensitive). */
    public boolean isSameCampusAs(Location other) {
        if (other == null) return false;
        return this.building.equalsIgnoreCase(other.building);
    }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location l)) return false;
        return building.equalsIgnoreCase(l.building) &&
               room.equalsIgnoreCase(l.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building.toLowerCase(), room.toLowerCase());
    }

    @Override
    public String toString() {
        return room.isBlank() ? building : building + ", " + room;
    }
}

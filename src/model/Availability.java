package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents one delivery mode / campus / semester combination for a {@link Topic}.
 *
 * CSV column "Availability" splits on " - " into:
 *   [attendanceMode, campusLocation, semesterN, availabilityNumber]
 *
 * An Availability offers 1..* {@link ClassOption}s.
 */
public final class Availability {

    private final String attendanceMode;      // e.g. "On Campus", "Online"
    private final String campusLocation;      // e.g. "City", "Bedford Park"
    private final String semesterN;           // e.g. "S1", "S2"
    private final String availabilityNumber;  // e.g. "1", "A"

    private final List<ClassOption> classOptions;

    public Availability(String attendanceMode,
                        String campusLocation,
                        String semesterN,
                        String availabilityNumber) {
        this.attendanceMode   = Objects.requireNonNull(attendanceMode).trim();
        this.campusLocation   = Objects.requireNonNull(campusLocation).trim();
        this.semesterN        = Objects.requireNonNull(semesterN).trim();
        this.availabilityNumber = Objects.requireNonNull(availabilityNumber).trim();
        this.classOptions     = new ArrayList<>();
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    /**
     * Parses the raw availability string.
     * Expected format: "AttendanceMode - Campus - Semester - AvailabilityNumber"
     * Falls back gracefully when fields are missing.
     */
    public static Availability parse(String raw) {
        Objects.requireNonNull(raw, "raw availability string must not be null");
        String[] parts = raw.split(" - ", -1);
        String mode   = parts.length > 0 ? parts[0].trim() : "";
        String campus = parts.length > 1 ? parts[1].trim() : "";
        String sem    = parts.length > 2 ? parts[2].trim() : "";
        String num    = parts.length > 3 ? parts[3].trim() : "";
        return new Availability(mode, campus, sem, num);
    }

    // ── Mutation ──────────────────────────────────────────────────────────────

    /** Adds a class option to this availability. */
    public void addClassOption(ClassOption option) {
        Objects.requireNonNull(option, "option must not be null");
        classOptions.add(option);
    }

    /**
     * Replaces {@code oldOption} with {@code newOption} in-place, preserving list order.
     *
     * @return true if {@code oldOption} was found and replaced
     */
    public boolean replaceClassOption(ClassOption oldOption, ClassOption newOption) {
        Objects.requireNonNull(oldOption, "oldOption must not be null");
        Objects.requireNonNull(newOption, "newOption must not be null");
        int idx = classOptions.indexOf(oldOption);
        if (idx < 0) return false;
        classOptions.set(idx, newOption);
        return true;
    }

    /**
     * Removes {@code option} from this availability.
     *
     * @return true if the option was present and has been removed
     */
    public boolean removeClassOption(ClassOption option) {
        Objects.requireNonNull(option, "option must not be null");
        return classOptions.remove(option);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String           getAttendanceMode()    { return attendanceMode; }
    public String           getCampusLocation()    { return campusLocation; }
    public String           getSemesterN()         { return semesterN; }
    public String           getAvailabilityNumber(){ return availabilityNumber; }
    public List<ClassOption> getClassOptions()     { return Collections.unmodifiableList(classOptions); }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Availability a)) return false;
        return attendanceMode.equalsIgnoreCase(a.attendanceMode) &&
               campusLocation.equalsIgnoreCase(a.campusLocation) &&
               semesterN.equalsIgnoreCase(a.semesterN) &&
               availabilityNumber.equalsIgnoreCase(a.availabilityNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                attendanceMode.toLowerCase(),
                campusLocation.toLowerCase(),
                semesterN.toLowerCase(),
                availabilityNumber.toLowerCase());
    }

    @Override
    public String toString() {
        return attendanceMode + " - " + campusLocation +
               " - " + semesterN + " - " + availabilityNumber;
    }
}

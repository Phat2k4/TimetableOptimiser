package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One entry in a {@link Timetable}, representing the chosen class options
 * for a single {@link Topic}.
 *
 * After construction, warnings are computed lazily (or explicitly) by the
 * timetable optimiser / clash-checker — they are NOT auto-set on creation.
 *
 * Relationship: TimetableEntry includes 1..* ClassOption (via ClassSession).
 */
public final class TimetableEntry {

    private final Topic       topic;

    /** The chosen class options for this topic in this timetable. */
    private final List<ClassOption> chosenOptions;

    /**
     * True if at least one session in this entry clashes with a session in
     * another entry in the same timetable.
     */
    private boolean clashWarning;

    /**
     * True if at least one pair of consecutive sessions (different campuses)
     * has a gap < 30 minutes, triggering a commute warning.
     */
    private boolean commuteWarning;

    public TimetableEntry(Topic topic) {
        this.topic         = Objects.requireNonNull(topic, "topic must not be null");
        this.chosenOptions = new ArrayList<>();
        this.clashWarning  = false;
        this.commuteWarning = false;
    }

    // ── Mutation ──────────────────────────────────────────────────────────────

    /** Adds a chosen class option to this entry. */
    public void addChosenOption(ClassOption option) {
        Objects.requireNonNull(option, "option must not be null");
        chosenOptions.add(option);
    }

    /** Removes a chosen class option (e.g. when user changes selection). */
    public boolean removeChosenOption(ClassOption option) {
        return chosenOptions.remove(option);
    }

    /** Called by the clash-checker to flag or clear the clash warning. */
    public void setClashWarning(boolean clashWarning) {
        this.clashWarning = clashWarning;
    }

    /** Called by the clash-checker to flag or clear the commute warning. */
    public void setCommuteWarning(boolean commuteWarning) {
        this.commuteWarning = commuteWarning;
    }

    // ── Derived helpers ───────────────────────────────────────────────────────

    /**
     * Collects all {@link ClassSession}s from the chosen options in this entry.
     */
    public List<ClassSession> getAllSessions() {
        List<ClassSession> sessions = new ArrayList<>();
        for (ClassOption opt : chosenOptions) {
            sessions.add(opt.getClassSession());
        }
        return Collections.unmodifiableList(sessions);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Topic            getTopic()          { return topic; }
    public List<ClassOption> getChosenOptions() { return Collections.unmodifiableList(chosenOptions); }
    public boolean          isClashWarning()    { return clashWarning; }
    public boolean          isCommuteWarning()  { return commuteWarning; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimetableEntry e)) return false;
        return topic.equals(e.topic) && chosenOptions.equals(e.chosenOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, chosenOptions);
    }

    @Override
    public String toString() {
        return "TimetableEntry{topic=" + topic.getTopicCode() +
               ", options=" + chosenOptions.size() +
               ", clash=" + clashWarning +
               ", commute=" + commuteWarning + "}";
    }
}

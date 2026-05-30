import model.AppState;

/**
 * Entry point for the Student Timetable Optimiser.
 *
 * <h3>Startup sequence</h3>
 * <ol>
 *   <li>Print the ASCII art banner via {@link DisplayFormatter#renderTitle()}.</li>
 *   <li>Load persisted application state via {@link AppState#getInstance()}
 *       (which calls {@code load()} automatically on first access).</li>
 *   <li>Hand control to {@link ConsoleController#run()}, which manages the
 *       main menu loop and saves state before every clean exit.</li>
 * </ol>
 *
 * <p><b>Requirements:</b> Java 17, no external libraries.
 * <p><b>Build:</b> Compile all {@code .java} files together:
 * <pre>
 *   javac -d out model/*.java *.java
 *   java  -cp out Main
 * </pre>
 */
public final class Main {

    private Main() {}   // utility class — never instantiated

    public static void main(String[] args) {
        // 1. ASCII art banner
        DisplayFormatter.renderTitle();

        // 2. Load persisted state (AppState.getInstance() triggers load() on first call)
        AppState state = AppState.getInstance();

        // 3. Show topic / timetable counts so the user knows what's loaded
        int topicCount     = state.allTopics().size();
        int timetableCount = state.allTimetables().size();

        if (topicCount > 0 || timetableCount > 0) {
            System.out.printf(AppState.ANSI_GREEN +
                    "  Loaded: %d topic(s), %d timetable(s) from saved state.%n" +
                    AppState.ANSI_RESET, topicCount, timetableCount);
        }

        // 4. Start the controller loop (handles all menu interaction and final save)
        ConsoleController controller = new ConsoleController();
        controller.run();
    }
}

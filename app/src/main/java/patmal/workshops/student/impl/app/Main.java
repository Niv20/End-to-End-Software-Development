package patmal.workshops.student.impl.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

/**
 * workshop-cli: fills a WorkshopSystem with demo data and exports it to JSON files
 * in the current folder.
 * <p>
 * Usage: {@code java -jar workshop-cli.jar [students.csv]}. Without an argument the students
 * are taken from the students.csv file inside the jar.
 */
public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    private static final List<String> TEAM_NAMES = List.of("Alpha", "Beta", "Gamma");
    private static final int TEAM_SIZE = 2;

    public static void main(String[] args) {
        if (args.length > 1) {
            log.error("Usage: java -jar workshop-cli.jar [students.csv]");
            System.exit(1);
        }
        try {
            WorkshopSystem system = new WorkshopSystemImpl();
            List<Student> students = args.length == 1
                    ? system.importStudentDataFromFile(new File(args[0]))
                    : importBundledStudents(system);
            List<Workshop> workshops = createWorkshops(system);
            createTeams(system, workshops, students);
            exportAll(system);
        } catch (RuntimeException e) {
            log.error("The application stopped: {}", e.getMessage());
            System.exit(1);
        }
    }

    // importStudentDataFromFile needs a real file, so the file in the jar is copied to a temporary file
    private static List<Student> importBundledStudents(WorkshopSystem system) {
        log.info("No CSV file was given, using the students.csv inside the jar");
        try (InputStream in = Main.class.getResourceAsStream("/students.csv")) {
            if (in == null) {
                throw new IllegalStateException("students.csv is missing from the jar");
            }
            Path tempFile = Files.createTempFile("students", ".csv");
            try {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                return system.importStudentDataFromFile(tempFile.toFile());
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the students from the jar", e);
        }
    }

    private static List<Workshop> createWorkshops(WorkshopSystem system) {
        Instructor dana = system.createInstructor("i1", "Dana Levi", "dana.levi@univ.edu");
        Instructor yossi = system.createInstructor("i2", "Yossi Mizrahi", "yossi.mizrahi@univ.edu");

        Workshop java = system.createWorkshop("Java Workshop", "Object oriented programming with Java");
        Workshop web = system.createWorkshop("Web Development", "HTML, CSS and JavaScript");
        Workshop databases = system.createWorkshop("Databases", "Relational databases and SQL");

        system.associateWorkshopWithInstructor(dana, java);
        system.associateWorkshopWithInstructor(dana, web);
        system.associateWorkshopWithInstructor(yossi, databases);
        return List.of(java, web, databases);
    }

    // One team of two students in each workshop, as long as there are enough students.
    // The last student always stays free.
    private static void createTeams(WorkshopSystem system, List<Workshop> workshops, List<Student> students) {
        for (int i = 0; i < TEAM_NAMES.size() && i < workshops.size(); i++) {
            int from = i * TEAM_SIZE;
            int to = from + TEAM_SIZE;
            if (to >= students.size()) {
                break;
            }
            List<String> members = new ArrayList<>();
            for (Student student : students.subList(from, to)) {
                members.add(student.getId());
            }
            system.createTeam(TEAM_NAMES.get(i), workshops.get(i), members);
        }
        if (system.getTeams().isEmpty()) {
            log.warn("There are not enough students to create a team");
        }
    }

    private static void exportAll(WorkshopSystem system) {
        File instructors = new File("instructors.json");
        File students = new File("students.json");
        File teams = new File("teams.json");

        system.exportInstructorsDataToJson(instructors);
        system.exportStudentsDataToJson(students);
        system.exportTeamsDataToJson(teams);

        log.info("{} instructors, {} workshops, {} students ({} free) and {} teams were written to {}",
                system.getInstructors().size(), system.getWorkshops().size(), system.getStudents().size(),
                system.getFreeStudents().size(), system.getTeams().size(), instructors.getAbsoluteFile().getParent());
    }
}

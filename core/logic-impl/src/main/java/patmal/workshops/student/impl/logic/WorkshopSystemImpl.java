package patmal.workshops.student.impl.logic;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Person;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

/**
 * The in-memory implementation of WorkshopSystem. Each area (instructors, students, workshops,
 * teams) has its own manager; this class checks the rules that involve more than one area,
 * delegates to the managers and logs every operation.
 * <p>
 * An invalid operation throws IllegalArgumentException and leaves the system unchanged.
 * Person ids and e-mails, workshop names and team names are unique (an id belongs to a student
 * or to an instructor, not to both). Names and e-mails are compared ignoring case and the
 * spaces at the start and the end.
 */
public class WorkshopSystemImpl implements WorkshopSystem {

    private static final Logger log = LogManager.getLogger(WorkshopSystemImpl.class);

    private final InstructorManager instructorManager = new InstructorManager();
    private final StudentManager studentManager = new StudentManager();
    private final WorkshopManager workshopManager = new WorkshopManager();
    private final TeamManager teamManager = new TeamManager(studentManager);
    private final JsonExporter jsonExporter = new JsonExporter(instructorManager, studentManager, teamManager);
    private final CsvStudentImporter csvImporter = new CsvStudentImporter();

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        try {
            checkNewPerson(id, name, email);
            Instructor instructor = instructorManager.create(id, name, email);
            log.info("Created instructor {} ({})", id, name);
            return instructor;
        } catch (IllegalArgumentException e) {
            log.warn("Instructor {} was not created: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        try {
            checkNewPerson(id, name, email);
            Student student = studentManager.create(id, name, email);
            log.info("Created student {} ({})", id, name);
            return student;
        } catch (IllegalArgumentException e) {
            log.warn("Student {} was not created: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        try {
            Workshop workshop = workshopManager.create(name, description);
            log.info("Created workshop {} ({})", workshop.getId(), name);
            return workshop;
        } catch (IllegalArgumentException e) {
            log.warn("Workshop {} was not created: {}", name, e.getMessage());
            throw e;
        }
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        try {
            InstructorImpl newInstructor = instructorManager.find(instructor);
            WorkshopImpl workshopToAssociate = workshopManager.find(workshop);
            instructorManager.assignWorkshop(newInstructor, workshopToAssociate);
            log.info("Workshop {} is taught by instructor {}", workshopToAssociate.getId(), newInstructor.getId());
        } catch (IllegalArgumentException e) {
            log.warn("Workshop was not associated with an instructor: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        try {
            WorkshopImpl teamWorkshop = workshopManager.find(workshop);
            Team team = teamManager.create(name, teamWorkshop, members);
            log.info("Created team {} ({}) in workshop {} with students {}",
                    team.getId(), name, teamWorkshop.getId(), members);
            return team;
        } catch (IllegalArgumentException e) {
            log.warn("Team {} was not created: {}", name, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Instructor> getInstructors() {
        return instructorManager.getAll();
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructorManager.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return studentManager.getAll();
    }

    @Override
    public Student getStudent(String id) {
        return studentManager.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return workshopManager.getAll();
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshopManager.get(id);
    }

    @Override
    public List<Team> getTeams() {
        return teamManager.getAll();
    }

    @Override
    public Team getTeam(Integer id) {
        return teamManager.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        return studentManager.getFree();
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        List<Student> result = new ArrayList<>();
        InstructorImpl instructor = instructorManager.get(instructorId);
        if (instructor == null) {
            return result;
        }
        for (Workshop workshop : instructor.getWorkshops()) {
            result.addAll(teamManager.getStudentsOfWorkshop(workshop.getId()));
        }
        return result;
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        return teamManager.getStudentsOfWorkshop(workshopId);
    }

    @Override
    public void exportInstructorsDataToJson(File targetFileName) {
        export("instructors", targetFileName, () -> jsonExporter.exportInstructors(targetFileName));
    }

    @Override
    public void exportStudentsDataToJson(File targetFileName) {
        export("students", targetFileName, () -> jsonExporter.exportStudents(targetFileName));
    }

    @Override
    public void exportTeamsDataToJson(File targetFileName) {
        export("teams", targetFileName, () -> jsonExporter.exportTeams(targetFileName));
    }

    @Override
    public List<Student> importStudentDataFromFile(File source) {
        try {
            List<Student> students = csvImporter.importStudents(source, this);
            log.info("Imported {} students from {}", students.size(), source);
            return students;
        } catch (IllegalArgumentException | UncheckedIOException e) {
            log.warn("The import of students from {} stopped: {}", source, e.getMessage());
            throw e;
        }
    }

    private void export(String what, File target, Runnable writeFile) {
        try {
            writeFile.run();
            log.info("Exported the {} to {}", what, target);
        } catch (IllegalArgumentException | UncheckedIOException e) {
            log.warn("The {} were not exported: {}", what, e.getMessage());
            throw e;
        }
    }

    private void checkNewPerson(String id, String name, String email) {
        Checks.requireText(id, "id");
        Checks.requireText(name, "name");
        Checks.requireValidEmail(email);
        if (instructorManager.exists(id)) {
            throw new IllegalArgumentException("An instructor with id " + id + " already exists");
        }
        if (studentManager.exists(id)) {
            throw new IllegalArgumentException("A student with id " + id + " already exists");
        }
        List<Person> persons = new ArrayList<>(instructorManager.getAll());
        persons.addAll(studentManager.getAll());
        for (Person person : persons) {
            if (Checks.sameText(person.getEmail(), email)) {
                throw new IllegalArgumentException("The e-mail " + email + " is already used by " + person.getId());
            }
        }
    }
}

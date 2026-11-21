package patmal.workshops.student.impl.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Person;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

/**
 * The in-memory implementation of WorkshopSystem. The work is done by one manager per area
 * (instructors, students, workshops, teams); this class only checks the rules that involve
 * more than one area and delegates to the managers.
 * <p>
 * Invalid operations throw IllegalArgumentException. Everything is checked before anything
 * is changed, so a rejected operation leaves the system exactly as it was and does not use up an id.
 * <p>
 * These must be unique in the system:
 * <ul>
 *   <li>A person id. One id belongs to one person: a student or an instructor, not both.</li>
 *   <li>A person e-mail.</li>
 *   <li>A workshop name.</li>
 *   <li>A team name.</li>
 * </ul>
 * Names and e-mails are compared ignoring upper/lower case and spaces at the start and the end.
 * <p>
 * A lookup by an id that does not exist returns null (or an empty list).
 */
public class WorkshopSystemImpl implements WorkshopSystem {

    private final InstructorManager instructorManager = new InstructorManager();
    private final StudentManager studentManager = new StudentManager();
    private final WorkshopManager workshopManager = new WorkshopManager();
    private final TeamManager teamManager = new TeamManager(studentManager);

    // ---------- Creating things ----------

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        checkNewPerson(id, name, email);
        return instructorManager.create(id, name, email);
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        checkNewPerson(id, name, email);
        return studentManager.create(id, name, email);
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        return workshopManager.create(name, description);
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        InstructorImpl newInstructor = instructorManager.find(instructor);
        WorkshopImpl workshopToAssociate = workshopManager.find(workshop);
        instructorManager.assignWorkshop(newInstructor, workshopToAssociate);
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        WorkshopImpl teamWorkshop = workshopManager.find(workshop);
        return teamManager.create(name, teamWorkshop, members);
    }

    // ---------- Querying ----------

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

    // ---------- Export ----------

    @Override
    public void exportInstructorsDataToJson(File targetFileName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void exportStudentsDataToJson(File targetFileName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void exportTeamsDataToJson(File targetFileName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ---------- Helpers ----------

    // A person is identified by the id, so one id belongs to one person only - a student or an instructor.
    // Two persons cannot have the same e-mail either.
    private void checkNewPerson(String id, String name, String email) {
        Checks.requireText(id, "id");
        Checks.requireText(name, "name");
        Checks.requireText(email, "e-mail");
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

package patmal.workshops.student.impl.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Person;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.StudentImpl;
import patmal.workshops.student.impl.model.TeamImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

/**
 * The in-memory implementation of WorkshopSystem. All the objects are kept in maps, by their id.
 * <p>
 * Invalid operations throw IllegalArgumentException. Everything is checked before anything
 * is changed, so a rejected operation leaves the system exactly as it was and does not use up an id.
 * <p>
 * These must be unique in the system. The instructions do not say so, but with duplicates
 * there would be no way to tell two things apart:
 * <ul>
 *   <li>A person id. One id belongs to one person: a student or an instructor, not both.</li>
 *   <li>A person e-mail.</li>
 *   <li>A workshop name.</li>
 *   <li>A team name.</li>
 * </ul>
 * Person names do not have to be unique - two different people can have the same name.
 * Names and e-mails are compared ignoring upper/lower case and spaces at the start and the end,
 * so "Java Workshop" and " java workshop" are the same name.
 * <p>
 * Ids, names and e-mails must not be null or empty, and a team must have at least one member.
 * <p>
 * Unknown keys: a lookup by an id that does not exist does not throw an exception.
 * getInstructor, getStudent, getWorkshop and getTeam return null,
 * and getStudentsOfInstructor and getStudentsOfWorkshop return an empty list.
 */
public class WorkshopSystemImpl implements WorkshopSystem {

    // LinkedHashMap keeps the objects in the order they were created
    private final Map<String, InstructorImpl> instructors = new LinkedHashMap<>();
    private final Map<String, StudentImpl> students = new LinkedHashMap<>();
    private final Map<Integer, WorkshopImpl> workshops = new LinkedHashMap<>();
    private final Map<Integer, TeamImpl> teams = new LinkedHashMap<>();

    // The id that the next new workshop / team will get
    private int nextWorkshopId = 1;
    private int nextTeamId = 1;

    // ---------- Creating things ----------

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        checkNewPerson(id, name, email);
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        return instructor;
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        checkNewPerson(id, name, email);
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        return student;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        requireText(name, "workshop name");
        for (WorkshopImpl existing : workshops.values()) {
            if (sameText(existing.getName(), name)) {
                throw new IllegalArgumentException("A workshop named " + existing.getName() + " already exists");
            }
        }
        WorkshopImpl workshop = new WorkshopImpl(nextWorkshopId, name, description);
        workshops.put(workshop.getId(), workshop);
        nextWorkshopId++;
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        InstructorImpl newInstructor = findInstructor(instructor);
        WorkshopImpl workshopToAssociate = findWorkshop(workshop);

        Instructor previousInstructor = workshopToAssociate.getInstructor();
        // Already associated with this instructor - nothing to change
        // (otherwise the workshop would move to the end of the instructor's list)
        if (newInstructor.equals(previousInstructor)) {
            return;
        }

        // A workshop has only one instructor, so remove it from its previous instructor (if it had one)
        if (previousInstructor != null) {
            instructors.get(previousInstructor.getId()).removeWorkshop(workshopToAssociate);
        }

        // Update both sides of the relation
        workshopToAssociate.setInstructor(newInstructor);
        newInstructor.addWorkshop(workshopToAssociate);
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        requireText(name, "team name");
        for (TeamImpl existing : teams.values()) {
            if (sameText(existing.getName(), name)) {
                throw new IllegalArgumentException("A team named " + existing.getName() + " already exists");
            }
        }
        WorkshopImpl teamWorkshop = findWorkshop(workshop);
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("A team must have at least one member");
        }

        // First check all the members. Nothing is changed until we know that the whole team is valid.
        List<StudentImpl> teamStudents = new ArrayList<>();
        for (String studentId : members) {
            StudentImpl student = students.get(studentId);
            if (student == null) {
                throw new IllegalArgumentException("There is no student with id " + studentId);
            }
            if (student.hasTeam()) {
                throw new IllegalArgumentException("Student " + studentId + " is already in team " + student.getTeamName());
            }
            if (teamStudents.contains(student)) {
                throw new IllegalArgumentException("Student " + studentId + " is listed more than once");
            }
            teamStudents.add(student);
        }

        // The team is valid - create it and update both sides of the relation
        TeamImpl team = new TeamImpl(nextTeamId, name, teamWorkshop);
        nextTeamId++;
        for (StudentImpl student : teamStudents) {
            team.addStudent(student);
            student.setTeam(team);
        }
        teams.put(team.getId(), team);
        return team;
    }

    // ---------- Querying ----------

    @Override
    public List<Instructor> getInstructors() {
        return new ArrayList<>(instructors.values());
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructors.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(students.values());
    }

    @Override
    public Student getStudent(String id) {
        return students.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshops.values());
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshops.get(id);
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public Team getTeam(Integer id) {
        return teams.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        List<Student> freeStudents = new ArrayList<>();
        for (StudentImpl student : students.values()) {
            if (!student.hasTeam()) {
                freeStudents.add(student);
            }
        }
        return freeStudents;
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        List<Student> result = new ArrayList<>();
        InstructorImpl instructor = instructors.get(instructorId);
        if (instructor == null) {
            return result;
        }
        for (Workshop workshop : instructor.getWorkshops()) {
            result.addAll(getStudentsOfWorkshop(workshop.getId()));
        }
        return result;
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        List<Student> result = new ArrayList<>();
        for (TeamImpl team : teams.values()) {
            if (team.getWorkshop().getId().equals(workshopId)) {
                result.addAll(team.getStudents());
            }
        }
        return result;
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

    // The caller gives us Instructor / Workshop objects. We find our own objects with the same id,
    // so that we can update them. An object that is not in the system is an invalid argument.

    private InstructorImpl findInstructor(Instructor instructor) {
        if (instructor == null) {
            throw new IllegalArgumentException("The instructor must not be null");
        }
        InstructorImpl found = instructors.get(instructor.getId());
        if (found == null) {
            throw new IllegalArgumentException("There is no instructor with id " + instructor.getId());
        }
        return found;
    }

    private WorkshopImpl findWorkshop(Workshop workshop) {
        if (workshop == null) {
            throw new IllegalArgumentException("The workshop must not be null");
        }
        WorkshopImpl found = workshops.get(workshop.getId());
        if (found == null) {
            throw new IllegalArgumentException("There is no workshop with id " + workshop.getId());
        }
        return found;
    }

    // A person is identified by the id, so one id belongs to one person only - a student or an instructor.
    // Two persons cannot have the same e-mail either.
    private void checkNewPerson(String id, String name, String email) {
        requireText(id, "id");
        requireText(name, "name");
        requireText(email, "e-mail");
        if (instructors.containsKey(id)) {
            throw new IllegalArgumentException("An instructor with id " + id + " already exists");
        }
        if (students.containsKey(id)) {
            throw new IllegalArgumentException("A student with id " + id + " already exists");
        }
        List<Person> persons = new ArrayList<>(instructors.values());
        persons.addAll(students.values());
        for (Person person : persons) {
            if (sameText(person.getEmail(), email)) {
                throw new IllegalArgumentException("The e-mail " + email + " is already used by " + person.getId());
            }
        }
    }

    private static void requireText(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The " + what + " must not be empty");
        }
    }

    // Names and e-mails: upper/lower case and spaces at the start and the end do not matter
    private static boolean sameText(String first, String second) {
        return first.strip().equalsIgnoreCase(second.strip());
    }
}

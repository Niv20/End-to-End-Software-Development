package patmal.workshops.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.model.InstructorImpl;
import patmal.workshops.model.StudentImpl;
import patmal.workshops.model.TeamImpl;
import patmal.workshops.model.WorkshopImpl;

/**
 * The in-memory implementation of WorkshopSystem. All the objects are kept in maps, by their id.
 * <p>
 * Invalid operations throw IllegalArgumentException. Everything is checked before anything
 * is changed, so a rejected operation leaves the system exactly as it was and does not use up an id.
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
        if (instructors.containsKey(id)) {
            throw new IllegalArgumentException("An instructor with id " + id + " already exists");
        }
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        return instructor;
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (students.containsKey(id)) {
            throw new IllegalArgumentException("A student with id " + id + " already exists");
        }
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        return student;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl workshop = new WorkshopImpl(nextWorkshopId, name, description);
        workshops.put(workshop.getId(), workshop);
        nextWorkshopId++;
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        InstructorImpl newInstructor = findInstructor(instructor);
        WorkshopImpl workshopToAssociate = findWorkshop(workshop);

        // A workshop has only one instructor, so remove it from its previous instructor (if it had one)
        Instructor previousInstructor = workshopToAssociate.getInstructor();
        if (previousInstructor != null) {
            instructors.get(previousInstructor.getId()).removeWorkshop(workshopToAssociate);
        }

        // Update both sides of the relation
        workshopToAssociate.setInstructor(newInstructor);
        newInstructor.addWorkshop(workshopToAssociate);
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        WorkshopImpl teamWorkshop = findWorkshop(workshop);

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

    // ---------- Helpers ----------

    // The caller gives us Instructor / Workshop objects. We find our own objects with the same id,
    // so that we can update them. An object that is not in the system is an invalid argument.

    private InstructorImpl findInstructor(Instructor instructor) {
        InstructorImpl found = instructors.get(instructor.getId());
        if (found == null) {
            throw new IllegalArgumentException("There is no instructor with id " + instructor.getId());
        }
        return found;
    }

    private WorkshopImpl findWorkshop(Workshop workshop) {
        WorkshopImpl found = workshops.get(workshop.getId());
        if (found == null) {
            throw new IllegalArgumentException("There is no workshop with id " + workshop.getId());
        }
        return found;
    }
}

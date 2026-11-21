package patmal.workshops.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

/**
 * A team of students that take a workshop together.
 * The id is given by the system when the team is created.
 */
public class TeamImpl implements Team {

    private final Integer id;
    private final String name;
    private final Workshop workshop;
    private final List<Student> students = new ArrayList<>();

    public TeamImpl(Integer id, String name, Workshop workshop) {
        this.id = id;
        this.name = name;
        this.workshop = workshop;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Student> getStudents() {
        // Return a copy, so whoever gets the list cannot change our own list
        return new ArrayList<>(students);
    }

    @Override
    public Workshop getWorkshop() {
        return workshop;
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    /**
     * Two teams are equal if they have the same id. The list of students is not part of the
     * comparison: it can change, and each student points back to its team, so comparing the
     * students would compare the team again and never end.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TeamImpl otherTeam = (TeamImpl) other;
        return Objects.equals(id, otherTeam.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

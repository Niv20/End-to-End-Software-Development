package patmal.workshops.model;

import java.util.ArrayList;
import java.util.List;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;

/**
 * A student. A student is in at most one team; a student without a team is a "free student".
 * The team name, the instructor and the team mates are not stored here,
 * they are always taken from the student's team.
 */
public class StudentImpl extends PersonImpl implements Student {

    // null as long as the student is free (not in any team)
    private Team team;

    public StudentImpl(String id, String name, String email) {
        super(id, name, email);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public boolean hasTeam() {
        return team != null;
    }

    @Override
    public String getTeamName() {
        if (team == null) {
            return null;
        }
        return team.getName();
    }

    @Override
    public Instructor getInstructor() {
        if (team == null) {
            return null;
        }
        return team.getWorkshop().getInstructor();
    }

    @Override
    public List<Student> getTeamMates() {
        List<Student> teamMates = new ArrayList<>();
        if (team == null) {
            return teamMates;
        }
        for (Student member : team.getStudents()) {
            // Skip this student - only the other members of the team are team mates
            if (member != this) {
                teamMates.add(member);
            }
        }
        return teamMates;
    }
}

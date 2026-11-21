package patmal.workshops.student.impl.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.student.impl.model.StudentImpl;
import patmal.workshops.student.impl.model.TeamImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

class TeamManager {

    // LinkedHashMap keeps the teams in the order they were created
    private final Map<Integer, TeamImpl> teams = new LinkedHashMap<>();
    private final StudentManager studentManager;

    // The id that the next new team will get
    private int nextId = 1;

    TeamManager(StudentManager studentManager) {
        this.studentManager = studentManager;
    }

    TeamImpl create(String name, WorkshopImpl workshop, List<String> members) {
        Checks.requireText(name, "team name");
        for (TeamImpl existing : teams.values()) {
            if (Checks.sameText(existing.getName(), name)) {
                throw new IllegalArgumentException("A team named " + existing.getName() + " already exists");
            }
        }
        List<StudentImpl> teamStudents = findMembers(members);

        // The team is valid - create it and update both sides of the relation
        TeamImpl team = new TeamImpl(nextId, name, workshop);
        nextId++;
        for (StudentImpl student : teamStudents) {
            team.addStudent(student);
            student.setTeam(team);
        }
        teams.put(team.getId(), team);
        return team;
    }

    TeamImpl get(Integer id) {
        return teams.get(id);
    }

    List<Team> getAll() {
        return new ArrayList<>(teams.values());
    }

    List<Team> getTeamsOfWorkshop(Integer workshopId) {
        List<Team> result = new ArrayList<>();
        for (TeamImpl team : teams.values()) {
            if (team.getWorkshop().getId().equals(workshopId)) {
                result.add(team);
            }
        }
        return result;
    }

    List<Student> getStudentsOfWorkshop(Integer workshopId) {
        List<Student> result = new ArrayList<>();
        for (Team team : getTeamsOfWorkshop(workshopId)) {
            result.addAll(team.getStudents());
        }
        return result;
    }

    // Checks all the members first. Nothing is changed until we know that the whole team is valid.
    private List<StudentImpl> findMembers(List<String> members) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("A team must have at least one member");
        }
        List<StudentImpl> teamStudents = new ArrayList<>();
        for (String studentId : members) {
            StudentImpl student = studentManager.get(studentId);
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
        return teamStudents;
    }
}

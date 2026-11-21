package patmal.workshops.student.impl.logic;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Person;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

/**
 * Writes the data of the system to JSON files.
 * The records at the end describe the JSON documents. Each document is a tree: an object
 * never points back to one of its ancestors, so there are no cycles.
 */
class JsonExporter {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final InstructorManager instructorManager;
    private final StudentManager studentManager;
    private final TeamManager teamManager;

    JsonExporter(InstructorManager instructorManager, StudentManager studentManager, TeamManager teamManager) {
        this.instructorManager = instructorManager;
        this.studentManager = studentManager;
        this.teamManager = teamManager;
    }

    void exportInstructors(File target) {
        List<InstructorJson> result = new ArrayList<>();
        for (Instructor instructor : instructorManager.getAll()) {
            List<WorkshopWithTeamsJson> workshops = new ArrayList<>();
            for (Workshop workshop : instructor.getWorkshops()) {
                workshops.add(new WorkshopWithTeamsJson(workshop.getId(), workshop.getName(),
                        workshop.getDescription(), teamsJson(workshop)));
            }
            result.add(new InstructorJson(instructor.getId(), instructor.getName(), instructor.getEmail(), workshops));
        }
        write(result, target);
    }

    void exportStudents(File target) {
        Map<String, Team> teamOfStudent = new HashMap<>();
        for (Team team : teamManager.getAll()) {
            for (Student student : team.getStudents()) {
                teamOfStudent.put(student.getId(), team);
            }
        }

        List<StudentJson> result = new ArrayList<>();
        for (Student student : studentManager.getAll()) {
            Team team = teamOfStudent.get(student.getId());
            StudentTeamJson teamJson = team == null ? null
                    : new StudentTeamJson(team.getId(), team.getName(), studentIds(team), workshopJson(team.getWorkshop()));
            result.add(new StudentJson(student.getId(), student.getName(), student.getEmail(), teamJson));
        }
        write(result, target);
    }

    void exportTeams(File target) {
        List<TeamJson> result = new ArrayList<>();
        for (Team team : teamManager.getAll()) {
            result.add(new TeamJson(team.getId(), team.getName(), studentsJson(team), workshopJson(team.getWorkshop())));
        }
        write(result, target);
    }

    private List<TeamWithStudentsJson> teamsJson(Workshop workshop) {
        List<TeamWithStudentsJson> result = new ArrayList<>();
        for (Team team : teamManager.getTeamsOfWorkshop(workshop.getId())) {
            result.add(new TeamWithStudentsJson(team.getId(), team.getName(), studentsJson(team)));
        }
        return result;
    }

    private static WorkshopJson workshopJson(Workshop workshop) {
        Instructor instructor = workshop.getInstructor();
        PersonJson instructorJson = instructor == null ? null : personJson(instructor);
        return new WorkshopJson(workshop.getId(), workshop.getName(), workshop.getDescription(), instructorJson);
    }

    private static List<PersonJson> studentsJson(Team team) {
        List<PersonJson> result = new ArrayList<>();
        for (Student student : team.getStudents()) {
            result.add(personJson(student));
        }
        return result;
    }

    private static List<String> studentIds(Team team) {
        List<String> result = new ArrayList<>();
        for (Student student : team.getStudents()) {
            result.add(student.getId());
        }
        return result;
    }

    private static PersonJson personJson(Person person) {
        return new PersonJson(person.getId(), person.getName(), person.getEmail());
    }

    // An existing file is overwritten, a missing file is created. The folder is not created.
    private static void write(Object data, File target) {
        if (target == null) {
            throw new IllegalArgumentException("The target file must not be null");
        }
        try (Writer writer = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8)) {
            MAPPER.writeValue(writer, data);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the file " + target, e);
        }
    }

    record PersonJson(String id, String name, String email) {
    }

    record InstructorJson(String id, String name, String email, List<WorkshopWithTeamsJson> workshops) {
    }

    record WorkshopWithTeamsJson(Integer id, String name, String description, List<TeamWithStudentsJson> teams) {
    }

    record TeamWithStudentsJson(Integer id, String name, List<PersonJson> students) {
    }

    record StudentJson(String id, String name, String email, StudentTeamJson team) {
    }

    record StudentTeamJson(Integer id, String name, List<String> studentIds, WorkshopJson workshop) {
    }

    record WorkshopJson(Integer id, String name, String description, PersonJson instructor) {
    }

    record TeamJson(Integer id, String name, List<PersonJson> students, WorkshopJson workshop) {
    }
}

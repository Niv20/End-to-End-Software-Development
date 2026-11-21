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

    void exportInstructors(List<Instructor> instructors, List<Team> teams, File target) {
        List<InstructorJson> result = new ArrayList<>();
        for (Instructor instructor : instructors) {
            List<WorkshopWithTeamsJson> workshops = new ArrayList<>();
            for (Workshop workshop : instructor.getWorkshops()) {
                workshops.add(new WorkshopWithTeamsJson(workshop.getId(), workshop.getName(),
                        workshop.getDescription(), teamsOf(workshop, teams)));
            }
            result.add(new InstructorJson(instructor.getId(), instructor.getName(), instructor.getEmail(), workshops));
        }
        write(result, target);
    }

    void exportStudents(List<Student> students, List<Team> teams, File target) {
        Map<String, Team> teamOfStudent = new HashMap<>();
        for (Team team : teams) {
            for (Student student : team.getStudents()) {
                teamOfStudent.put(student.getId(), team);
            }
        }

        List<StudentJson> result = new ArrayList<>();
        for (Student student : students) {
            Team team = teamOfStudent.get(student.getId());
            StudentTeamJson teamJson = null;
            if (team != null) {
                List<String> studentIds = new ArrayList<>();
                for (Student member : team.getStudents()) {
                    studentIds.add(member.getId());
                }
                teamJson = new StudentTeamJson(team.getId(), team.getName(), studentIds, toJson(team.getWorkshop()));
            }
            result.add(new StudentJson(student.getId(), student.getName(), student.getEmail(), teamJson));
        }
        write(result, target);
    }

    void exportTeams(List<Team> teams, File target) {
        List<TeamJson> result = new ArrayList<>();
        for (Team team : teams) {
            result.add(new TeamJson(team.getId(), team.getName(), toJson(team.getStudents()), toJson(team.getWorkshop())));
        }
        write(result, target);
    }

    private static List<TeamWithStudentsJson> teamsOf(Workshop workshop, List<Team> teams) {
        List<TeamWithStudentsJson> result = new ArrayList<>();
        for (Team team : teams) {
            if (team.getWorkshop().getId().equals(workshop.getId())) {
                result.add(new TeamWithStudentsJson(team.getId(), team.getName(), toJson(team.getStudents())));
            }
        }
        return result;
    }

    private static WorkshopJson toJson(Workshop workshop) {
        Instructor instructor = workshop.getInstructor();
        PersonJson instructorJson = instructor == null ? null : toJson(instructor);
        return new WorkshopJson(workshop.getId(), workshop.getName(), workshop.getDescription(), instructorJson);
    }

    private static List<PersonJson> toJson(List<Student> students) {
        List<PersonJson> result = new ArrayList<>();
        for (Student student : students) {
            result.add(toJson(student));
        }
        return result;
    }

    private static PersonJson toJson(Person person) {
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

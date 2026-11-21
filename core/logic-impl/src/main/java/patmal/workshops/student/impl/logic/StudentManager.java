package patmal.workshops.student.impl.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import patmal.workshops.api.model.Student;
import patmal.workshops.student.impl.model.StudentImpl;

class StudentManager {

    // LinkedHashMap keeps the students in the order they were created
    private final Map<String, StudentImpl> students = new LinkedHashMap<>();

    StudentImpl create(String id, String name, String email) {
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        return student;
    }

    boolean exists(String id) {
        return students.containsKey(id);
    }

    StudentImpl get(String id) {
        return students.get(id);
    }

    List<Student> getAll() {
        return new ArrayList<>(students.values());
    }

    List<Student> getFree() {
        List<Student> freeStudents = new ArrayList<>();
        for (StudentImpl student : students.values()) {
            if (!student.hasTeam()) {
                freeStudents.add(student);
            }
        }
        return freeStudents;
    }
}

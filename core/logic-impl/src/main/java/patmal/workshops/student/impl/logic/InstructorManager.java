package patmal.workshops.student.impl.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

class InstructorManager {

    // LinkedHashMap keeps the instructors in the order they were created
    private final Map<String, InstructorImpl> instructors = new LinkedHashMap<>();

    InstructorImpl create(String id, String name, String email) {
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        return instructor;
    }

    boolean exists(String id) {
        return instructors.containsKey(id);
    }

    InstructorImpl get(String id) {
        return instructors.get(id);
    }

    List<Instructor> getAll() {
        return new ArrayList<>(instructors.values());
    }

    // Finds our own object with the same id, so that we can update it
    InstructorImpl find(Instructor instructor) {
        if (instructor == null) {
            throw new IllegalArgumentException("The instructor must not be null");
        }
        InstructorImpl found = instructors.get(instructor.getId());
        if (found == null) {
            throw new IllegalArgumentException("There is no instructor with id " + instructor.getId());
        }
        return found;
    }

    void assignWorkshop(InstructorImpl instructor, WorkshopImpl workshop) {
        Instructor previousInstructor = workshop.getInstructor();
        // Already associated with this instructor - nothing to change
        // (otherwise the workshop would move to the end of the instructor's list)
        if (instructor.equals(previousInstructor)) {
            return;
        }

        // A workshop has only one instructor, so remove it from its previous instructor (if it had one)
        if (previousInstructor != null) {
            instructors.get(previousInstructor.getId()).removeWorkshop(workshop);
        }

        // Update both sides of the relation
        workshop.setInstructor(instructor);
        instructor.addWorkshop(workshop);
    }
}

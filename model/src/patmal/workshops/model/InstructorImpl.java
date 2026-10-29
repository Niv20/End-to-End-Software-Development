package patmal.workshops.model;

import java.util.ArrayList;
import java.util.List;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

/**
 * An instructor and the workshops that this instructor teaches.
 */
public class InstructorImpl extends PersonImpl implements Instructor {

    private final List<Workshop> workshops = new ArrayList<>();

    public InstructorImpl(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public List<Workshop> getWorkshops() {
        // Return a copy, so whoever gets the list cannot change our own list
        return new ArrayList<>(workshops);
    }

    public void addWorkshop(Workshop workshop) {
        workshops.add(workshop);
    }

    public void removeWorkshop(Workshop workshop) {
        workshops.remove(workshop);
    }
}

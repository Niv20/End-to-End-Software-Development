package patmal.workshops.student.impl.logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.WorkshopImpl;

class WorkshopManager {

    // LinkedHashMap keeps the workshops in the order they were created
    private final Map<Integer, WorkshopImpl> workshops = new LinkedHashMap<>();

    // The id that the next new workshop will get
    private int nextId = 1;

    WorkshopImpl create(String name, String description) {
        Checks.requireText(name, "workshop name");
        for (WorkshopImpl existing : workshops.values()) {
            if (Checks.sameText(existing.getName(), name)) {
                throw new IllegalArgumentException("A workshop named " + existing.getName() + " already exists");
            }
        }
        WorkshopImpl workshop = new WorkshopImpl(nextId, name, description);
        workshops.put(workshop.getId(), workshop);
        nextId++;
        return workshop;
    }

    WorkshopImpl get(Integer id) {
        return workshops.get(id);
    }

    List<Workshop> getAll() {
        return new ArrayList<>(workshops.values());
    }

    // The caller gives us a Workshop object. We find our own object with the same id,
    // so that we can update it. A workshop that is not in the system is an invalid argument.
    WorkshopImpl find(Workshop workshop) {
        if (workshop == null) {
            throw new IllegalArgumentException("The workshop must not be null");
        }
        WorkshopImpl found = workshops.get(workshop.getId());
        if (found == null) {
            throw new IllegalArgumentException("There is no workshop with id " + workshop.getId());
        }
        return found;
    }
}

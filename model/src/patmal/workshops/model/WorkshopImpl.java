package patmal.workshops.model;

import java.util.Objects;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

/**
 * A workshop. The id is given by the system when the workshop is created.
 * The instructor is null until the workshop is associated with an instructor.
 */
public class WorkshopImpl implements Workshop {

    private final Integer id;
    private final String name;
    private final String description;
    private Instructor instructor;

    public WorkshopImpl(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
    public String getDescription() {
        return description;
    }

    @Override
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    /**
     * Two workshops are equal if they have the same id. The instructor is not part of the
     * comparison, because it can change after the workshop is created.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        WorkshopImpl otherWorkshop = (WorkshopImpl) other;
        return Objects.equals(id, otherWorkshop.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

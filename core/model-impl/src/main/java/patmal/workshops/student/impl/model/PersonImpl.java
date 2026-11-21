package patmal.workshops.student.impl.model;

import java.util.Objects;

import patmal.workshops.api.model.Person;

/**
 * The data that every person in the system has: id, name and e-mail.
 * InstructorImpl and StudentImpl extend this class.
 */
public abstract class PersonImpl implements Person {

    private final String id;
    private final String name;
    private final String email;

    protected PersonImpl(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    /**
     * Two persons are equal if they are of the same kind (both students or both instructors)
     * and have the same id. The id is what identifies a person in the system.
     * A student and an instructor with the same id are not equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PersonImpl otherPerson = (PersonImpl) other;
        return Objects.equals(id, otherPerson.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

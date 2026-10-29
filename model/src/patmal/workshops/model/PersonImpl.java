package patmal.workshops.model;

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
}

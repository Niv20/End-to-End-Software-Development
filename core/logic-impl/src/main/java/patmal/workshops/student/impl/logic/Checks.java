package patmal.workshops.student.impl.logic;

/**
 * Small input checks that are shared by the managers.
 */
final class Checks {

    private Checks() {
    }

    static void requireText(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The " + what + " must not be empty");
        }
    }

    // Names and e-mails: upper/lower case and spaces at the start and the end do not matter
    static boolean sameText(String first, String second) {
        return first.strip().equalsIgnoreCase(second.strip());
    }
}

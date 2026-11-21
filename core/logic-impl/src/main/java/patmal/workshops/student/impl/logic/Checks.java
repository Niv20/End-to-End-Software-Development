package patmal.workshops.student.impl.logic;

import org.apache.commons.validator.routines.EmailValidator;

/**
 * Input checks that are used by more than one class.
 */
final class Checks {

    private Checks() {
    }

    static void requireText(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The " + what + " must not be empty");
        }
    }

    static void requireValidEmail(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new IllegalArgumentException("The e-mail " + email + " is not a valid address");
        }
    }

    // Names and e-mails: upper/lower case and spaces at the start and the end do not matter
    static boolean sameText(String first, String second) {
        return first.strip().equalsIgnoreCase(second.strip());
    }
}

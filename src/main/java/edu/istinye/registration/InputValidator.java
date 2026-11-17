package edu.istinye.registration;

import java.util.Optional;

/**
 * Checks that a request's values are in their allowed ranges (R1, R3, R4).
 * It does NOT apply business rules like the credit limit; that is EligibilityChecker's job.
 */
public class InputValidator {

    public static final double MIN_GPA = 0.00;
    public static final double MAX_GPA = 4.00;
    public static final int MIN_SEMESTER = 1;
    public static final int MAX_SEMESTER = 8;
    public static final int MIN_COURSE_CREDITS = 1;
    public static final int MAX_COURSE_CREDITS = 10;
    // Fix D4 (R4b): 36 highest limit + 6 overload = 42
    public static final int MAX_REGISTERED_CREDITS = 42;

    /**
     * Validates a request.
     *
     * Optional is a box that is either empty or holds one value.
     * Here: empty  = the input is valid,
     *       a text = the input is invalid, and the text explains why.
     *
     * @param request the request to check (may be null)
     * @return Optional.empty() if valid, otherwise the error message
     */
    public Optional<String> validate(RegistrationRequest request) {
        // Missing data (negative testing: null inputs)
        if (request == null) {
            return Optional.of("Request is missing");
        }
        if (request.courseCode() == null || request.courseCode().isBlank()) {
            return Optional.of("Course code is missing");
        }
        if (request.registeredCourses() == null) {
            return Optional.of("Registered course list is missing");
        }

        // R1: GPA range
        // Fix D3: NaN is not caught by range comparisons (every comparison with NaN is false)
        if (Double.isNaN(request.gpa())
                || request.gpa() < MIN_GPA || request.gpa() > MAX_GPA) {
            return Optional.of("GPA must be between 0.00 and 4.00");
        }

        // R4: semester range
        if (request.semester() < MIN_SEMESTER || request.semester() > MAX_SEMESTER) {
            return Optional.of("Semester must be between 1 and 8");
        }

        // R3: first-semester students have no GPA yet, so it must be exactly 0.00
        if (request.semester() == 1 && request.gpa() != 0.00) {
            return Optional.of("First-semester students must have GPA 0.00");
        }

        // R4: credit ranges
        // Fix D4 (R4b): registered credits must also have an upper limit
        if (request.registeredCredits() < 0
                || request.registeredCredits() > MAX_REGISTERED_CREDITS) {
            return Optional.of("Registered credits must be between 0 and 42");
        }
        if (request.courseCredits() < MIN_COURSE_CREDITS
                || request.courseCredits() > MAX_COURSE_CREDITS) {
            return Optional.of("Course credits must be between 1 and 10");
        }

        return Optional.empty(); // everything is valid
    }
}

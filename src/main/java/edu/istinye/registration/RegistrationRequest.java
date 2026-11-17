package edu.istinye.registration;

import java.util.Set;

/**
 * All the inputs for one registration check.
 *
 * A "record" is a short way to write a class that only carries data.
 * Java automatically creates the constructor and one getter per field
 * (for example request.gpa(), request.semester()).
 *
 * No validation happens here on purpose: the record must be able to hold
 * invalid values (e.g. GPA 5.0) so that negative tests can create them.
 * Validation is done by InputValidator.
 *
 * @param gpa                current GPA, expected 0.00 - 4.00 (R1)
 * @param semester           current semester, expected 1 - 8 (R4)
 * @param registeredCredits  credits already registered this semester, expected >= 0 (R4)
 * @param courseCode         code of the requested course, e.g. "SWE411"
 * @param courseCredits      credits of the requested course, expected 1 - 10 (R4)
 * @param prerequisitePassed true if the student passed the course's prerequisite (R5)
 * @param advisorApproval    true if the advisor approved an overload (R6)
 * @param registeredCourses  codes of courses already registered this semester (R7)
 */
public record RegistrationRequest(
        double gpa,
        int semester,
        int registeredCredits,
        String courseCode,
        int courseCredits,
        boolean prerequisitePassed,
        boolean advisorApproval,
        Set<String> registeredCourses) {
}

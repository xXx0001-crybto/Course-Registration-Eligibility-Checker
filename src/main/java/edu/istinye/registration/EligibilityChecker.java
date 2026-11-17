package edu.istinye.registration;

import java.util.Locale;
import java.util.Optional;

/**
 * The main business logic: decides whether a registration request is approved.
 *
 * Rules are checked in a fixed order, and the first failing rule decides the result:
 *   1. invalid input   -> INVALID_INPUT   (R1, R3, R4)
 *   2. duplicate course -> DENIED         (R7)
 *   3. prerequisite     -> DENIED         (R5)
 *   4. credit limit     -> APPROVED / DENIED (R2, R3, R6, R8)
 */
public class EligibilityChecker {

    // The checker uses the other two classes instead of repeating their logic.
    // They are passed in through the constructor, so tests can create them directly.
    private final InputValidator validator;
    private final CreditLimitPolicy policy;

    public EligibilityChecker(InputValidator validator, CreditLimitPolicy policy) {
        this.validator = validator;
        this.policy = policy;
    }

    /** Convenience constructor with the default validator and policy. */
    public EligibilityChecker() {
        this(new InputValidator(), new CreditLimitPolicy());
    }

    /**
     * Checks one registration request.
     *
     * @param request the student's request
     * @return the decision and the reason for it
     */
    public RegistrationResult check(RegistrationRequest request) {
        // 1. Input validation (R1, R3, R4)
        Optional<String> error = validator.validate(request);
        if (error.isPresent()) {
            return new RegistrationResult(Decision.INVALID_INPUT, error.get());
        }

        // 2. Duplicate registration (R7)
        // Fix D1 + D2: compare codes ignoring upper/lower case (D1) and surrounding spaces (D2)
        String requestedCode = normalize(request.courseCode());
        for (String registeredCode : request.registeredCourses()) {
            if (normalize(registeredCode).equals(requestedCode)) {
                return new RegistrationResult(Decision.DENIED,
                        "Course " + request.courseCode() + " is already registered");
            }
        }

        // 3. Prerequisite (R5) - no override possible
        if (!request.prerequisitePassed()) {
            return new RegistrationResult(Decision.DENIED, "Prerequisite not passed");
        }

        // 4. Credit limit (R2, R3, R6, R8)
        int limit = policy.getLimit(request.gpa(), request.semester());
        int total = request.registeredCredits() + request.courseCredits();

        // R8: exactly at the limit is allowed (<=, not <)
        if (total <= limit) {
            return new RegistrationResult(Decision.APPROVED,
                    "Within credit limit (" + total + "/" + limit + ")");
        }

        // Over the limit: try the advisor overload (R6)
        if (!request.advisorApproval()) {
            return new RegistrationResult(Decision.DENIED,
                    "Credit limit exceeded (" + total + "/" + limit + ") without advisor approval");
        }
        if (!policy.isOverloadEligible(request.gpa())) {
            return new RegistrationResult(Decision.DENIED,
                    "GPA too low for an overload");
        }
        int maxWithOverload = limit + CreditLimitPolicy.OVERLOAD_ALLOWANCE;
        if (total <= maxWithOverload) {
            return new RegistrationResult(Decision.APPROVED,
                    "Approved with advisor overload (" + total + "/" + maxWithOverload + ")");
        }
        return new RegistrationResult(Decision.DENIED,
                "Exceeds maximum overload (" + total + "/" + maxWithOverload + ")");
    }

    /**
     * Fix D1 + D2: puts a course code into one standard form so that
     * "swe411", " SWE411" and "SWE411" are all treated as the same course.
     * Locale.ROOT makes toUpperCase behave the same on every computer
     * (for example, a Turkish locale would turn "i" into a dotted capital "İ").
     *
     * @param code a course code
     * @return the code without surrounding spaces, in upper case
     */
    private String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}

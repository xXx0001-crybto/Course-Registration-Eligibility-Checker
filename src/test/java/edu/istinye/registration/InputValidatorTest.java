package edu.istinye.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for InputValidator.validate(request).
 *
 * validate() returns Optional.empty() when the input is VALID,
 * and an Optional holding an error message when the input is INVALID.
 *
 * Default valid request (only the field under test is changed):
 *   new RegistrationRequest(gpa 2.50, semester 3, registeredCredits 10, courseCode "SWE411",
 *                           courseCredits 4, prerequisitePassed true, advisorApproval false,
 *                           registeredCourses empty set)
 */
class InputValidatorTest {

    private final InputValidator validator = new InputValidator();

    /** Checks that the validator rejected the input (an error message is present). */
    private void assertInvalid(Optional<String> result) {
        assertTrue(result.isPresent(), "Expected an error message, but the input was accepted as valid");
    }

    // ---------------------------------------------------------------------------------
    // Technique: Equivalence Partitioning + Boundary Value Analysis - Requirement R1 (GPA 0.00..4.00).
    // All four values are outside the valid range, so all must be rejected.
    @ParameterizedTest(name = "{0}: GPA {1} -> invalid")
    @DisplayName("EP-G1, BVA-G1, BVA-G12, EP-G5: GPA outside 0.00..4.00 is rejected (R1)")
    @CsvSource({
        "EP-G1,   -1.0",
        "BVA-G1,  -0.01",
        "BVA-G12, 4.01",
        "EP-G5,   5.0"
    })
    void gpaOutOfRangeIsInvalid(String testId, double gpa) {
        RegistrationRequest request = new RegistrationRequest(gpa, 3, 10, "SWE411", 4, true, false, Set.of());
        assertInvalid(validator.validate(request));
    }

    // ---------------------------------------------------------------------------------
    // Technique: Equivalence Partitioning - Requirement R4 (course credits 1..10).
    // One value below, one inside and one above the valid range.
    // shouldBeValid = true means validate() must return Optional.empty().
    @ParameterizedTest(name = "{0}: course credits {1} -> valid = {2}")
    @DisplayName("EP-C1..EP-C3: course credit classes (R4)")
    @CsvSource({
        "EP-C1, -2, false",
        "EP-C2,  5, true",
        "EP-C3, 11, false"
    })
    void courseCreditClasses(String testId, int courseCredits, boolean shouldBeValid) {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, "SWE411", courseCredits, true, false, Set.of());
        Optional<String> result = validator.validate(request);
        assertEquals(shouldBeValid, result.isEmpty(), "Validator returned: " + result);
    }

    // Technique: Boundary Value Analysis - Requirement R4 (course credits 1..10).
    // Values at and next to both edges: 0/1/2 and 9/10/11.
    @ParameterizedTest(name = "{0}: course credits {1} -> valid = {2}")
    @DisplayName("BVA-C1..BVA-C6: course credit boundaries (R4)")
    @CsvSource({
        "BVA-C1,  0, false",
        "BVA-C2,  1, true",
        "BVA-C3,  2, true",
        "BVA-C4,  9, true",
        "BVA-C5, 10, true",
        "BVA-C6, 11, false"
    })
    void courseCreditBoundaries(String testId, int courseCredits, boolean shouldBeValid) {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, "SWE411", courseCredits, true, false, Set.of());
        Optional<String> result = validator.validate(request);
        assertEquals(shouldBeValid, result.isEmpty(), "Validator returned: " + result);
    }

    // ---------------------------------------------------------------------------------
    // Technique: Boundary Value Analysis - new Requirement R4b (registered credits 0..42).
    // BVA-R4 (43) is EXPECTED TO FAIL: the code has no upper limit for registered credits.
    @ParameterizedTest(name = "{0}: registered credits {1} -> valid = {2}")
    @DisplayName("BVA-R1..BVA-R4: registered credit boundaries (R4b)")
    @CsvSource({
        "BVA-R1, -1, false",
        "BVA-R2,  0, true",
        "BVA-R3, 42, true",
        "BVA-R4, 43, false"
    })
    void registeredCreditBoundaries(String testId, int registeredCredits, boolean shouldBeValid) {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, registeredCredits, "SWE411", 4, true, false, Set.of());
        Optional<String> result = validator.validate(request);
        assertEquals(shouldBeValid, result.isEmpty(), "Validator returned: " + result);
    }

    // ---------------------------------------------------------------------------------
    // Technique: Negative testing - missing data (null / empty values).

    @Test
    @DisplayName("NEG-1: course code null -> invalid")
    void courseCodeNullIsInvalid() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, null, 4, true, false, Set.of());
        assertInvalid(validator.validate(request));
    }

    @Test
    @DisplayName("NEG-2: course code empty \"\" -> invalid")
    void courseCodeEmptyIsInvalid() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, "", 4, true, false, Set.of());
        assertInvalid(validator.validate(request));
    }

    @Test
    @DisplayName("NEG-3: registered courses null -> invalid")
    void registeredCoursesNullIsInvalid() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, "SWE411", 4, true, false, null);
        assertInvalid(validator.validate(request));
    }

    @Test
    @DisplayName("NEG-4: request null -> invalid")
    void requestNullIsInvalid() {
        assertInvalid(validator.validate(null));
    }

    // Technique: Negative testing - Requirement R4 (semester 1..8), values just outside the range.
    @ParameterizedTest(name = "{0}: semester {1} -> invalid")
    @DisplayName("NEG-5, NEG-6: semester outside 1..8 is rejected (R4)")
    @CsvSource({
        "NEG-5, 0",
        "NEG-6, 9"
    })
    void semesterOutOfRangeIsInvalid(String testId, int semester) {
        RegistrationRequest request = new RegistrationRequest(2.50, semester, 10, "SWE411", 4, true, false, Set.of());
        assertInvalid(validator.validate(request));
    }

    // Technique: Negative testing - Requirement R4 (registered credits cannot be negative).
    @Test
    @DisplayName("NEG-7: registered credits -1 -> invalid (R4)")
    void negativeRegisteredCreditsIsInvalid() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, -1, "SWE411", 4, true, false, Set.of());
        assertInvalid(validator.validate(request));
    }

    // Technique: Negative testing - Requirement R3 (first-semester students must have GPA 0.00).
    @Test
    @DisplayName("NEG-8: semester 1 with GPA 3.50 -> invalid (R3)")
    void firstSemesterWithGpaIsInvalid() {
        RegistrationRequest request = new RegistrationRequest(3.50, 1, 10, "SWE411", 4, true, false, Set.of());
        assertInvalid(validator.validate(request));
    }

    // Technique: Negative testing - special values (R1, R4b).
    // Both are EXPECTED TO FAIL: the validator does not catch these values.

    @Test
    @DisplayName("NEG-13: GPA Double.NaN -> invalid (R1)")
    void gpaNaNIsInvalid() {
        RegistrationRequest request = new RegistrationRequest(Double.NaN, 3, 10, "SWE411", 4, true, false, Set.of());
        assertInvalid(validator.validate(request));
    }

    @Test
    @DisplayName("NEG-14: registered credits Integer.MAX_VALUE -> invalid (R4b)")
    void hugeRegisteredCreditsIsInvalid() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, Integer.MAX_VALUE, "SWE411", 4, true, false, Set.of());
        assertInvalid(validator.validate(request));
    }
}

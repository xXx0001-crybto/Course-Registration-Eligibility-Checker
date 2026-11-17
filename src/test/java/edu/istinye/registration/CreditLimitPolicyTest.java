package edu.istinye.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for CreditLimitPolicy.getLimit(gpa, semester).
 *
 * Each @CsvSource line is one test case: "testId, input(s), expected result".
 * The name = "..." pattern makes JUnit show the test ID first in the report;
 * {0} is the first column, {1} the second, and so on.
 */
class CreditLimitPolicyTest {

    private final CreditLimitPolicy policy = new CreditLimitPolicy();

    // Technique: Equivalence Partitioning (EP) - Requirement R2 (limit depends on GPA band).
    // One representative GPA from each valid band, semester 3.
    @ParameterizedTest(name = "{0}: GPA {1} -> limit {2}")
    @DisplayName("EP-G2..EP-G4: one GPA per band gives the right limit (R2)")
    @CsvSource({
        "EP-G2, 1.50, 24",
        "EP-G3, 2.50, 30",
        "EP-G4, 3.50, 36"
    })
    void equivalenceClassesForGpa(String testId, double gpa, int expectedLimit) {
        assertEquals(expectedLimit, policy.getLimit(gpa, 3));
    }

    // Technique: Boundary Value Analysis (BVA) - Requirement R2.
    // Values at and next to each band edge (0.00, 2.00, 3.00, 4.00), semester 3.
    @ParameterizedTest(name = "{0}: GPA {1} -> limit {2}")
    @DisplayName("BVA-G2..BVA-G11: GPA band boundaries give the right limit (R2)")
    @CsvSource({
        "BVA-G2,  0.00, 24",
        "BVA-G3,  0.01, 24",
        "BVA-G4,  1.99, 24",
        "BVA-G5,  2.00, 30",
        "BVA-G6,  2.01, 30",
        "BVA-G7,  2.99, 30",
        "BVA-G8,  3.00, 36",
        "BVA-G9,  3.01, 36",
        "BVA-G10, 3.99, 36",
        "BVA-G11, 4.00, 36"
    })
    void boundaryValuesForGpa(String testId, double gpa, int expectedLimit) {
        assertEquals(expectedLimit, policy.getLimit(gpa, 3));
    }

    // Requirement R3: first-semester students always get limit 30.
    // R3-2 checks that semester 2 is NOT treated as a first semester.
    @ParameterizedTest(name = "{0}: GPA {1}, semester {2} -> limit {3}")
    @DisplayName("R3-1, R3-2: first-semester limit (R3)")
    @CsvSource({
        "R3-1, 0.00, 1, 30",
        "R3-2, 1.50, 2, 24"
    })
    void firstSemesterLimit(String testId, double gpa, int semester, int expectedLimit) {
        assertEquals(expectedLimit, policy.getLimit(gpa, semester));
    }
}

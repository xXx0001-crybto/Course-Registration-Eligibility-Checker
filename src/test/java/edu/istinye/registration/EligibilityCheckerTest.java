package edu.istinye.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for EligibilityChecker.check(request).
 *
 * Every test asserts the Decision (APPROVED / DENIED / INVALID_INPUT).
 * The assertion message prints the checker's reason, so a failing test shows why.
 * JUnit converts the text "APPROVED" in @CsvSource into Decision.APPROVED automatically.
 *
 * Default valid request (only the fields under test are changed):
 *   new RegistrationRequest(gpa 2.50, semester 3, registeredCredits 10, courseCode "SWE411",
 *                           courseCredits 4, prerequisitePassed true, advisorApproval false,
 *                           registeredCourses empty set)
 */
class EligibilityCheckerTest {

    private final EligibilityChecker checker = new EligibilityChecker();

    /** Runs the checker and asserts the decision, showing the reason if it fails. */
    private void assertDecision(Decision expected, RegistrationRequest request) {
        RegistrationResult result = checker.check(request);
        assertEquals(expected, result.decision(), "Reason given by checker: " + result.reason());
    }

    // ---------------------------------------------------------------------------------
    // Technique: Decision table - Requirement R6 (advisor overload when OVER the normal limit).
    // Conditions: advisor approval? / GPA >= 2.00? / total <= limit + 6?
    // GPA 2.50 -> limit 30 (max 36 with overload); GPA 1.50 -> limit 24.
    @ParameterizedTest(name = "{0}: GPA {1}, registered {2} + course {3}, approval {4} -> {5}")
    @DisplayName("DT-a..DT-d: overload decision table (R6)")
    @CsvSource({
        "DT-a, 2.50, 30, 6, true,  APPROVED",   // approval Y, GPA>=2 Y, total 36 <= 36 Y
        "DT-b, 2.50, 30, 8, true,  DENIED",     // approval Y, GPA>=2 Y, total 38 <= 36 N
        "DT-c, 1.50, 24, 4, true,  DENIED",     // approval Y, GPA>=2 N (total 28 > 24)
        "DT-d, 2.50, 30, 4, false, DENIED"      // approval N (total 34 > 30)
    })
    void overloadDecisionTable(String testId, double gpa, int registeredCredits, int courseCredits,
                               boolean advisorApproval, Decision expected) {
        RegistrationRequest request = new RegistrationRequest(
                gpa, 3, registeredCredits, "SWE411", courseCredits, true, advisorApproval, Set.of());
        assertDecision(expected, request);
    }

    // ---------------------------------------------------------------------------------
    // Technique: Boundary Value Analysis - Requirement R8 (exactly at the limit is allowed).
    // GPA 2.50 -> normal limit 30, maximum with advisor overload 36. Course credits stay 4.
    @ParameterizedTest(name = "{0}: registered {1} + course {2}, approval {3} -> {4}")
    @DisplayName("BVA-L1..BVA-L5: credit limit boundaries (R8, R6)")
    @CsvSource({
        "BVA-L1, 25, 4, false, APPROVED",   // total 29 (limit - 1)
        "BVA-L2, 26, 4, false, APPROVED",   // total 30 (exactly the limit)
        "BVA-L3, 27, 4, false, DENIED",     // total 31 (limit + 1, no approval)
        "BVA-L4, 32, 4, true,  APPROVED",   // total 36 (exactly the overload maximum)
        "BVA-L5, 33, 4, true,  DENIED"      // total 37 (overload maximum + 1)
    })
    void creditLimitBoundaries(String testId, int registeredCredits, int courseCredits,
                               boolean advisorApproval, Decision expected) {
        RegistrationRequest request = new RegistrationRequest(
                2.50, 3, registeredCredits, "SWE411", courseCredits, true, advisorApproval, Set.of());
        assertDecision(expected, request);
    }

    // ---------------------------------------------------------------------------------
    // Requirement R3 + R6: a first-semester student (GPA 0.00) cannot get an overload,
    // even with advisor approval. Total 28 + 4 = 32 > 30.
    @Test
    @DisplayName("R3-3: semester 1, GPA 0.00, registered 28 + course 4, approval Y -> DENIED")
    void firstSemesterCannotOverload() {
        RegistrationRequest request = new RegistrationRequest(0.00, 1, 28, "SWE411", 4, true, true, Set.of());
        assertDecision(Decision.DENIED, request);
    }

    // ---------------------------------------------------------------------------------
    // Technique: Negative testing - Requirement R7 (no duplicate registration)
    // and Requirement R5 (prerequisite must be passed).

    @Test
    @DisplayName("NEG-9: course SWE411 already registered -> DENIED (R7)")
    void duplicateCourseIsDenied() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, "SWE411", 4, true, false, Set.of("SWE411"));
        assertDecision(Decision.DENIED, request);
    }

    @Test
    @DisplayName("NEG-10: prerequisite not passed -> DENIED (R5)")
    void prerequisiteNotPassedIsDenied() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, "SWE411", 4, false, false, Set.of());
        assertDecision(Decision.DENIED, request);
    }

    // Technique: Negative testing - Requirement R7 with differently written course codes.
    // The same course written in lower case or with a leading space must still count as a duplicate.
    // Both are EXPECTED TO FAIL: the checker compares the codes exactly.
    // (The single quotes in @CsvSource keep the leading space in ' SWE411'.)
    @ParameterizedTest(name = "{0}: course code \"{1}\", registered [SWE411] -> DENIED")
    @DisplayName("NEG-11, NEG-12: duplicate course written differently -> DENIED (R7)")
    @CsvSource({
        "NEG-11, swe411",
        "NEG-12, ' SWE411'"
    })
    void duplicateCourseWithDifferentSpellingIsDenied(String testId, String courseCode) {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, courseCode, 4, true, false, Set.of("SWE411"));
        assertDecision(Decision.DENIED, request);
    }

    // Technique: Negative testing - special values that should be caught as invalid input.
    // Both are EXPECTED TO FAIL.

    @Test
    @DisplayName("NEG-13b: GPA Double.NaN, registered 32 + course 4 -> INVALID_INPUT (R1)")
    void gpaNaNGivesInvalidInput() {
        RegistrationRequest request = new RegistrationRequest(Double.NaN, 3, 32, "SWE411", 4, true, false, Set.of());
        assertDecision(Decision.INVALID_INPUT, request);
    }

    @Test
    @DisplayName("NEG-14b: registered credits Integer.MAX_VALUE -> INVALID_INPUT (R4b)")
    void hugeRegisteredCreditsGivesInvalidInput() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, Integer.MAX_VALUE, "SWE411", 4, true, false, Set.of());
        assertDecision(Decision.INVALID_INPUT, request);
    }

    // ---------------------------------------------------------------------------------
    // Rule order: when a request breaks BOTH R7 (duplicate) and R5 (prerequisite),
    // the duplicate check comes first, so the reason must mention "already registered".
    @Test
    @DisplayName("ORD-1: duplicate course AND prerequisite not passed -> DENIED, reason \"already registered\"")
    void duplicateIsCheckedBeforePrerequisite() {
        RegistrationRequest request = new RegistrationRequest(2.50, 3, 10, "SWE411", 4, false, false, Set.of("SWE411"));
        RegistrationResult result = checker.check(request);

        assertEquals(Decision.DENIED, result.decision());
        assertTrue(result.reason().contains("already registered"),
                "Reason was: " + result.reason());
    }
}

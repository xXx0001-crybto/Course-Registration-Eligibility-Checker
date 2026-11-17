package edu.istinye.registration;

/**
 * Decides how many credits a student may take in one semester.
 * Implements requirements R2 (limit by GPA), R3 (first semester) and the
 * numbers used by R6 (advisor overload).
 */
public class CreditLimitPolicy {

    // Named constants instead of "magic numbers" scattered in the code.
    // If a rule changes (e.g. for regression testing), only one line changes.
    public static final int LOW_GPA_LIMIT = 24;          // GPA below 2.00
    public static final int MID_GPA_LIMIT = 30;          // GPA 2.00 - 2.99
    public static final int HIGH_GPA_LIMIT = 36;         // GPA 3.00 and above
    public static final int FIRST_SEMESTER_LIMIT = 30;   // R3

    public static final double MID_GPA_THRESHOLD = 2.00;
    public static final double HIGH_GPA_THRESHOLD = 3.00;

    public static final int OVERLOAD_ALLOWANCE = 6;           // R6: extra credits with approval
    public static final double MIN_GPA_FOR_OVERLOAD = 2.00;   // R6

    /**
     * Returns the normal credit limit for a student (without advisor overload).
     * Assumes the inputs were already validated by InputValidator.
     *
     * @param gpa      the student's GPA
     * @param semester the student's semester
     * @return the maximum number of credits allowed
     */
    public int getLimit(double gpa, int semester) {
        // R3: first-semester students always get a fixed limit.
        if (semester == 1) {
            return FIRST_SEMESTER_LIMIT;
        }
        // R2: limit depends on the GPA band.
        if (gpa < MID_GPA_THRESHOLD) {
            return LOW_GPA_LIMIT;
        }
        if (gpa < HIGH_GPA_THRESHOLD) {
            return MID_GPA_LIMIT;
        }
        return HIGH_GPA_LIMIT;
    }

    /**
     * R6: an advisor can approve extra credits only for students with GPA >= 2.00.
     * First-semester students have GPA 0.00 (R3), so they never qualify.
     *
     * @param gpa the student's GPA
     * @return true if the student is allowed an overload
     */
    public boolean isOverloadEligible(double gpa) {
        return gpa >= MIN_GPA_FOR_OVERLOAD;
    }
}

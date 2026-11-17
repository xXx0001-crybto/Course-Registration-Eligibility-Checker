package edu.istinye.registration;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Console entry point. Reads one registration request from the keyboard,
 * runs the EligibilityChecker and prints the result.
 *
 * All decision logic lives in EligibilityChecker; this class only handles
 * input and output, so the logic can be unit-tested without a console.
 */
public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("=== Course Registration Eligibility Checker ===");

        try {
            double gpa = readDouble(in, "GPA (0.00 - 4.00): ");
            int semester = readInt(in, "Semester (1 - 8): ");
            int registeredCredits = readInt(in, "Credits already registered: ");
            String courseCode = readLine(in, "Course code (e.g. SWE411): ");
            int courseCredits = readInt(in, "Course credits (1 - 10): ");
            boolean prerequisitePassed = readYesNo(in, "Prerequisite passed? (y/n): ");
            boolean advisorApproval = readYesNo(in, "Advisor approval? (y/n): ");
            Set<String> registeredCourses = readCourseList(in,
                    "Already registered course codes, comma-separated (empty if none): ");

            RegistrationRequest request = new RegistrationRequest(gpa, semester,
                    registeredCredits, courseCode, courseCredits,
                    prerequisitePassed, advisorApproval, registeredCourses);

            RegistrationResult result = new EligibilityChecker().check(request);
            System.out.println("Result: " + result.decision());
            System.out.println("Reason: " + result.reason());
        } catch (NumberFormatException e) {
            // e.g. the user typed "abc" where a number was expected
            System.out.println("Result: " + Decision.INVALID_INPUT);
            System.out.println("Reason: a number was expected");
        }
    }

    private static String readLine(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private static double readDouble(Scanner in, String prompt) {
        return Double.parseDouble(readLine(in, prompt));
    }

    private static int readInt(Scanner in, String prompt) {
        return Integer.parseInt(readLine(in, prompt));
    }

    private static boolean readYesNo(Scanner in, String prompt) {
        return readLine(in, prompt).equalsIgnoreCase("y");
    }

    private static Set<String> readCourseList(Scanner in, String prompt) {
        Set<String> courses = new HashSet<>();
        for (String code : readLine(in, prompt).split(",")) {
            if (!code.isBlank()) {
                courses.add(code.trim());
            }
        }
        return courses;
    }
}

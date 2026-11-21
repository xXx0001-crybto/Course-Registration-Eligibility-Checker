# Course Registration Eligibility Checker

A small Java console application that decides whether a student may register for a course, tested with JUnit 5.

Developed for **SWE411 Software Verification and Validation** at İstinye University (Assignment 1: Software Development and Automated Testing with JUnit).

## What it does

The checker takes one registration request and returns one of three results:

| Result | Meaning |
|---|---|
| `APPROVED` | The student may register for the course |
| `DENIED` | A business rule blocks the registration (with a reason) |
| `INVALID_INPUT` | The request contains missing or out-of-range values |

There is no GUI and no database. The focus is on the business rules and on testing them.

## Business rules

| ID | Rule |
|---|---|
| R1 | GPA must be 0.00–4.00 |
| R2 | Credit limit: GPA < 2.00 → 24; 2.00–2.99 → 30; ≥ 3.00 → 36 |
| R3 | First semester: limit 30, and GPA must be 0.00 |
| R4 | Course credits 1–10, semester 1–8 |
| R4b | Registered credits must be 0–42 |
| R5 | Prerequisite not passed → DENIED |
| R6 | Over the limit: advisor approval and GPA ≥ 2.00 allow up to limit + 6 |
| R7 | Course already registered → DENIED (ignores upper/lower case and spaces) |
| R8 | Total exactly at the limit → APPROVED |

Rules are checked in this order: invalid input → duplicate course → prerequisite → credit limit.

## Project structure

```
src/main/java/edu/istinye/registration/
├── RegistrationRequest.java   # the 8 inputs of one request
├── Decision.java              # APPROVED / DENIED / INVALID_INPUT
├── RegistrationResult.java    # decision + reason
├── InputValidator.java        # R1, R3, R4, R4b
├── CreditLimitPolicy.java     # R2, R3, R6
├── EligibilityChecker.java    # applies all rules in order (R5–R8)
└── Main.java                  # console input and output

src/test/java/edu/istinye/registration/
├── CreditLimitPolicyTest.java
├── InputValidatorTest.java
└── EligibilityCheckerTest.java
```

## Requirements

- Java 17 or newer
- Maven 3.9+

## How to run

Run all tests and generate the coverage report:

```
mvn clean test
```

Open the coverage report: `target/site/jacoco/index.html`

Run the console application:

```
mvn compile
java -cp target/classes edu.istinye.registration.Main
```

## Testing

60 automated JUnit 5 tests, designed from the requirements using:

- **Equivalence partitioning:** valid and invalid groups for GPA, credits and semester
- **Boundary value analysis:** values below, at and above each boundary (e.g. GPA 1.99 / 2.00 / 2.01)
- **Decision table testing:** advisor overload rule (R6), 4 test cases
- **Negative testing:** missing values, out-of-range values, contradictory input, NaN, integer overflow
- **Regression testing:** all tests re-run after every fix

Each test has an ID (e.g. `BVA-G5`, `NEG-13`) shown with `@DisplayName`, matching the test plan in the report.

### Results

| Run | Tests | Passed | Failed |
|---|---|---|---|
| First run | 59 | 52 | 7 (defects D1–D4) |
| After fixes (regression) | 59 | 59 | 0 |
| After adding COV-1 | 60 | 60 | 0 |

### Defects found by the tests

| Defect | Problem | Fix |
|---|---|---|
| D1 | `swe411` not recognised as a duplicate of `SWE411` | Compare codes with `toUpperCase(Locale.ROOT)` |
| D2 | `" SWE411"` (with a space) not recognised as a duplicate | `trim()` before comparing |
| D3 | GPA `NaN` was accepted and approved | Explicit `Double.isNaN()` check |
| D4 | Huge credit values overflowed `int` and were approved | New rule R4b: registered credits 0–42 |

### Coverage (JaCoCo)

All classes containing business logic reach 100% instruction and branch coverage. `Main` (console input and output only) is not unit-tested.

## Use of AI

AI (Claude) was used to help learn Java, to generate most of the Java and JUnit code from the requirements and test design, and to organise the report. All tests were run and their results checked by the author.

## Author

Riza Arslan, İstinye University, Software Engineering

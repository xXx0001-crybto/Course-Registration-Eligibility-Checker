package edu.istinye.registration;

/**
 * The output of a registration check: the decision plus a human-readable reason.
 *
 * @param decision APPROVED, DENIED or INVALID_INPUT
 * @param reason   why this decision was made (shown to the user)
 */
public record RegistrationResult(Decision decision, String reason) {
}

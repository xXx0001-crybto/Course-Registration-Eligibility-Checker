package edu.istinye.registration;

/**
 * The three possible outcomes of a registration check.
 * An enum allows only these fixed values, so a typo like "APROVED" cannot compile.
 */
public enum Decision {
    APPROVED,
    DENIED,
    INVALID_INPUT
}

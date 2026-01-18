package com.ridei.identity.shared;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Abstract base class that enables the <b>Self-Validation Pattern</b> for Domain Objects.
 * <p>
 * This class belongs to the <b>Shared Kernel</b>. It provides a mechanism for objects
 * (specifically Commands and Value Objects) to enforce their invariants immediately upon instantiation.
 * By extending this class, we ensure a <b>Fail-Fast</b> behavior: it is impossible to create
 * an instance of a subclass that violates its defined constraints (e.g., @NotNull, @Size).
 * </p>
 *
 * @param <T> The type of the subclass extending this utility.
 */
public abstract class SelfValidating<T> {

    /**
     * Singleton Validator instance shared across all subclasses to optimize performance.
     * <p>
     * <b>Performance Note:</b> {@link ValidatorFactory} creation is expensive. It is initialized
     * statically to avoid overhead during high-throughput object creation (e.g., incoming requests).
     * </p>
     */
    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Default constructor.
     */
    public SelfValidating() {
        // No operational logic needed here as the validator is static.
    }

    /**
     * Triggers the validation of the current instance's properties.
     * <p>
     * This method should be called as the last step in the subclass constructor.
     * It scans all fields annotated with Jakarta Validation constraints (e.g., {@code @NotBlank}).
     * </p>
     *
     * @throws ConstraintViolationException if any validation constraint is violated.
     * This runtime exception wraps a set of violations detailing exactly what failed.
     */
    protected void validateSelf() {
        // Cast 'this' to T. Safe because this class is always extended by T.
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<T>> violations = validator.validate((T) this);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

}
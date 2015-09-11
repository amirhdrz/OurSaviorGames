package com.oursaviorgames.backend.model.types;

/**
 * Abstract class for representing a validated type.
 * Subclasses provide the validation logic by overriding {@link Validated#validate(Object)}
 * function call.
 * @param <T>
 */
abstract class Validated<T> {

    private T value;

    public Validated(T value) throws ValidationException {
        if (value == null) {
            throw new ValidationException("value to be validated cannot be null");
        }
        if (validate(value)) {
            this.value = value;
        } else {
            throw new ValidationException("Could not validate value (" + value.toString() + ")");
        }
    }

    /**
     * Guaranteed non-null validated value.
     * @return Returns the validated value.
     */
    public T getValue() {
        return value;
    }

    /**
     * Contains logic for validating. {@code value}.
     * @param value Non-null value to be validated.
     * @return The validated value.
     */
    abstract protected boolean validate(T value);

}
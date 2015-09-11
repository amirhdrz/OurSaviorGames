package com.oursaviorgames.backend.model.types;

/**
 * Represents a validated username.
 */
public final class ValidatedUsername extends Validated<String> {

    public static final int UsernameMinLength = 5;
    public static final int UsernameMaxLength = 30;

    public ValidatedUsername(String value) throws ValidationException, NullPointerException{
        super(value);
    }

    @Override
    protected boolean validate(String name) {
        if (name.length() < UsernameMinLength || name.length() > UsernameMaxLength) {
            return false;
        }
        if (!name.matches("(^\\..*)|(.*\\.$)")) {
            if (name.matches("^_?([a-zA-Z0-9][_\\.]?)+")) {
                return true;
            }
        }
        return false;
    }

}

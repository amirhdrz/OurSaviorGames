package com.oursaviorgames.backend.model.types;

/**
 * Represents a validated email address.
 */
public final class ValidatedEmail extends Validated<String> {

    public ValidatedEmail(String value) throws ValidationException, NullPointerException {
        super(value);
    }

    @Override
    protected boolean validate(String value){
        //TODO: implement the validation logic.
        return true;
    }
}

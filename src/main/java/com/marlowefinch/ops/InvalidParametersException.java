package com.marlowefinch.ops;

import java.util.List;

/** One or more request parameters were invalid; carries every message for the 400 body. */
public class InvalidParametersException extends RuntimeException {

    private final List<String> errors;

    public InvalidParametersException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}

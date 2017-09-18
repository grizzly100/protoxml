package org.grizzlytech.protoxml.util;


/**
 * To be thrown when fatal errors occur. This will allow the entry method to exit cleanly.
 */
public class FatalException extends RuntimeException {
    public FatalException() {
        super();
    }

    public FatalException(String message) {
        super(message);
    }

    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }
}

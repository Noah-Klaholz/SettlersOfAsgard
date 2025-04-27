package ch.unibas.dmi.dbis.cs108.client.ui.events;

import ch.unibas.dmi.dbis.cs108.client.networking.events.ErrorEvent.ErrorSeverity;

import java.time.Instant;
import java.util.logging.Logger;

/**
 * UIEvent representing an error in the application.
 * Provides details about the error.
 */
public class ErrorEvent implements UIEvent {
    private final Instant timestamp = Instant.now();
    private final String errorCode;
    private final String errorMessage;
    private final ErrorSeverity severity;

    /**
     * Constructs an ErrorEvent.
     *
     * @param errorCode    code representing the error
     * @param errorMessage message describing the error
     * @param severity     severity of the error
     */
    public ErrorEvent(String errorCode, String errorMessage, ErrorSeverity severity) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.severity = severity;
    }

    /**
     * Returns the timestamp of the event.
     *
     * @return timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the code representing the error.
     *
     * @return error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the message describing the error.
     *
     * @return error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the severity of the error.
     *
     * @return error severity
     */
    public ErrorSeverity getSeverity() {
        return severity;
    }

    @Override
    public String getType() {
        return "ERROR";
    }
}

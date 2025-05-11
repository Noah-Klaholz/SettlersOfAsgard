package ch.unibas.dmi.dbis.cs108.client.ui.events;

import ch.unibas.dmi.dbis.cs108.client.networking.events.ErrorEvent.ErrorSeverity;

import java.time.Instant;

/**
 * UIEvent representing an error in the application.
 * Provides details about the error.
 */
public class ErrorEvent implements UIEvent {
    /**
     * Enum representing the severity of the error.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The severity of the error.
     */
    private final String errorCode;
    /**
     * The error code.
     */
    private final String errorMessage;
    /**
     * The error message.
     */
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

    /**
     * Returns the event type identifier.
     *
     * @return event type
     */
    @Override
    public String getType() {
        return "ERROR";
    }
}

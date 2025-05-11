package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

/**
 * UIEvent representing an error in the application.
 * This event is triggered when an error occurs, providing details about the error.
 */
public class ErrorEvent implements Event {
    /**
     * The timestamp of the event.
     */
    private final Instant timestamp = Instant.now();
    /**
     * The code representing the error.
     */
    private final String errorCode;
    /**
     * A message describing the error.
     */
    private final String errorMessage;
    /**
     * The severity of the error.
     */
    private final ErrorSeverity severity;

    /**
     * Constructor for ErrorEvent.
     *
     * @param errorCode    The code representing the error.
     * @param errorMessage A message describing the error.
     * @param severity     The severity of the error.
     */
    public ErrorEvent(String errorCode, String errorMessage, ErrorSeverity severity) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.severity = severity;
    }

    /**
     * Get the timestamp of the event.
     *
     * @return The timestamp.
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the code representing the error.
     *
     * @return The error code.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Get the message describing the error.
     *
     * @return The error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the severity of the error.
     *
     * @return The error severity.
     */
    public ErrorSeverity getSeverity() {
        return severity;
    }

    /**
     * Enum representing the severity of the error.
     */
    public enum ErrorSeverity {
        /**
         * Informational message.
         */
        INFO,
        /**
         * Warning message.
         */
        WARNING,
        /**
         * Error message.
         */
        ERROR,
        /**
         * Fatal error message.
         */
        FATAL
    }
}
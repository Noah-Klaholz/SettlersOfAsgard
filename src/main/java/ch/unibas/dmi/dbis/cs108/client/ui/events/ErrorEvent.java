package ch.unibas.dmi.dbis.cs108.client.ui.events;

import java.time.Instant;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ErrorEvent.ErrorSeverity;

/**
 * UIEvent representing an error in the application.
 * This event is triggered when an error occurs, providing details about the error.
 */
public class ErrorEvent implements UIEvent {
    private final Instant timestamp = Instant.now();
    private final String errorCode;
    private final String errorMessage;
    private final ErrorSeverity severity;

    /**
     * Constructor for ErrorEvent.
     *.
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

    @Override
    public String getType() {
        return "ERROR";
    }
}

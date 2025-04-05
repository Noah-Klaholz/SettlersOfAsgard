package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public class ErrorEvent implements Event {
    private final Instant timestamp = Instant.now();
    private final String errorCode;
    private final String errorMessage;
    private final ErrorSeverity severity;

    public ErrorEvent(String errorCode, String errorMessage, ErrorSeverity severity) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.severity = severity;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public enum ErrorSeverity {
        INFO, WARNING, ERROR, FATAL
    }
}
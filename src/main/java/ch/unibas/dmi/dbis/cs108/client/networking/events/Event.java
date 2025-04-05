package ch.unibas.dmi.dbis.cs108.client.networking.events;

import java.time.Instant;

public interface Event {
    Instant getTimestamp();
}
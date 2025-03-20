package ch.unibas.dmi.dbis.cs108.client.core.commands;

import ch.unibas.dmi.dbis.cs108.client.networking.GameClient;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GameCommand interface is responsible for executing commands
 */
public interface GameCommand {
    /**
     * Executes the command
     * @return String
     */
    String execute();



}

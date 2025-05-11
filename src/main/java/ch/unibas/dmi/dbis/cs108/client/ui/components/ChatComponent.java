package ch.unibas.dmi.dbis.cs108.client.ui.components;

import ch.unibas.dmi.dbis.cs108.client.audio.AudioManager;
import ch.unibas.dmi.dbis.cs108.client.audio.AudioTracks;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.WhisperChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.game.CheatEvent;
import ch.unibas.dmi.dbis.cs108.shared.game.Player;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reusable UI component for chat functionality, supporting global, lobby, and
 * whisper chat.
 */
public class ChatComponent extends UIComponent<BorderPane> {
    /**
     * Logger for the ChatComponent class.
     */
    private static final Logger LOGGER = Logger.getLogger(ChatComponent.class.getName());
    /**
     * DateTimeFormatter for formatting chat timestamps.
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    /**
     * Pattern for matching whisper commands.
     */
    private static final Pattern WHISPER_PATTERN = Pattern.compile("^/w\\s+(\\S+)\\s+(.*)", Pattern.CASE_INSENSITIVE);
    /**
     * Pattern for matching cheat codes.
     */
    private static final Pattern CHEAT_PATTERN = CheatEvent.getCheatPattern();

    /**
     * Pattern for matching cheat codes.
     */
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    /**
     * The UI event bus for handling chat events.
     */
    private final UIEventBus eventBus;
    /**
     * The current lobby ID for sending/receiving lobby messages.
     */
    private String currentLobbyId;
    /**
     * The local player context for sending whispers and cheat codes.
     */
    private Player localPlayer; // Use shared.game.Player
    /**
     * Flag to indicate if the player is currently in-game.
     */
    private boolean inGame = false; // Flag to check if the player is in-game

    /**
     * FXML UI elements.
     */

    /**
     * The main view of the chat component.
     */
    @FXML
    private ListView<String> chatMessages;
    /**
     * The input field for sending chat messages.
     */
    @FXML
    private TextField chatInput;
    /**
     * The button for sending chat messages.
     */
    @FXML
    private ToggleButton globalChatButton;
    /**
     * The button for sending lobby chat messages.
     */
    @FXML
    private ToggleButton lobbyChatButton;
    /**
     * The toggle group for chat mode selection.
     */
    @FXML
    private ToggleGroup chatToggleGroup;
    /**
     * The button for sending chat messages.
     */
    @FXML
    private Button sendButton;

    /**
     * Constructs the ChatComponent.
     */
    public ChatComponent() {
        super("/fxml/components/ChatComponent.fxml");
        this.eventBus = UIEventBus.getInstance();
        try {
            LOGGER.info("Chat component FXML loaded, initializing...");
            initializeComponent();
            subscribeToEvents();
            LOGGER.info("Chat component successfully created");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing chat component", e);
            createFallbackView();
        }
    }

    /**
     * Creates a fallback view if FXML loading fails.
     */
    private void createFallbackView() {
        try {
            LOGGER.info("Creating fallback chat view");
            if (view == null) {
                view = new BorderPane();
                Label errorLabel = new Label("Chat component failed to load properly");
                errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 10;");
                view.setCenter(errorLabel);
                Button retryButton = new Button("Retry");
                retryButton.setOnAction(e -> initializeComponent());
                view.setBottom(new HBox(retryButton));
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to create fallback view", ex);
        }
    }

    /**
     * Initializes the chat component's UI elements and handlers.
     */
    private void initializeComponent() {
        if (view == null) {
            LOGGER.severe("View is null - FXML not loaded correctly");
            createFallbackView();
            return;
        }
        if (chatMessages == null) {
            LOGGER.severe("chatMessages ListView is null - FXML not loaded correctly");
            return;
        }

        view.setEffect(null);
        chatMessages.setItems(messages);
        VBox.setVgrow(chatMessages, Priority.ALWAYS); // Ensure ListView grows vertically
        BorderPane.setMargin(chatMessages, new javafx.geometry.Insets(0)); // Remove margin if needed
        chatMessages.setCellFactory(list -> new ListCell<>() {
            private final Label label = new Label();

            {
                label.setWrapText(true);
                label.setMaxWidth(Double.MAX_VALUE);
                label.setPadding(new javafx.geometry.Insets(2, 5, 2, 5));
                // Apply default text fill from variables.css
                label.setStyle("-fx-text-fill: -color-text-primary;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                // Clear previous styles from the cell itself
                getStyleClass().removeAll("system-message", "whisper-message");
                // Reset label style in case it was changed by specific message types
                label.setStyle("-fx-text-fill: -color-text-primary; -fx-font-style: normal;");

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    // Ensure empty cells also clear styles
                    getStyleClass().removeAll("system-message", "whisper-message");
                } else {
                    label.setText(item);
                    // Ensure label resizes with cell width
                    label.setMaxWidth(getListView().getWidth() - 20); // Adjust padding as needed

                    // Apply CSS classes to the ListCell based on content
                    if (item.contains("Whisper to") || item.contains("Whisper from")) {
                        getStyleClass().add("whisper-message");
                        // Specific label style for whispers can be handled in CSS using
                        // .whisper-message .label
                    } else if (item.contains("System:")) {
                        getStyleClass().add("system-message");
                        // Specific label style for system can be handled in CSS using .system-message
                        // .label
                    }
                    // Default style is handled by .chat-list .list-cell in CSS

                    setGraphic(label);
                }
            }
        });
        chatMessages.widthProperty().addListener((obs, oldVal, newVal) -> chatMessages.refresh());
        chatInput.setOnAction(evt -> handleSendMessage());
        sendButton.setOnAction(evt -> handleSendMessage());
        globalChatButton.setSelected(true);
        lobbyChatButton.setDisable(currentLobbyId == null || currentLobbyId.isEmpty());
        addSystemMessage("Chat component initialized. Use /w <username> <message> to whisper.");

        globalChatButton.addEventHandler(javafx.event.ActionEvent.ACTION, event -> {
            AudioManager.getInstance().playSoundEffect(AudioTracks.Track.BUTTON_CLICK.getFileName());
        });
        lobbyChatButton.addEventHandler(javafx.event.ActionEvent.ACTION, event -> {
            AudioManager.getInstance().playSoundEffect(AudioTracks.Track.BUTTON_CLICK.getFileName());
        });
    }

    /**
     * Subscribes to chat events on the UIEventBus.
     */
    private void subscribeToEvents() {
        try {
            eventBus.subscribe(GlobalChatEvent.class, this::handleGlobalChatMessage);
            eventBus.subscribe(LobbyChatEvent.class, this::handleLobbyChatMessage);
            eventBus.subscribe(WhisperChatEvent.class, this::handleWhisperMessage);
            LOGGER.info("Successfully subscribed to chat events");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to subscribe to chat events", e);
            addSystemMessage("Error: Failed to initialize chat event handling.");
        }
    }

    /**
     * Handles sending a chat message based on the current chat mode.
     */
    @FXML
    public void handleSendMessage() {
        String input = chatInput.getText().trim();
        if (input.isEmpty())
            return;
        if (localPlayer == null) {
            addSystemMessage("Error: Cannot send message. Player information missing.");
            LOGGER.severe("localPlayer is null in handleSendMessage");
            return;
        }
        Matcher whisperMatcher = WHISPER_PATTERN.matcher(input);
        Matcher cheatCodeMatcher = CHEAT_PATTERN.matcher(input);
        try {
            if (whisperMatcher.matches()) {
                String recipient = whisperMatcher.group(1);
                String msg = whisperMatcher.group(2);
                if (recipient != null && !recipient.trim().isEmpty() && msg != null && !msg.trim().isEmpty()) {
                    if (localPlayer.getName().equalsIgnoreCase(recipient.trim())) {
                        addSystemMessage("You cannot whisper to yourself.");
                    } else {
                        LOGGER.fine("Sending whisper to " + recipient.trim() + ": " + msg.trim());
                        eventBus.publish(new WhisperChatEvent(recipient.trim(), msg.trim()));
                    }
                } else {
                    if (recipient == null || recipient.trim().isEmpty()) {
                        addSystemMessage("Invalid whisper format. Missing recipient. Use /w <username> <message>");
                    } else {
                        addSystemMessage("Invalid whisper format. Missing message. Use /w <username> <message>");
                    }
                }
            } else if (cheatCodeMatcher.matches() && inGame) {
                String cheatCode = cheatCodeMatcher.group(1);
                LOGGER.fine("Sending cheat code: " + cheatCode);
                eventBus.publish(new CheatEvent(CheatEvent.Cheat.fromCode(cheatCode)));
            } else if (globalChatButton.isSelected()) {
                LOGGER.fine("Sending global message: " + input);
                eventBus.publish(new GlobalChatEvent(input, GlobalChatEvent.ChatType.GLOBAL));
            } else if (lobbyChatButton.isSelected()) {
                if (currentLobbyId != null && !currentLobbyId.isEmpty()) {
                    LOGGER.fine("Sending lobby message: " + input + " to lobby: " + currentLobbyId);
                    eventBus.publish(new LobbyChatEvent(currentLobbyId, input));
                } else {
                    addSystemMessage("Cannot send lobby message: Not in a lobby.");
                    globalChatButton.setSelected(true);
                }
            } else {
                LOGGER.warning("No chat type selected, defaulting to GLOBAL");
                eventBus.publish(new GlobalChatEvent(input, GlobalChatEvent.ChatType.GLOBAL));
            }
            AudioManager.getInstance().playSoundEffect(AudioTracks.Track.MESSAGE_SENT.getFileName());
            chatInput.clear();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending message", e);
            addSystemMessage("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Handles incoming global chat messages.
     */
    private void handleGlobalChatMessage(GlobalChatEvent event) {
        // Ignore locally generated events before server echo
        if (event.getSender() == null && event.getChatType() != GlobalChatEvent.ChatType.SYSTEM) {
            return;
        }
        Platform.runLater(() -> {
            String formatted;
            String sender = event.getSender();
            if (event.getChatType() == GlobalChatEvent.ChatType.SYSTEM) {
                formatted = String.format("[%s] System: %s",
                        TIME_FORMATTER.format(event.getTimestamp()),
                        event.getContent());
            } else {
                // Sender null check remains for safety, though ideally wouldn't happen for
                // non-system server messages
                if (sender == null) {
                    LOGGER.warning(
                            "Received GlobalChatEvent (non-system) with null sender. Content: " + event.getContent());
                    sender = "Unknown";
                }
                formatted = String.format("[%s] %s: %s",
                        TIME_FORMATTER.format(event.getTimestamp()),
                        sender,
                        event.getContent());
            }
            messages.add(formatted);
            scrollToBottom();
        });
    }

    /**
     * Handles incoming lobby chat messages.
     */
    private void handleLobbyChatMessage(LobbyChatEvent event) {
        // Ignore locally generated events before server echo
        if (event.getSender() == null) {
            return;
        }
        // Only display if the lobby tab is selected and it matches the current lobby
        Platform.runLater(() -> {
            String sender = event.getSender();
            // Sender null check remains for safety
            if (sender == null) {
                LOGGER.warning("Received LobbyChatEvent with null sender for lobby " + event.getLobbyId()
                        + ". Content: " + event.getMessage());
                sender = "Unknown";
            }
            String formatted = String.format("[%s] %s: %s",
                    TIME_FORMATTER.format(event.getTimestamp()),
                    sender,
                    event.getMessage());
            messages.add(formatted);
            scrollToBottom();
        });

        // Log ignored messages for debugging if needed
        // else if (lobbyChatButton.isSelected() && currentLobbyId != null &&
        // !currentLobbyId.equals(event.getLobbyId())) {
        // LOGGER.fine("Ignored lobby message from different lobby: " +
        // event.getLobbyId() + " (Current: " + currentLobbyId + ")");
        // } else if (!lobbyChatButton.isSelected()) {
        // LOGGER.fine("Ignored lobby message while global tab selected.");
        // }
    }

    /**
     * Handles incoming whisper messages from the UIEventBus.
     * Only display whispers that have a valid sender (i.e., received from server).
     */
    private void handleWhisperMessage(WhisperChatEvent event) {
        Platform.runLater(() -> {
            // Ignore locally generated events (sender == null)
            if (event.getSender() == null) {
                return;
            }
            // Check if it's an echo of a message sent by the local player
            boolean isEchoOfSent = localPlayer != null && event.getSender().equals(localPlayer.getName());
            // Check if the message is addressed to the local player
            boolean isAddressedToSelf = localPlayer != null && event.getRecipient().equals(localPlayer.getName());

            if (isEchoOfSent) {
                // Display outgoing whisper echo
                addWhisperMessage(event.getTimestamp(), event.getSender(), event.getRecipient(), event.getMessage(),
                        true);
            } else if (isAddressedToSelf) {
                // Display incoming whisper
                addWhisperMessage(event.getTimestamp(), event.getSender(), event.getRecipient(), event.getMessage(),
                        false);
            } else {
                // Log whispers not involving the local player (shouldn't happen with correct
                // server logic)
                LOGGER.fine("Ignored whisper message not involving local player: From=" + event.getSender() + ", To="
                        + event.getRecipient());
            }
        });
    }

    /**
     * Adds a standard (global or lobby) chat message directly to the display.
     * This method is now primarily intended for SYSTEM messages or potentially
     * debugging,
     * as regular messages rely on server echo.
     */
    private void addChatMessage(LocalDateTime timestamp, String sender, String message, boolean isGlobal) {
        Platform.runLater(() -> {
            String formatted = String.format("[%s] %s: %s",
                    TIME_FORMATTER.format(timestamp),
                    sender,
                    message);
            messages.add(formatted);
            scrollToBottom();
        });
    }

    /**
     * Adds a whisper message directly to the display with special formatting.
     */
    private void addWhisperMessage(LocalDateTime timestamp, String sender, String recipient, String message,
                                   boolean sentBySelf) {
        Platform.runLater(() -> {
            String formatted;
            if (sentBySelf) {
                String displayRecipient = recipient != null ? recipient : "Unknown";
                formatted = String.format("[%s] Whisper to %s: %s",
                        TIME_FORMATTER.format(timestamp),
                        displayRecipient,
                        message);
            } else {
                String displaySender = sender != null ? sender : "Unknown";
                formatted = String.format("[%s] Whisper from %s: %s",
                        TIME_FORMATTER.format(timestamp),
                        displaySender,
                        message);
            }
            messages.add(formatted);
            scrollToBottom();
        });
    }

    /**
     * Adds a system message directly to the chat display.
     */
    public void addSystemMessage(String message) {
        Platform.runLater(() -> {
            String formatted = String.format("[%s] System: %s", TIME_FORMATTER.format(LocalDateTime.now()), message);
            messages.add(formatted);
            scrollToBottom();
        });
    }

    /**
     * Sets the current lobby ID for sending/receiving lobby messages.
     */
    public void setCurrentLobbyId(String lobbyId) {
        this.currentLobbyId = lobbyId;
        Platform.runLater(() -> {
            boolean inLobby = lobbyId != null && !lobbyId.isEmpty();
            if (lobbyChatButton != null) {
                lobbyChatButton.setDisable(!inLobby);
                if (!inLobby && globalChatButton != null) {
                    globalChatButton.setSelected(true);
                }
            }
        });
    }

    /**
     * Sets the player context for the chat component.
     *
     * @param player The local player.
     */
    public void setPlayer(Player player) {
        if (player != null) {
            this.localPlayer = player;
            LOGGER.fine("ChatComponent player context updated to: " + player.getName());
        } else {
            LOGGER.warning("Attempted to set null player in ChatComponent");
        }
    }

    /**
     * Sets the in-game status of the player.
     *
     * @param inGame True if the player is in-game, false otherwise.
     */
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
        LOGGER.fine("ChatComponent in-game status updated to: " + inGame);
    }

    /**
     * Clears all messages from the chat display.
     */
    public void clearMessages() {
        Platform.runLater(messages::clear);
    }

    /**
     * Unsubscribes from all events to prevent memory leaks.
     */
    public void cleanup() {
        try {
            eventBus.unsubscribe(GlobalChatEvent.class, this::handleGlobalChatMessage);
            eventBus.unsubscribe(LobbyChatEvent.class, this::handleLobbyChatMessage);
            eventBus.unsubscribe(WhisperChatEvent.class, this::handleWhisperMessage);
            LOGGER.info("Chat component resources cleaned up");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during chat component cleanup", e);
        }
    }

    private void scrollToBottom() {
        if (!messages.isEmpty() && chatMessages != null) {
            chatMessages.scrollTo(messages.size() - 1);
        }
    }

    /**
     * Makes the chat component visible and managed within its parent container.
     * This is typically called when the parent view containing the chat becomes
     * active.
     */
    @Override
    public void show() {
        if (getView() != null) {
            getView().setVisible(true);
            getView().setManaged(true);
            LOGGER.fine("ChatComponent shown (visible and managed).");
        } else {
            LOGGER.warning("Attempted to show ChatComponent, but view is null.");
        }
    }
}

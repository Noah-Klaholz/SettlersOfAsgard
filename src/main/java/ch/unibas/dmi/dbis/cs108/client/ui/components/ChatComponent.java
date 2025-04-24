package ch.unibas.dmi.dbis.cs108.client.ui.components;

import ch.unibas.dmi.dbis.cs108.client.core.Player;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.GlobalChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.LobbyChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.chat.WhisperChatEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

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
    private static final Logger LOGGER = Logger.getLogger(ChatComponent.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern WHISPER_PATTERN = Pattern.compile("^/w\\s+(\\S+)\\s+(.*)", Pattern.CASE_INSENSITIVE);

    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final UIEventBus eventBus;
    private String currentLobbyId;
    private Player localPlayer;

    @FXML
    private ListView<String> chatMessages;
    @FXML
    private TextField chatInput;
    @FXML
    private ToggleButton globalChatButton;
    @FXML
    private ToggleButton lobbyChatButton;
    @FXML
    private ToggleGroup chatToggleGroup;
    @FXML
    private Button sendButton;

    /**
     * Constructs the ChatComponent.
     */
    public ChatComponent() {
        super("/fxml/components/ChatComponent.fxml");
        this.eventBus = UIEventBus.getInstance();
        this.localPlayer = new Player("Guest");
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
        String cssPath = getClass().getResource("/css/chat-component.css").toExternalForm();
        view.getStylesheets().add(cssPath);
        view.setEffect(null);
        chatMessages.setItems(messages);
        chatMessages.setCellFactory(list -> new ListCell<>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                label.setTextFill(Color.valueOf("#e8e8e8"));
                label.setMaxWidth(Double.MAX_VALUE);
                label.setPadding(new javafx.geometry.Insets(2, 5, 2, 5));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    label.setText(item);
                    label.setMaxWidth(getListView().getWidth() - 20);
                    if (item.contains("Whisper to") || item.contains("Whisper from")) {
                        label.setTextFill(Color.MAGENTA);
                        setStyle("-fx-background-color: #3a3a4a;");
                    } else if (item.contains("System:")) {
                        label.setTextFill(Color.LIGHTSKYBLUE);
                        setStyle("");
                    } else {
                        label.setTextFill(Color.valueOf("#e8e8e8"));
                        setStyle("");
                    }
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
        try {
            if (whisperMatcher.matches()) {
                String recipient = whisperMatcher.group(1);
                String msg = whisperMatcher.group(2);
                if (recipient != null && !recipient.isEmpty() && msg != null && !msg.isEmpty()) {
                    LOGGER.fine("Sending whisper to " + recipient + ": " + msg);
                    eventBus.publish(new WhisperChatEvent(recipient, msg));
                    addWhisperMessage(LocalDateTime.now(), localPlayer.getName(), recipient, msg, true);
                } else {
                    addSystemMessage("Invalid whisper format. Use /w <username> <message>");
                }
            } else if (globalChatButton.isSelected()) {
                LOGGER.fine("Sending global message: " + input);
                eventBus.publish(new GlobalChatEvent(input, GlobalChatEvent.ChatType.GLOBAL));
                addChatMessage(LocalDateTime.now(), localPlayer.getName(), input, true);
            } else if (lobbyChatButton.isSelected()) {
                if (currentLobbyId != null && !currentLobbyId.isEmpty()) {
                    LOGGER.fine("Sending lobby message: " + input + " to lobby: " + currentLobbyId);
                    eventBus.publish(new LobbyChatEvent(currentLobbyId, input));
                    addChatMessage(LocalDateTime.now(), localPlayer.getName(), input, false);
                } else {
                    addSystemMessage("Cannot send lobby message: Not in a lobby.");
                    globalChatButton.setSelected(true);
                }
            } else {
                LOGGER.warning("No chat type selected, defaulting to GLOBAL");
                eventBus.publish(new GlobalChatEvent(input, GlobalChatEvent.ChatType.GLOBAL));
                addChatMessage(LocalDateTime.now(), localPlayer.getName(), input, true);
            }
            chatInput.clear();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending message", e);
            addSystemMessage("Error sending message: " + e.getMessage());
        }
    }

    private void handleGlobalChatMessage(GlobalChatEvent event) {
        boolean isOwnMessage = localPlayer != null && event.getSender() != null
                && localPlayer.getName().equals(event.getSender());
        if (!isOwnMessage || event.getChatType() == GlobalChatEvent.ChatType.SYSTEM) {
            Platform.runLater(() -> {
                String formatted;
                if (event.getChatType() == GlobalChatEvent.ChatType.SYSTEM) {
                    formatted = String.format("[%s] System: %s",
                            TIME_FORMATTER.format(event.getTimestamp()),
                            event.getContent());
                } else {
                    formatted = String.format("[%s] %s: %s",
                            TIME_FORMATTER.format(event.getTimestamp()),
                            event.getSender(),
                            event.getContent());
                }
                messages.add(formatted);
                scrollToBottom();
            });
        }
    }

    private void handleLobbyChatMessage(LobbyChatEvent event) {
        boolean isOwnMessage = localPlayer != null && event.getSender() != null
                && localPlayer.getName().equals(event.getSender());
        boolean isIncomingNetworkMessage = event.getLobbyId() == null && event.getSender() != null;
        if (!isOwnMessage && lobbyChatButton.isSelected() && currentLobbyId != null) {
            if (isIncomingNetworkMessage) {
                Platform.runLater(() -> {
                    String formatted = String.format("[%s] %s: %s",
                            TIME_FORMATTER.format(event.getTimestamp()),
                            event.getSender(),
                            event.getMessage());
                    messages.add(formatted);
                    scrollToBottom();
                });
            }
        }
    }

    /**
     * Handles incoming whisper messages from the UIEventBus.
     *
     * @param event The WhisperChatEvent received.
     */
    private void handleWhisperMessage(WhisperChatEvent event) {
        boolean isOwnMessage = localPlayer != null && event.getSender() != null
                && localPlayer.getName().equals(event.getSender());
        if (!isOwnMessage && event.getSender() != null) {
            Platform.runLater(() -> {
                addWhisperMessage(event.getTimestamp(), event.getSender(), event.getRecipient(), event.getMessage(),
                        false);
            });
        }
    }

    /**
     * Adds a standard (global or lobby) chat message directly to the display.
     *
     * @param timestamp When the message was sent/received.
     * @param sender    Who sent the message.
     * @param message   The message content.
     * @param isGlobal  Whether it's a global chat message.
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
     *
     * @param timestamp  When the message was sent/received.
     * @param sender     Who sent the message.
     * @param recipient  Who received the message.
     * @param message    The message content.
     * @param sentBySelf True if this client sent the whisper, false if received.
     */
    private void addWhisperMessage(LocalDateTime timestamp, String sender, String recipient, String message,
            boolean sentBySelf) {
        Platform.runLater(() -> {
            String formatted;
            if (sentBySelf) {
                formatted = String.format("[%s] Whisper to %s: %s",
                        TIME_FORMATTER.format(timestamp),
                        recipient,
                        message);
            } else {
                formatted = String.format("[%s] Whisper from %s: %s",
                        TIME_FORMATTER.format(timestamp),
                        sender,
                        message);
            }
            messages.add(formatted);
            scrollToBottom();
        });
    }

    /**
     * Adds a system message directly to the chat display.
     *
     * @param message The system message content.
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
     *
     * @param lobbyId The ID of the current lobby, or null if not in a lobby.
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
     * Sets the local player object for display purposes.
     *
     * @param player The Player object representing the local user.
     */
    public void setPlayer(Player player) {
        if (player != null) {
            this.localPlayer = player;
            LOGGER.fine("ChatComponent player context updated to: " + player.getName());
        } else {
            LOGGER.warning("Attempted to set null player in ChatComponent");
            this.localPlayer = new Player("Guest");
        }
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
}

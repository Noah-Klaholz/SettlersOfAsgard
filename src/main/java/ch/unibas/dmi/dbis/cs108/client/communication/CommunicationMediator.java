package ch.unibas.dmi.dbis.cs108.client.communication;

import ch.unibas.dmi.dbis.cs108.client.networking.NetworkController;
import ch.unibas.dmi.dbis.cs108.client.networking.events.ChatMessageEvent;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher;
import ch.unibas.dmi.dbis.cs108.client.networking.events.EventDispatcher.EventListener;
import ch.unibas.dmi.dbis.cs108.client.ui.events.SendChatEvent;
import ch.unibas.dmi.dbis.cs108.client.ui.events.UIEventBus;

public class CommunicationMediator {
    private final NetworkController networkController;

    public CommunicationMediator(NetworkController networkController) {
        this.networkController = networkController;
        registerUIListeners();
        registerNetworkListeners();
    }

    // Subscribes to UI events and forwards them to the network layer.
    private void registerUIListeners() {
        UIEventBus.getInstance().subscribe(SendChatEvent.class, event -> {
            // Forward a global chat message from the UI to the networking controller.
            networkController.sendGlobalChat(event.getMessage());
        });
    }

    // Listens for network events and publishes corresponding UI events.
    private void registerNetworkListeners() {
        EventDispatcher.getInstance().registerListener(ChatMessageEvent.class, new EventListener<ChatMessageEvent>() {
            @Override
            public void onEvent(ChatMessageEvent networkEvent) {
                // Transform the networking chat event into a UI chat event.
                ch.unibas.dmi.dbis.cs108.client.ui.events.ChatMessageEvent uiEvent =
                        new ch.unibas.dmi.dbis.cs108.client.ui.events.ChatMessageEvent(
                                networkEvent.getSender() + ": " + networkEvent.getContent()
                        );
                UIEventBus.getInstance().publish(uiEvent);
            }

            @Override
            public Class<ChatMessageEvent> getEventType() {
                return ChatMessageEvent.class;
            }
        });
    }
}
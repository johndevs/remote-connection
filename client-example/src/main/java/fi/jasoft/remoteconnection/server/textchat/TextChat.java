package fi.jasoft.remoteconnection.server.textchat;

import com.vaadin.ui.AbstractComponent;

import fi.jasoft.remoteconnection.shared.textchat.TextChatState;

/**
 * Server side implementation of the component
 */
public class TextChat extends AbstractComponent {

	public TextChat(String id) {
		getState().peerConnectionId = id;
	}
	
	@Override
	protected TextChatState getState() {
		return (TextChatState) super.getState();
	}

	public void connect(String id) {
		getState().peerConnectionTarget = id;
	}
	
	public void disconnect() {
		getState().peerConnectionTarget = null;
	}
	
}
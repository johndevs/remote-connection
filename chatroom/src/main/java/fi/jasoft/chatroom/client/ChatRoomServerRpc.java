package fi.jasoft.chatroom.client;

import com.vaadin.shared.communication.ServerRpc;

public interface ChatRoomServerRpc extends ServerRpc {

	public void setPeerId(String peerId);
}

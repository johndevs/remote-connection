package fi.jasoft.remoteconnection.client;

import com.vaadin.shared.communication.ClientRpc;

public interface RemoteConnectionClientRPC extends ClientRpc {

	public void openChannel(String id);
	
	public void sendMessage(String id, String message);
	
	public void terminate();
	
	public void connect();
	
}

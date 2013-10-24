package fi.jasoft.remoteconnection.client;

import com.vaadin.shared.communication.ServerRpc;

public interface RemoteConnectionServerRPC extends ServerRpc {
	
	public void recievedConnection(String id);
	
	public void recievedMessage(String id, String message);
	
	public void channelConnected(String id);
		
	public void connected();
	
	public void error(String reason);

}

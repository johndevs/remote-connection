package fi.jasoft.remoteconnection;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

import fi.jasoft.remoteconnection.client.RemoteConnectionClientRPC;
import fi.jasoft.remoteconnection.client.RemoteConnectionServerRPC;
import fi.jasoft.remoteconnection.shared.ConnectionError;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;
import fi.jasoft.remoteconnection.shared.RemoteConnectionErrorHandler;
import fi.jasoft.remoteconnection.shared.RemoteConnectionState;

@JavaScript("peer.min.js")
public class ServerRemoteConnection extends AbstractExtension implements RemoteConnection {
	
	/*
	 * RPC for making communication from client -> server
	 */
	private RemoteConnectionServerRPC rpc = new RemoteConnectionServerRPC() {
		
		@Override
		public void recievedMessage(String id, String message) {
			getChannelById(id).messageRecieved(message);			
		}
		
		@Override
		public void recievedConnection(String id) {
			channels.add(new ServerRemoteChannel(id));			
		}

		@Override
		public void connected() {
			connected = true;			
		}

		@Override
		public void error(String reason) {
			if(errorHandler != null){
				errorHandler.onConnectionError(ConnectionError.get(reason));
			} else {
				System.err.println("An error occurred when connecting ("+reason+")");
			}
		}

		@Override
		public void channelConnected(String id) {
			getChannelById(id).setConnected(true);			
		}
	};
	
	// Currently open channels
	private final List<ServerRemoteChannel> channels = new LinkedList<ServerRemoteChannel>();
	
	// Currently attached data listeners
	private final List<RemoteConnectionDataListener> listeners = new LinkedList<RemoteConnectionDataListener>();
	
	private boolean connected = false;
	
	private RemoteConnectionErrorHandler errorHandler;
		
	/**
	 * Default construction
	 * 
	 * @param ui
	 * 		The UI the connection should be attached to
	 */
	private ServerRemoteConnection(UI ui){
		super.extend(ui);
		registerRpc(rpc);
	}
	
	/**
	 * Returns, or creates a new, channel by using its remote peer id
	 * 
	 * @param id
	 * 		The peer id of the channel endpoint
	 * @return
	 */
	private ServerRemoteChannel getChannelById(String id){
		for(ServerRemoteChannel channel : channels){
			if(channel.getId().equals(id)){
				return channel;
			}
		}
		return new ServerRemoteChannel(id);
	}
	
	/**
	 * Attaches a remote connection to an UI
	 * 
	 * @param ui
	 * 		The UI to attach to
	 * @return
	 * 		Returns the connection to communicate with remote channels with
	 */
	public static ServerRemoteConnection register(UI ui){
		return new ServerRemoteConnection(ui);
	}
	
	/**
	 * Attaches a remote connection to an UI
	 * 
	 * @param ui
	 * 		The UI to attach to
	 * @param peerId
	 * 		The id of this remote connection
	 * @return
	 *		Returns the connection to communicate with remote channels with
	 */
	public static ServerRemoteConnection register(UI ui, String peerId){
		ServerRemoteConnection peer = register(ui);
		peer.getState().id = peerId;
		return peer;
	}

	@Override
	public void setId(String peerId) {
		getState().id = peerId;		
	}
	
	/**
	 * Returns the id of this remote connection. 
	 */
	public String getId(){
		if(getState().id == null) {
			getState().id = UUID.randomUUID().toString();
		}
		return getState().id;
	}
	
	
	@Override
	public void beforeClientResponse(boolean initial) {
		if(getState().id == null) {
			getState().id = UUID.randomUUID().toString();
		}
	}
	
	/**
	 * Creates a channel between this and another remote connection
	 * 
	 * @param endpointPeerId
	 * 		The id of the other remote connection
	 */
	public RemoteChannel openChannel(String endpointPeerId) {
		RemoteChannel channel = getChannel(endpointPeerId);
		if(channel != null){
			return channel;
		}
		
		// Open the channel on the client side
		getRpcProxy(RemoteConnectionClientRPC.class).openChannel(endpointPeerId);
		
		// Create channel instance
		channel = new ServerRemoteChannel(endpointPeerId);
		channels.add((ServerRemoteChannel) channel);
		return channel;
	}		
	
	@Override
	protected RemoteConnectionState getState() {		
		return (RemoteConnectionState) super.getState();
	}
	
	/**
	 * Adds a new data listener for listing to messages
	 * 
	 * @param listener
	 * 		The listener to attach
	 */
	public void addDataListener(RemoteConnectionDataListener listener) {
		listeners.add(listener);
	}

	@Override
	public void broadcast(String message) {
		for(RemoteChannel channel : channels){
			channel.send(message);
		}		
	}

	@Override
	public RemoteChannel getChannel(String endpointPeerId) {
		for(ServerRemoteChannel channel : channels){
			if(channel.getId().equals(endpointPeerId)){
				return channel;
			}
		}
		return null;
	}

	@Override
	public void terminate() {
		getRpcProxy(RemoteConnectionClientRPC.class).terminate();		
	}

	@Override
	public void connect() {
		getRpcProxy(RemoteConnectionClientRPC.class).connect();		
	}

	@Override
	public boolean isConnected() {		
		return connected;
	}

	@Override
	public void setErrorHandler(RemoteConnectionErrorHandler handler) {
		errorHandler = handler;		
	}
	
	/**
	 * A channel between two remote connections
	 *
	 */
	public class ServerRemoteChannel implements RemoteChannel{

		private final String id;
		
		private boolean connected = false;
		
		public ServerRemoteChannel(String id){
			this.id = id;			
		}
		
		public void send(String message){
			getRpcProxy(RemoteConnectionClientRPC.class).sendMessage(id, message);
		}	

		public String getId() {
			return id;
		}
		
		private void messageRecieved(String message) {
			for(RemoteConnectionDataListener listener : listeners){
				listener.dataRecieved(this, message);
			}			
		}
		
		private void setConnected(boolean connected){
			this.connected = connected;
		}

		@Override
		public boolean isConnected() {			
			return connected;
		}
	}

}

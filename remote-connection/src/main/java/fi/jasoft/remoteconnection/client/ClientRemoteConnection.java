/*
* Copyright 2013 John Ahlroos
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fi.jasoft.remoteconnection.client;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;

import fi.jasoft.remoteconnection.client.peer.DataConnection;
import fi.jasoft.remoteconnection.client.peer.ObjectPeerListener;
import fi.jasoft.remoteconnection.client.peer.Peer;
import fi.jasoft.remoteconnection.client.peer.PeerError;
import fi.jasoft.remoteconnection.client.peer.PeerListener;
import fi.jasoft.remoteconnection.client.peer.StringPeerListener;
import fi.jasoft.remoteconnection.shared.ConnectedListener;
import fi.jasoft.remoteconnection.shared.IncomingChannelConnectionListener;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;
import fi.jasoft.remoteconnection.shared.RemoteConnectionErrorHandler;

/**
 * Client side implementation of {@link RemoteConnection}. 
 * Use {@link ClientRemoteConnection#register()} to get a instance.
 * 
 * @author John Ahlroos
 */
public class ClientRemoteConnection implements RemoteConnection {
	
	/**
	 * Client side implementation of RemoteConnection. Use {@link ClientRemoteConnection#register()} to get an instance.
	 * 
	 * @author John Ahlroos
	 */
	public class ClientRemoteChannel implements RemoteChannel {

		private final String id;
		
		private DataConnection connection;
				
		private List<String> messageQueue = new LinkedList<String>();

		private final List<RemoteConnectionDataListener> listeners = new LinkedList<RemoteConnectionDataListener>();
		
		private final List<ConnectedListener> connectedListeners = new LinkedList<ConnectedListener>();
		
		@Override
		public void send(String message) {		
			if(isConnected()){
				ClientRemoteConnection.getLogger().info("Sending message to "+id);
				connection.send(message);
			} else {
				ClientRemoteConnection.getLogger().warning("No connection to channel endpoint. Queueing message for later.");
				messageQueue.add(message);
			}			
		}

		@Override
		public String getId() {
			return id;
		}		
		
		@Override
		public boolean isConnected() {			
			return connection != null && connection.isOpen();
		}
		
		@Override
		public void addConnectedListener(ConnectedListener listener) {
			connectedListeners.add(listener);
		}
		
		/**
		 * Default constructor
		 * @param id
		 */
		private ClientRemoteChannel(String id){
			this.id = id;					
		}						

		/**
		 * Adds a data listener to the channel
		 * 
		 * @param listener
		 * 		The listener to add
		 */
		private void addDataListener(RemoteConnectionDataListener listener) {
			listeners.add(listener);
		}
					
		private void setConnection(final DataConnection con){
			if(con == null){
				throw new IllegalArgumentException("Connection cannot be null");
			}
					
			this.connection = con;
			
			ClientRemoteConnection.getLogger().info("Opening channel connection to "+connection.getPeerId());
			connection.addListener("open", new PeerListener() {

				@Override
				public void execute() {
					ClientRemoteConnection.getLogger().info("Connected to channel "+getId());			;
					flushMessageQueue();				
					for(ConnectedListener listener : connectedListeners) {
						listener.connected(getId());
					}
				}				
			});
			
			connection.addDataListener(new StringPeerListener() {
				
				@Override
				public void execute(String str) {						
					messageRecieved(str);
				}
			});		
		}
		
		private void flushMessageQueue(){
			while(!messageQueue.isEmpty()){
				this.send(messageQueue.remove(0));
			}			
		}	

		private void messageRecieved(String message) {		
			for(RemoteConnectionDataListener listener : listeners){			
				listener.dataRecieved(this, message);
			}		
		}
	}
	
	private String id;
		
	// Currently open connectedChannels
	private final List<ClientRemoteChannel> connectedChannels = new LinkedList<ClientRemoteChannel>();	
	    
    private final List<ClientRemoteChannel> pendingConnectionChannels = new LinkedList<ClientRemoteChannel>();
        	
	private final List<RemoteConnectionDataListener> listeners = new LinkedList<RemoteConnectionDataListener>();
    
	private final List<IncomingChannelConnectionListener> incomingListeners = new LinkedList<IncomingChannelConnectionListener>();
	
	private final List<ConnectedListener> connectedListeners = new LinkedList<ConnectedListener>();
	
    private Peer peer;
    
    private boolean connectedToSignallingServer = false;
    
    private RemoteConnectionErrorHandler errorHandler;
    
    private static String PEER_JS_URL = "http://cdn.peerjs.com/0.3/peer.min.js";
    
    private boolean scriptLoaded = false;
    
    private boolean scriptFailedToLoad = false;
    
    static Logger getLogger(){
    	return Logger.getLogger(ClientRemoteConnection.class.getName());
    }
    
    /**
     * Registers a new remote connection. Id is autogenerated by the signalling server.
     */
    public static RemoteConnection register(){
    	return new ClientRemoteConnection();
    }
    
    /**
     * Registers a new remote connection. Id is autogenerated by the signalling server.
     * 
     * @param injectScript
     * 		Should the Peer.js script be injected
     */
    public static RemoteConnection register(boolean injectScript){
    	return new ClientRemoteConnection(injectScript);
    }
    
    /**
     * Register a new Remote connection with a specific id
     * 
     * @param id
     * 		The unique id of the connection
     * @return
     */
    public static RemoteConnection register(String id) {    	    	    	
    	return register(id, true);
    }
    
    /**
     * Register a new remote connection
     * 
     * @param id
     * 		The unique id of the connection
     * @param injectScript
     * 		Should the Peer.js script be injected into the DOM
     * @return
     */
    public static RemoteConnection register(String id, boolean injectScript) {    	    	    	
    	return new ClientRemoteConnection(id, injectScript);
    }
    			
    /**
     * Default constructor
     * 
     * @param id
     * 		The unique peer id of this remote connection
     */
	private ClientRemoteConnection(String id, boolean injectScript){
		this.id = id;
		this.scriptLoaded = !injectScript;		
	}
	
	/**
	 * Default constructor. Peer.js is not loaded and id is autogenerated by signalling server
	 */
	private ClientRemoteConnection(){
		
	}
	
	/**
	 * Default constructor. Id is autogenerated by signalling server.
	 * 
	 * @param injectScript
	 * 		Should the Peer.js script be autogenerated.
	 */
	private ClientRemoteConnection(boolean injectScript){
		this.scriptLoaded = !injectScript;		
	}
	
	private final native boolean isPeerAvailable() 
	/*-{
		return typeof($wnd.Peer) === 'function'; 
	}-*/;
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public void setId(String id){	
		this.id = id;	
	}
	
	/**
	 * Connects to the remote signalling server.
	 */
	public void connect(){
		if(peer != null){
			throw new IllegalStateException("Already connected, call terminate() before connecting again");
		} 
		
		// Inject the script if needed
		if(!scriptLoaded && !scriptFailedToLoad && !isPeerAvailable()){						
			ScriptInjector
					.fromUrl(PEER_JS_URL)
					.setWindow(ScriptInjector.TOP_WINDOW)
					.setCallback(new Callback<Void, Exception>() {
				
				@Override
				public void onSuccess(Void result) {
					getLogger().info("Loaded peer.js successfully");
					scriptLoaded = true;
					if(isPeerAvailable()){
						connect();					
					} else {
						getLogger().severe("Peer is not available in DOM after loading script. Aborting.");
						scriptFailedToLoad = true;
					}
				}
				
				@Override
				public void onFailure(Exception reason) {
					scriptFailedToLoad = true;
					getLogger().severe("Failed to load Peer.js from "+PEER_JS_URL);
				}
			}).inject();	 
			return;
		}				
				
		assert scriptLoaded;		
		
		// Create peer
		peer = Peer.create(id);	
		
		// Register with signaling server
    	peer.addListener("open", new StringPeerListener() {
			
			@Override
			public void execute(String peerId) {
				onOpen(peerId);					
			}
		});
    	
    	// Triggered when another remote connection is established
    	peer.addListener("connection", new ObjectPeerListener() {
			
			@Override
			public void execute(JavaScriptObject obj) {			
				onConnection((DataConnection) obj);											
			}
		});
    	
    	// Triggered when an error occurs
    	peer.addListener("error", new ObjectPeerListener() {
			
			@Override
			public void execute(JavaScriptObject obj) {				
				onError((PeerError) obj);				
			}
		});
    	
    	// Triggered when the connection is closed
    	peer.addListener("close", new PeerListener() {
			
			@Override
			public void execute() {
				onClose();				
			}
		});
	}
	
	/**
	 * Triggered when a connection to the signalling server has been made
	 * 
	 * @param peerId
	 * 		The peer id recieved from the signalling server
	 */
	protected void onOpen(String peerId){
		id = peerId;
		connectedToSignallingServer = true;		
		getLogger().info("Connected to signalling server. Listening on id "+id);				
		flushChannelQueue();			
		for(ConnectedListener listener : connectedListeners) {
			listener.connected(peerId);
		}
	}
	
	/**
	 * Trigged when an external connection is being requested
	 * 
	 * @param connection
	 * 		The incoming data connection
	 */
	protected void onConnection(DataConnection connection){
		ClientRemoteChannel channel = getChannelById(connection.getPeerId());
		getLogger().info("Recieved incoming connection from "+connection.getPeerId());
		
		if(channel == null){		
			channel = new ClientRemoteChannel(connection.getPeerId());			
			connectToChannel(channel, connection);					
			for(IncomingChannelConnectionListener listener : incomingListeners){
				listener.connected(channel);
			}
		}		
	}
	
	/**
	 * Triggered when the peer is closed.
	 */
	protected void onClose() {
		getLogger().info("Connection closed");
	}
	
	/**
	 * Triggered when an error occurs with the peer
	 * 
	 * @param error
	 * 		The error that was triggered
	 */
	protected void onError(PeerError error) {				
		String msg = (error.getType() == null ? error.toString() : error.getType().toString());
		if(errorHandler != null){
			getLogger().severe("Remote connection got error: "+msg);		
			if(errorHandler.onConnectionError(error.getType())){
				terminate();		
			};
		} else {
			terminate();	
			getLogger().severe("Remote connection terminated with the error: "+msg);	
				
		}
	}	
	
	/**
	 * Disconnects from the signalling server but leaves all open channels open. 
	 * Call {@link #terminate()} to close all open channels as well.
	 */
	public void disconnect(){
		peer.disconnect();
		connectedToSignallingServer = false;
	}
	
	@Override
	public void terminate(){
		disconnect();
		peer.destroy();
		peer = null;
		pendingConnectionChannels.clear();
		connectedChannels.clear();
	}
	
	private void flushChannelQueue(){
		while(!pendingConnectionChannels.isEmpty()){
			connectToChannel(pendingConnectionChannels.remove(0));			
		}			
	}	
		
	private ClientRemoteChannel connectToChannel(ClientRemoteChannel channel) {	
		DataConnection connection = peer.connect(channel.getId());
		assert connection != null;
		return connectToChannel(channel, connection);		
	}
	
	private ClientRemoteChannel connectToChannel(ClientRemoteChannel channel, DataConnection connection) {
		channel.setConnection(connection);		

		for(RemoteConnectionDataListener listener : listeners){
			channel.addDataListener(listener);
		}	
				
		connectedChannels.add(channel);
		
		return channel;
	}
			
	@Override
	public RemoteChannel openChannel(String endpointPeerId) {
		if(endpointPeerId == null){
			throw new IllegalArgumentException("Cannot connect to null channel");
		}
		
		ClientRemoteChannel channel = getChannelById(endpointPeerId);
		if(channel != null){
			return channel;
		}
				
		channel = new ClientRemoteChannel(endpointPeerId);		
		if(connectedToSignallingServer){
			 channel = connectToChannel(channel);
		} else {
			pendingConnectionChannels.add(channel);
		}
		
		getLogger().info("Created channel to "+endpointPeerId);
		
		return channel;
	}	
	
		
	@Override
	public void addDataListener(RemoteConnectionDataListener listener) {
		listeners.add(listener);	
		for(ClientRemoteChannel channel : connectedChannels) {
			channel.addDataListener(listener);
		}
	}
	
	@Override
	public RemoteChannel getChannel(String channelEndpointId) {		
		RemoteChannel channel = getChannelById(channelEndpointId);
		if(channel == null){
			for(ClientRemoteChannel c : pendingConnectionChannels){
				if(c.getId().equals(channelEndpointId)){
					channel = c;
					break;
				}
			}
		}
		return channel;
	}
		
	@Override
	public void broadcast(String message) {		
		for(RemoteChannel channel: connectedChannels){
			channel.send(message);
		}	
	}
	
	@Override
	public void setErrorHandler(RemoteConnectionErrorHandler handler){
		this.errorHandler = handler;
	}
	
	/**
	 * Returns, or creates a new, channel by using its remote peer id
	 * 
	 * @param id
	 * 		The peer id of the channel endpoint
	 * @return
	 */
	private ClientRemoteChannel getChannelById(String id){
		for(ClientRemoteChannel channel : connectedChannels){
			if(channel.getId().equals(id)){
				return channel;
			}
		}
		return null;
	}
	
	@Override
	public boolean isConnected(){
		return peer != null;
	}

	@Override
	public void addIncomingConnectionListener(
			IncomingChannelConnectionListener listener) {
		incomingListeners.add(listener);
	}

	@Override
	public void addConnectedListener(ConnectedListener listener) {
		connectedListeners.add(listener);
	}	
}

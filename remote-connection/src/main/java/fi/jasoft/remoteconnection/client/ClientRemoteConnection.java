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

public class ClientRemoteConnection implements RemoteConnection {
	
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
     * Register a new Remote connection
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
	
	private final native boolean isPeerAvailable() 
	/*-{
		return typeof($wnd.Peer) === 'function'; 
	}-*/;
	
	/**
	 * Returns the unique peer id of this remote connection. 
	 * 
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * Sets the id of the connection
	 * 
	 * @param id
	 * 		The id to set
	 */
	public void setId(String id){
		if(peer != null){
			throw new IllegalStateException("Currently connected, call terminate() before setting a new id");
		}		
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
			listener.connected();
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
	
	/**
	 * Disconnects from the signalling srever and closes all channels
	 */
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
			
	/**
	 * Open a channel to another remote host
	 * 
	 * @param endpointPeerId
	 * 		The id of the remote peer
	 * @return
	 */
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
	
	/**
	 * Returns a channel
	 * 
	 * @param channelEndpointId
	 * 		The endpoint of the channel
	 * @return
	 */
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
		
	/**
	 * Broadcase a message to all connected channels
	 * 
	 * @param message
	 * 		The message to broadcast
	 */
	public void broadcast(String message) {		
		for(RemoteChannel channel: connectedChannels){
			channel.send(message);
		}	
	}
	
	/**
	 * Sets the error handler that is triggered when the connection gets an error
	 * 
	 * @param handler
	 * 		The handler to recieve the error
	 */
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
	
	/**
	 * Is the connection connected to the signalling server
	 */
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

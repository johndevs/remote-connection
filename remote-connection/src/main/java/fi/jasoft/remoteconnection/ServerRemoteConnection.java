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
package fi.jasoft.remoteconnection;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.UI;

import fi.jasoft.remoteconnection.client.RemoteConnectionClientRPC;
import fi.jasoft.remoteconnection.client.RemoteConnectionServerRPC;
import fi.jasoft.remoteconnection.shared.ConnectedListener;
import fi.jasoft.remoteconnection.shared.ConnectionError;
import fi.jasoft.remoteconnection.shared.IncomingChannelConnectionListener;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionConfiguration;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;
import fi.jasoft.remoteconnection.shared.RemoteConnectionErrorHandler;
import fi.jasoft.remoteconnection.shared.RemoteConnectionState;

/**
 * Server implementation for {@link RemoteConnection}
 * 
 * @author John Ahlroos
 */
@SuppressWarnings("serial")
@JavaScript("peer.min.js")
public class ServerRemoteConnection extends AbstractExtension implements RemoteConnection {
	
	/**
	 * A channel between two remote connections
	 *
	 */
	public class ServerRemoteChannel implements RemoteChannel{

		private final String id;
		
		private boolean connected = false;
		
		private List<ConnectedListener> connectedListeners = new LinkedList<ConnectedListener>();
		
		@Override
		public void send(String message){
			getRpcProxy(RemoteConnectionClientRPC.class).sendMessage(id, message);
		}	

		@Override
		public String getId() {
			return id;
		}
		
		@Override
		public boolean isConnected() {			
			return connected;
		}

		@Override
		public void addConnectedListener(ConnectedListener listener) {
			connectedListeners.add(listener);
		}
		
		private ServerRemoteChannel(String id){
			this.id = id;			
		}
				
		private void messageRecieved(String message) {
			for(RemoteConnectionDataListener listener : listeners){
				listener.dataRecieved(this, message);
			}			
		}
		
		private void setConnected(boolean connected, String channelId){
			this.connected = connected;
			if(connected){
				for(ConnectedListener listener : connectedListeners){
					listener.connected(channelId);
				}
			}
		}
	}
	
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
			ServerRemoteChannel channel = new ServerRemoteChannel(id);
			channels.add(channel);	
			for(IncomingChannelConnectionListener listener : incomingListeners) {
				listener.connected(channel);
			}
		}

		@Override
		public void peerConnected(String id) {
			connected = true;		
			getState(false).configuration.setId(id);
			for(ConnectedListener listener : connetedListeners) {
				listener.connected(id);
			}
		}

		@Override
		public void error(String reason) {
			if(errorHandler != null){
				errorHandler.onConnectionError(ConnectionError.get(reason), reason);
			} else {
				System.err.println("An error occurred when connecting ("+reason+")");
			}
		}

		@Override
		public void channelConnected(String id) {
			getChannelById(id).setConnected(true, id);			
		}
	};
	
	// Currently open channels
	private final List<ServerRemoteChannel> channels = new LinkedList<ServerRemoteChannel>();
	
	// Currently attached data listeners
	private final List<RemoteConnectionDataListener> listeners = new LinkedList<RemoteConnectionDataListener>();
	private final List<IncomingChannelConnectionListener> incomingListeners = new LinkedList<IncomingChannelConnectionListener>();
	private final List<ConnectedListener> connetedListeners = new LinkedList<ConnectedListener>();
	
	private boolean connected = false;
	
	private RemoteConnectionErrorHandler errorHandler;
	
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
	public static RemoteConnection register(UI ui, RemoteConnectionConfiguration configuration){
		ServerRemoteConnection peer = register(ui);
		peer.getState().configuration = configuration;
		return peer;
	}
			
	@Override
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
	
	@Override
	protected RemoteConnectionState getState(boolean markAsDirty) {		
		return (RemoteConnectionState) super.getState(markAsDirty);
	}
	
	@Override
	public void addDataListener(RemoteConnectionDataListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void addIncomingConnectionListener(IncomingChannelConnectionListener listener) {
		incomingListeners.add(listener);
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
		if(getState(false).configuration.getKey().equals(RemoteConnectionConfiguration.DEVELOPMENT_PEER_JS_KEY)){
			getLogger().warning("You are using the development key of RemoteConnection "
					+ "with a very limited amount of connections shared among all "
					+ "RemoteConnection users. You are strongly encoraged to apply " 
					+ "for your own developer key at http://peerjs.com/peerserver or "
					+ "run your own server which can be downloaded from https://github.com/peers/peerjs-server. "
					+ "You can supply your own peer server details through the RemoteConnection.getConfiguration() "
					+ "option. Thank you.");
		}
		
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

	@Override
	public void addConnectedListener(ConnectedListener listener) {
		connetedListeners.add(listener);	
	}

	/**
	 * Default construction
	 * 
	 * @param ui
	 * 		The UI the connection should be attached to
	 */
	private ServerRemoteConnection(UI ui){
		super.extend(ui);
		registerRpc(rpc);
		
		// Use Vaadin's script injection
		getState().configuration.setScriptInjected(false);
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
		return null;
	}
	
	private Logger getLogger(){
		return Logger.getLogger(ServerRemoteConnection.class.getCanonicalName());
	}

	@Override
	public RemoteConnectionConfiguration getConfiguration() {
		return getState().configuration;
	}
}

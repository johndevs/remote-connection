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

import fi.jasoft.remoteconnection.client.peer.DataConnection;
import fi.jasoft.remoteconnection.client.peer.PeerListener;
import fi.jasoft.remoteconnection.client.peer.StringPeerListener;
import fi.jasoft.remoteconnection.shared.ConnectedListener;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;

public class ClientRemoteChannel implements RemoteChannel {

	private final String id;
	
	private DataConnection connection;
			
	private List<String> messageQueue = new LinkedList<String>();

	private final List<RemoteConnectionDataListener> listeners = new LinkedList<RemoteConnectionDataListener>();
	
	private final List<ConnectedListener> connectedListeners = new LinkedList<ConnectedListener>();
	
	public ClientRemoteChannel(String id){
		assert id != null;
		this.id = id;					
	}					
			
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
	
	public DataConnection getConnection() {
		return connection;
	}
	
	public void setConnection(final DataConnection con){
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
					listener.connected();
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
	
	@Override
	public boolean isConnected() {			
		return connection != null && connection.isOpen();
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
	public void addConnectedListener(ConnectedListener listener) {
		connectedListeners.add(listener);
	}
}
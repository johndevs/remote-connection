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
package fi.jasoft.remoteconnection.shared;

/**
 * A connection for communicating remotely over WebRTC with other remotely connected remote connections.
 * 
 * @author John Ahlroos
 */
public interface RemoteConnection {
	
	/**
	 * Opens a remote channel to another remote connection
	 * 
	 * @param endpointPeerId
	 * 		The id of the other remote connection we should connect to
	 * @return
	 * 		Return a channel to the other remote connection with which we can send and recieve data
	 */
	RemoteChannel openChannel(String endpointPeerId);
	
	/**
	 * Adds a listener for receiving data from connected channels
	 * 
	 * @param listener
	 * 		The listener to attach
	 */
	void addDataListener(RemoteConnectionDataListener listener);

	/**
	 * Add a listener for listening for incomming connections from other Remote connections
	 * 
	 * @param listener
	 * 		The listener to add
	 */
	void addIncomingConnectionListener(IncomingChannelConnectionListener listener);
	
	/**
	 * Add a listener for listening to when the remote connection is connected to the signalling server.
	 * 
	 * @param listener
	 * 		The listener to add
	 */
	void addConnectedListener(ConnectedListener listener);
	
	/**
	 * Broadcast a message vi all connected channels
	 * 
	 * @param message
	 * 		The message to broadcast
	 */
	void broadcast(String message);
	
	/**
	 * Get a channel by its endpoint id
	 * 
	 * @param endpointPeerId
	 * 		The id of the remotely connected channel
	 * @return
	 * 		Return a channel to the other remote connection with which we can send and recieve data
	 */
	RemoteChannel getChannel(String endpointPeerId);
	
	/**
	 * Terminate the connection to the signalling server. Also terminates any connected channels.
	 */
	void terminate();
	
	/**
	 * Connect to the signalling server.
	 */
	void connect();
	
	/**
	 * Are we connected to the signalling server
	 */
	public boolean isConnected();
	
	/**
	 * Sets a handler that gets called when we receive an error on the connection.
	 * 
	 * @param handler
	 * 		The handler to call when an error occurs
	 */
	public void setErrorHandler(RemoteConnectionErrorHandler handler);
	
	/**
	 * Set the configuration for the connection
	 * 
	 * @param configuration
	 * 		The configuration for the connection. Should not be null.
	 */
	public RemoteConnectionConfiguration getConfiguration();
	
}

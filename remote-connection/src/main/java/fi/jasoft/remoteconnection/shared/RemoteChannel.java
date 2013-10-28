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
 * A channel is used for connecting to {@link RemoteConnection}'s. The channel 
 * provides to means for sending data from one {@link RemoteConnection} to another.
 * 
 * @author John Ahlroos
 */
public interface RemoteChannel {

	/**
	 * Send some data through the remote channel to the other remote connection. 
	 * When the data arrives to other remote connections {@link RemoteConnectionDataListener} 
	 * is invoked with the message.
	 * 
	 * @param message
	 * 		The message to send over the channel
	 */
	void send(String message);

	/**
	 * The id of the {@link RemoteConnection} the channel is connected to
	 */
	String getId();
	
	/**
	 * Is the channel connected to an {@link RemoteConnection}
	 */
	boolean isConnected();
	
	/**
	 * Adds a listener which gets triggered when a connection to the other {@link RemoteConnection} is established.
	 * 
	 * @param listener
	 * 		The listener to add
	 */
	void addConnectedListener(ConnectedListener listener);
}

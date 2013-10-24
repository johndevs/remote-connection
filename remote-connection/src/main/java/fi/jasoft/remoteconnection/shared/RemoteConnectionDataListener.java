package fi.jasoft.remoteconnection.shared;

/**
 * A listener for listening to received data messages
 */
public interface RemoteConnectionDataListener {
	
	/**
	 * Triggered when data is recieved via established remote channels
	 * 
	 * @param channel
	 * 		The channel from where the data arrived
	 * @param data
	 * 		The data message
	 */
	void dataRecieved(RemoteChannel channel, String data);
}
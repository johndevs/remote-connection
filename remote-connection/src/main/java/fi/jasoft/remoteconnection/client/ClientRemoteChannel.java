package fi.jasoft.remoteconnection.client;

import java.util.LinkedList;
import java.util.List;

import fi.jasoft.remoteconnection.client.peer.DataConnection;
import fi.jasoft.remoteconnection.client.peer.PeerListener;
import fi.jasoft.remoteconnection.client.peer.StringPeerListener;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;

public class ClientRemoteChannel implements RemoteChannel {

	private final String id;
	
	private DataConnection connection;
			
	private List<String> messageQueue = new LinkedList<String>();

	private final List<RemoteConnectionDataListener> listeners = new LinkedList<RemoteConnectionDataListener>();
	
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
}
package fi.jasoft.remoteconnection.shared;

public interface RemoteConnection {

	public String getId();
	
	public RemoteChannel openChannel(String endpointPeerId);
	
	public void addDataListener(RemoteConnectionDataListener listener);
	
	public void broadcast(String message);
	
	public RemoteChannel getChannel(String endpointPeerId);
	
	public void terminate();
	
	public void connect();
	
	public boolean isConnected();
	
	public void setId(String peerId);
	
	public void setErrorHandler(RemoteConnectionErrorHandler handler);
	
	
}

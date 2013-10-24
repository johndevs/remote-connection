package fi.jasoft.remoteconnection.shared;

public interface RemoteChannel {

	void send(String message);

	String getId();
	
	boolean isConnected();
}

package fi.jasoft.remoteconnection.shared;

public interface RemoteConnectionErrorHandler {

	boolean onConnectionError(ConnectionError error);
	
}

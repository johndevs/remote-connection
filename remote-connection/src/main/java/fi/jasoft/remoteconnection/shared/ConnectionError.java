package fi.jasoft.remoteconnection.shared;

public enum ConnectionError {
	BROWSER_INCOMPATIBLE("browser-incompatible"),
	INVALID_KEY("invalid-id"),
	INVALID_ID("invalid-key"),
	UNAVAILABLE_ID("unavailable-id"),
	SSL_UNAVAILABLE("ssl-unavailable"),
	SERVER_DISCONNECTED("server-disconnected"),
	SERVER_ERROR("server-error"),
	SOCKET_ERROR("socket-error"),
	SOCKET_CLOSED("socket-closed");
			
	private String nativeError;
	
	private ConnectionError(String nativeError) {
		this.nativeError = nativeError;
	}
	
	public static final ConnectionError get(String nativeError){
		for(ConnectionError type : ConnectionError.values()){
			if(type.nativeError.equals(nativeError)){
				return type;
			}
		}
		return null;
	}
}
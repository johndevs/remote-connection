package fi.jasoft.remoteconnection.client.peer;

import com.google.gwt.core.client.JavaScriptObject;

import fi.jasoft.remoteconnection.shared.ConnectionError;

public class PeerError extends JavaScriptObject {

	protected PeerError(){
		
	}
	
	public native final String getNativeError()/*-{
		return this.type;	
	}-*/;
	
	public final ConnectionError getType(){
		return ConnectionError.get(getNativeError());
	}	
}

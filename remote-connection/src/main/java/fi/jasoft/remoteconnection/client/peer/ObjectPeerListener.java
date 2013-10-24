package fi.jasoft.remoteconnection.client.peer;

import com.google.gwt.core.client.JavaScriptObject;

public abstract class ObjectPeerListener implements PeerListener {

	public abstract void execute(JavaScriptObject obj);
	
	@Override
	public void execute() {
		throw new UnsupportedOperationException("String return values not supported");		
	}

}

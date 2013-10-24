package fi.jasoft.remoteconnection.client.peer;


public abstract class StringPeerListener implements PeerListener {

	public abstract void execute(String str);
	
	@Override
	public void execute() {
		throw new UnsupportedOperationException("String return values not supported");		
	}
}

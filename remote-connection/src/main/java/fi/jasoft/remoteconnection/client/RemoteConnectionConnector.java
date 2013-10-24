package fi.jasoft.remoteconnection.client;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

import fi.jasoft.remoteconnection.ServerRemoteConnection;
import fi.jasoft.remoteconnection.shared.ConnectionError;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;
import fi.jasoft.remoteconnection.shared.RemoteConnectionErrorHandler;
import fi.jasoft.remoteconnection.shared.RemoteConnectionState;

@Connect(ServerRemoteConnection.class)
public class RemoteConnectionConnector extends AbstractExtensionConnector {

	private RemoteConnection connection;
	
	private RemoteConnectionServerRPC rpc = RpcProxy.create(RemoteConnectionServerRPC.class, this);
	    
    @Override
    protected void init() {    
    	super.init();
    	
    	// Create remote connection, peer.js will be registered by the @Javascript annotation
    	connection = ClientRemoteConnection.register(getState().id, false);    	  
    	
    	//Listen for incoming data
    	connection.addDataListener(new RemoteConnectionDataListener() {
			
			@Override
			public void dataRecieved(RemoteChannel channel, String data) {
				rpc.recievedMessage(channel.getId(), data);				
			}
		});
    	
    	connection.setErrorHandler(new RemoteConnectionErrorHandler() {
			
			@Override
			public boolean onConnectionError(ConnectionError error) {
				rpc.error(error.toString());
				return true;
			}
		});
    	
    	// Listen for incoming rpc
    	registerRpc(RemoteConnectionClientRPC.class, new RemoteConnectionClientRPC() {
			
			@Override
			public void sendMessage(final String channelId, final String message) {				
				connection.getChannel(channelId).send(message);	
			}
			
			@Override
			public void openChannel(final String channelId) {				
				connection.openChannel(channelId);				
			}

			@Override
			public void terminate() {
				connection.terminate();				
			}

			@Override
			public void connect() {
				connection.connect();				
			}
		});
    }    
    
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {    	
    	super.onStateChanged(stateChangeEvent);
    	if(stateChangeEvent.hasPropertyChanged("id")){
    		connection.setId(getState().id);    	
    	}
    }
            
	@Override
	protected void extend(ServerConnector target) {
		
	}
	
	@Override
	public void onUnregister() {		
		super.onUnregister();
		connection.terminate();
	}
	
	@Override
	public RemoteConnectionState getState() {		
		return (RemoteConnectionState) super.getState();
	}		
}

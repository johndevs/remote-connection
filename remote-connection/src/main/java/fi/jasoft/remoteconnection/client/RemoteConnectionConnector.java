/*
* Copyright 2013 John Ahlroos
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fi.jasoft.remoteconnection.client;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

import fi.jasoft.remoteconnection.ServerRemoteConnection;
import fi.jasoft.remoteconnection.shared.ConnectedListener;
import fi.jasoft.remoteconnection.shared.ConnectionError;
import fi.jasoft.remoteconnection.shared.IncomingChannelConnectionListener;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionConfiguration;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;
import fi.jasoft.remoteconnection.shared.RemoteConnectionErrorHandler;
import fi.jasoft.remoteconnection.shared.RemoteConnectionState;

@Connect(ServerRemoteConnection.class)
public class RemoteConnectionConnector extends AbstractExtensionConnector {

	private RemoteConnection connection;
	
	private RemoteConnectionServerRPC rpc = RpcProxy.create(RemoteConnectionServerRPC.class, this);
    
    private void createConnectionFromConfiguration(RemoteConnectionConfiguration conf) {

    	connection = ClientRemoteConnection.register(conf);
    	
    	//Listen for incoming data
    	connection.addDataListener(new RemoteConnectionDataListener() {
			
			@Override
			public void dataRecieved(RemoteChannel channel, String data) {
				rpc.recievedMessage(channel.getId(), data);				
			}
		});
    	
    	connection.setErrorHandler(new RemoteConnectionErrorHandler() {
			
			@Override
			public boolean onConnectionError(ConnectionError error, String message) {
				rpc.error(message);
				return error != ConnectionError.CHANNEL_ERROR;
			}
		});
    	
    	connection.addConnectedListener(new ConnectedListener() {
			
			@Override
			public void connected(String peerId) {
				if(peerId == null){
					throw new IllegalStateException("Peer id cannot be null");
				}				
				rpc.peerConnected(peerId);
			}
		});
    	
    	connection.addIncomingConnectionListener(new IncomingChannelConnectionListener() {
			
			@Override
			public void connected(RemoteChannel channel) {
				rpc.recievedConnection(channel.getId());				
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
				RemoteChannel channel = connection.openChannel(channelId);
				channel.addConnectedListener(new ConnectedListener() {
					
					@Override
					public void connected(String channelId) {
						rpc.channelConnected(channelId);
					}
				});
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
    	if(stateChangeEvent.hasPropertyChanged("configuration")){
    		createConnectionFromConfiguration(getState().configuration);
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

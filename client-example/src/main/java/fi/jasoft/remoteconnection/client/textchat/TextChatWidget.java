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
package fi.jasoft.remoteconnection.client.textchat;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import fi.jasoft.remoteconnection.client.ClientRemoteConnection;
import fi.jasoft.remoteconnection.shared.ConnectedListener;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionConfiguration;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;

/**
 * Client side widget. Can extend any GWT widget.
 */
public class TextChatWidget extends Composite {
       	
    private TextArea messages;
        
    private RemoteConnection peer;
    
    private TextBox myid;
            
    public TextChatWidget() {
    	buildUI();
    }
    
    private void initConnection() {
    	
    	// Create a new remote connection
    	peer = ClientRemoteConnection.register();
    	
    	// Connect to signalling server
    	peer.connect();    	
    	
    	// Listen for connection
    	peer.addConnectedListener(new ConnectedListener() {
			
			@Override
			public void connected(String id) {
				myid.setText(id);				
			}
		});
    	
    	// Listen for recieved messages
    	peer.addDataListener(new RemoteConnectionDataListener() {
			
			@Override
			public void dataRecieved(RemoteChannel channel, String data) {
				messages.setText(messages.getText()+channel.getId()+" >> "+data+"\n");				
			}
		});    	    	
    }
    
    private void buildUI(){
    	VerticalPanel vl = new VerticalPanel();

    	// Our id
    	myid = new TextBox();
    	myid.setValue("Connecting...");
    	myid.setReadOnly(true);
    	vl.add(myid);
    	
    	// Remote id
    	final TextBox remoteId = new TextBox();
    	Button connectToRemote = new Button("Connect", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RemoteChannel channel = peer.openChannel(remoteId.getValue());
				channel.addConnectedListener(new ConnectedListener() {
					
					@Override
					public void connected(String channelId) {
						remoteId.setReadOnly(true);								
						messages.setText(messages.getText()+"Connected to channel "+channelId+"\n");
					}
				});				
			}
		});    	
    	
    	HorizontalPanel pnl = new HorizontalPanel();
    	pnl.add(remoteId);
    	pnl.add(connectToRemote);
    	vl.add(pnl);
    	
    	
    	// Message display where messages are displayed    	
    	messages = new TextArea();
    	messages.setReadOnly(true);
    	messages.setWidth("400px");
    	messages.setHeight("300px");
    	vl.add(messages);
    	
    	// Message field
        final TextBox message = new TextBox();
        Button send = new Button("Send", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {

				// Show message in message window
				messages.setValue(messages.getValue()+peer.getConfiguration().getId()+" >> "+message.getValue()+"\n");
				
				// Broadcast the message to all connected peers
				peer.broadcast(message.getValue());
				
				message.setValue("");
			}
		});    	
        
        pnl = new HorizontalPanel();
    	pnl.add(message);
    	pnl.add(send);
    	vl.add(pnl);

        initWidget(vl);
    }
    
    @Override
    protected void onLoad() {    	
    	super.onLoad();
    	// Create connection
    	initConnection();
    }
    
	@Override
	protected void onUnload() {		
		super.onUnload();
		
		// Ensure channels and connection to signalling server gets terminated
		peer.terminate();
	}
    
}
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
package fi.jasoft.remoteconnection;

import java.util.UUID;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

import fi.jasoft.remoteconnection.shared.ConnectedListener;
import fi.jasoft.remoteconnection.shared.IncomingChannelConnectionListener;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;

@Theme("ServerExample")
public class ServerExampleUI extends UI{

	private RemoteConnection peer;
	
	private TextArea messages;
	
	private Label myId;
	
	protected void init(VaadinRequest request) {
		
		/*
		 *  Setup the remote connection and start listening for messages
		 */
		initConnection();
		
		/*
		 * Build the chat application UI
		 */
		buildUI();
	}
	
	private void initConnection(){
				
		// Create a connection
		peer = ServerRemoteConnection.register(this);
		
		peer.getConfiguration().setId(UUID.randomUUID().toString());
		
		// Connect to pairing server
		peer.connect();
		
		// Listen to incoming data
		peer.addDataListener(new RemoteConnectionDataListener() {					
			@Override
			public void dataRecieved(RemoteChannel channel, String data) {
				messages.setValue(messages.getValue()+channel.getId()+" >> "+data+"\n");
			}
		});
		
		// Listen for incoming connections
		peer.addIncomingConnectionListener(new IncomingChannelConnectionListener() {
			
			@Override
			public void connected(RemoteChannel channel) {
				Notification.show(channel.getId()+" is connected.", Type.TRAY_NOTIFICATION);
			}
		});
		
		// Listen for when signalling server is connected
		peer.addConnectedListener(new ConnectedListener() {
			
			@Override
			public void connected(String peerId) {
				myId.setValue(peerId);
				Notification.show("Connection establised.", Type.TRAY_NOTIFICATION);				
			}
		});		
	}
	
	private void buildUI(){
		FormLayout vl = new FormLayout();
		setContent(vl);
		
		// Our id
		myId = new Label("Connecting...");
		myId.setCaption("My id:");
		vl.addComponent(myId);
		
		// Remote id
		final TextField remoteId = new TextField();
		remoteId.setWidth("100%");
		NativeButton connectToRemote = new NativeButton("Connect", new Button.ClickListener() {
			
			@Override
			public void buttonClick(final ClickEvent event) {
				final RemoteChannel channel = peer.openChannel(remoteId.getValue());
				channel.addConnectedListener(new ConnectedListener() {
					
					@Override
					public void connected(String channelId) {
						remoteId.setReadOnly(true);
						event.getButton().setVisible(false);
						Notification.show("Connected to "+channelId, Type.TRAY_NOTIFICATION);				
					}
				});
			}
		});
		
		HorizontalLayout hl = new HorizontalLayout(remoteId, connectToRemote);
		hl.setExpandRatio(remoteId, 1);
		hl.setWidth("100%");
		hl.setCaption("Remote id: ");
		vl.addComponent(hl);
		
		// Message display where messages are displayed
		messages = new TextArea();
		messages.setWidth("100%");
		vl.addComponent(messages);
		
		// Message field
		final TextField message = new TextField();
		message.setWidth("100%");
		NativeButton send = new NativeButton("Send", new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				
				// Show message in message window
				messages.setValue(messages.getValue()+peer.getConfiguration().getId()+" >> "+message.getValue()+"\n");
				
				// Broadcast the message to all connected peers
				peer.broadcast(message.getValue());
				
				message.setValue("");
			}
		});
		
		hl = new HorizontalLayout(message, send);
		hl.setExpandRatio(message, 1);
		hl.setWidth("100%");
		hl.setCaption("Send message: ");
		vl.addComponent(hl);
	}
	
	
	
}

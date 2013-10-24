package fi.jasoft.remoteconnection.client.textchat;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import fi.jasoft.remoteconnection.client.ClientRemoteChannel;
import fi.jasoft.remoteconnection.client.ClientRemoteConnection;
import fi.jasoft.remoteconnection.client.peer.DataConnection;
import fi.jasoft.remoteconnection.client.peer.ObjectPeerListener;
import fi.jasoft.remoteconnection.client.peer.Peer;
import fi.jasoft.remoteconnection.client.peer.PeerListener;
import fi.jasoft.remoteconnection.client.peer.StringPeerListener;
import fi.jasoft.remoteconnection.shared.ConnectionError;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;
import fi.jasoft.remoteconnection.shared.RemoteConnectionErrorHandler;

/**
 * Client side widget. Can extend any GWT widget.
 */
public class TextChatWidget extends Composite implements ChangeHandler{
       
    private TextArea chatWindow = new TextArea();
    
    private TextBox messageField = new TextBox();
    
    RemoteConnection remote;
        
    public TextChatWidget() {
    	
    	chatWindow.setReadOnly(true);
    	chatWindow.setWidth("400px");
    	chatWindow.setHeight("300px");
    	
        messageField.addChangeHandler(this);
        messageField.setWidth("400px");
        
        VerticalPanel vl = new VerticalPanel();
        vl.add(chatWindow);
        vl.add(messageField);
        
        initWidget(vl);
    }
    
    public void initPeerConnection(final String id) {
    	
    	// Create a new remote connection
    	remote = ClientRemoteConnection.register(id);
    	
    	// Set an error handler
    	remote.setErrorHandler(new RemoteConnectionErrorHandler() {
			
			@Override
			public boolean onConnectionError(ConnectionError error) {
				
				if(error == ConnectionError.UNAVAILABLE_ID){
					// Try again with a concatenated id					
					initPeerConnection("_"+id+"_");					
				} else {
					// Just give up
					chatWindow.setText("Connection error. ("+error+")");
					messageField.setEnabled(false);
				}
				return true;
			}
		});
    	
    	// Connect to signalling server
    	remote.connect();
    	
    	// Listen for recieved messages
    	remote.addDataListener(new RemoteConnectionDataListener() {
			
			@Override
			public void dataRecieved(RemoteChannel channel, String data) {
				writeToChat(channel.getId()+" >> "+data);				
			}
		});    	
    }
    
    private void writeToChat(String text){
    	chatWindow.setText(chatWindow.getText()+"\n"+text);
    }
            
	@Override
	public void onChange(ChangeEvent event) {
		writeToChat(remote.getId()+" >> "+messageField.getText());
		
		// Send message to all channels
		remote.broadcast(messageField.getText());		
	}
	
	public void addConnection(String peerId) {
		remote.openChannel(peerId);
	}
	
	@Override
	protected void onUnload() {		
		super.onUnload();
		
		// Ensure channels and connection to signalling server gets terminated
		remote.terminate();
	}
    
}
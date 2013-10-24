package fi.jasoft.remoteconnection.client.textchat;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.AbstractComponentState;
import com.vaadin.shared.ui.Connect;

import fi.jasoft.remoteconnection.client.textchat.TextChatWidget;
import fi.jasoft.remoteconnection.server.textchat.TextChat;
import fi.jasoft.remoteconnection.shared.textchat.TextChatState;

/**
 * Connects the server side component with the client side widget
 */
@Connect(TextChat.class)
public class TextChatConnector extends AbstractComponentConnector {

    @Override
    public TextChatWidget getWidget() {
        return (TextChatWidget) super.getWidget();
    }
    
    @Override
    public TextChatState getState() {
    	return (TextChatState) super.getState();
    }
    
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
    	super.onStateChanged(stateChangeEvent);
    	
    	if(stateChangeEvent.hasPropertyChanged("peerConnectionId")){
    		getWidget().initPeerConnection(getState().peerConnectionId);   	
    	}
    	
    	if(stateChangeEvent.hasPropertyChanged("peerConnectionTarget")){
    		if(getState().peerConnectionTarget != null){
    			getWidget().addConnection(getState().peerConnectionTarget);
    		}    		
    	}    	
    }    
}


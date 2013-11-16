package fi.jasoft.chatroom.client;

import java.util.ArrayList;

import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

import fi.jasoft.chatroom.ChatRoom;
import fi.jasoft.chatroom.shared.ChatRoomState;
import fi.jasoft.remoteconnection.shared.ConnectedListener;

@Connect(ChatRoom.class)
public class ChatRoomConnector extends AbstractComponentConnector {

	private ChatRoomServerRpc rpc = RpcProxy.create(ChatRoomServerRpc.class, this);
	
	@Override
	protected void init() {		
		super.init();
		getWidget().getConnection().addConnectedListener(new ConnectedListener() {
			
			@Override
			public void connected(String id) {
				rpc.setPeerId(id);				
			}
		});
	}	
	
	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);
		getWidget().setUsers(new ArrayList<String>(getState().users));
		getWidget().setUsername(getState().username);
	}
	
	@Override
	public ChatRoomApplication getWidget() {
		return (ChatRoomApplication) super.getWidget();
	}
	
	@Override
	public ChatRoomState getState() {
		return (ChatRoomState) super.getState();
	}
}

package fi.jasoft.chatroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.server.WebBrowser;
import com.vaadin.ui.AbstractComponent;

import fi.jasoft.chatroom.client.ChatRoomServerRpc;
import fi.jasoft.chatroom.shared.ChatRoomState;

public class ChatRoom extends AbstractComponent{
	
	private static List<ChatRoom> firefoxRooms = new ArrayList<ChatRoom>();
	
	private static List<ChatRoom> chromeRooms = new ArrayList<ChatRoom>();
	
	private String peerId;
	
	public ChatRoom(String username) {
		setSizeFull();
		getState().username = username;
		registerRpc(new ChatRoomServerRpc() {
			
			@Override
			public void setPeerId(String peerId) {
				ChatRoom.this.peerId = peerId;	
				updateUserList();
			}
		});
	}
	
	@Override
	public void attach() {		
		super.attach();
		WebBrowser browser = getUI().getSession().getBrowser();
		if(browser.isFirefox()){
			firefoxRooms.add(this);
		} else if(browser.isChrome()){
			chromeRooms.add(this);
		}
	}
	
	@Override
	public void detach() {		
		firefoxRooms.remove(this);
		chromeRooms.remove(this);
		updateUserList();
		super.detach();
	}
		
	@Override
	protected ChatRoomState getState() {
		return (ChatRoomState) super.getState();
	}
	
	@Override
	protected ChatRoomState getState(boolean markAsDirty) {
		return (ChatRoomState) super.getState(markAsDirty);
	}
	
	public String getId(){
		return peerId;
	}
	
	private void updateUserList(){
		final List<String> users = new ArrayList<String>();
		
		WebBrowser browser = getUI().getSession().getBrowser();
		List<ChatRoom> rooms = Collections.emptyList();
		if(browser.isFirefox()){
			rooms = firefoxRooms;
		} else if(browser.isChrome()){
			rooms = chromeRooms;
		}
		
		for(ChatRoom room : rooms) {
			if(room.getId() != null){
				users.add(room.getId());
			}			
		}
		for(final ChatRoom room : rooms) {
			room.getUI().access(new Runnable() {
				
				@Override
				public void run() {
					room.getState().users = new HashSet<String>(users);					
				}
			});			
		}
	}
}

package fi.jasoft.chatroom.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.shared.AbstractComponentState;

public class ChatRoomState extends AbstractComponentState {
	
	public Set<String> users = new HashSet<String>();
	
	public String username;
	
}

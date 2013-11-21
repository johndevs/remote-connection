package fi.jasoft.chatroom.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import fi.jasoft.remoteconnection.client.ClientRemoteConnection;
import fi.jasoft.remoteconnection.shared.ConnectedListener;
import fi.jasoft.remoteconnection.shared.IncomingChannelConnectionListener;
import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnection;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;

public class ChatRoomApplication extends Composite {

	private RemoteConnection connection;
	
	private HTML messages = new HTML();
	
	private TextArea input = new TextArea();
	
	private CellList<String> users = new CellList<String>(new TextCell());
	
	private String id;

	private String username;
		
	private String selectedUserId;
	
	private Map<String,String> userNames = new HashMap<String, String>();
	{
		userNames.put("", "* Chat Users *");
	}
	
	public ChatRoomApplication() {
		FlowPanel root = new FlowPanel();
		initWidget(root);
		
		FlowPanel hp = new FlowPanel();
		hp.setStyleName("chat");
		root.add(hp);
		
		messages.setStyleName("message-box");
		hp.add(messages);
		
		input.setStyleName("chat-message-box");
		input.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
					
					if(selectedUserId != null){
						connection.getChannel(selectedUserId).send(input.getText());
						printMessage("<b>"+username+" -> "+userNames.get(selectedUserId)+":</b> "+input.getText());
					} else {
						connection.broadcast(input.getText());
						printMessage("<b>"+username+":</b> "+input.getText());
					}				
					
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							input.setText(null);							
						}
					});					
				}				
			}
		});
		hp.add(input);

		users.setStyleName("user-list");
		
		final SingleSelectionModel<String> sm = new SingleSelectionModel<String>();
		users.setSelectionModel(sm);
		sm.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {								
				if(sm.getSelectedObject().equals(userNames.get(""))){
					sm.clear();
					selectedUserId = null;
				} else {
					for(Entry<String,String> e : userNames.entrySet()){
						if(e.getValue().equals(sm.getSelectedObject())){
							selectedUserId = e.getKey();										
							return;
						}
					}		
				}				
			}
		});	
			
		root.add(users);	
		
		connection = ClientRemoteConnection.register();
		
		connection.addConnectedListener(new ConnectedListener() {
			
			@Override
			public void connected(String peerId) {
				id = peerId;
				userNames.put(peerId, username);
				printMessage("Connected to chat server. <em> Hi "+username+"!</em>");
				updateUserList();
			}
		});	
		
		connection.addDataListener(new RemoteConnectionDataListener() {
			
			@Override
			public void dataRecieved(RemoteChannel channel, String data) {
				if(data.equals("username")){
					// Username requested, send it
					channel.send("username:"+username);
				} else if(data.startsWith("username:")){
					// Recieved username
					userNames.put(channel.getId(), data.substring("username:".length()));
					updateUserList();
					printMessage(userNames.get(channel.getId())+" joined the channel.");
				} else if(data.equals("disconnected")){
					printMessage(userNames.get(channel.getId())+" left the channel.");
					userNames.remove(channel.getId());
					updateUserList();					
				} else {
					// Data message
					printMessage("<b>"+userNames.get(channel.getId())+":</b> "+data);				
				}
			}
		});
		
		connection.addIncomingConnectionListener(new IncomingChannelConnectionListener() {
			
			@Override
			public void connected(RemoteChannel channel) {					
				// Request user name
				channel.send("username");
			}
		});
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();						
		connection.connect();
	}
	
	@Override
	protected void onUnload() {
		// Notify others I am disconnecting
		connection.broadcast("disconnected");
		
		// Terminate
		connection.terminate();
		super.onUnload();
	}
	
	private void printMessage(String message) {
		if(messages.getHTML().equals("")){
			messages.setHTML(message);
		} else {
			messages.setHTML(messages.getHTML()+"<br/>"+message);			
		}
	}
	
	private void updateUserList(){
		this.users.setRowData(new ArrayList<String>(userNames.values()));				
	}
	
	public void setUsers(List<String> users){		
		for(String user : users){
			RemoteChannel channel = connection.openChannel(user);			
			channel.send("username");
		}
	}
	
	public RemoteConnection getConnection(){
		return connection;
	}
		
	public void setUsername(String username) {
		this.username = username;
	}
}

package fi.jasoft.remoteconnection;

import java.util.UUID;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.annotations.Theme;

import fi.jasoft.remoteconnection.shared.RemoteChannel;
import fi.jasoft.remoteconnection.shared.RemoteConnectionDataListener;

@Theme("ServerExample")
public class ServerExampleUI extends UI{

	protected void init(VaadinRequest request) {
		VerticalLayout vl = new VerticalLayout();
		setContent(vl);
		
		final String id = UUID.randomUUID().toString();
		vl.addComponent(new Label(id));
		
		// Create a connection
		final ServerRemoteConnection peer = ServerRemoteConnection.register(this, id);
		
		// Connect the connection
		peer.connect();
		
		// Listen to incoming data
		peer.addDataListener(new RemoteConnectionDataListener() {
			
			@Override
			public void dataRecieved(RemoteChannel channel, String data) {
				Notification.show(data);				
			}
		});
		
		final TextField connectTo = new TextField();
		connectTo.setImmediate(true);
		vl.addComponent(connectTo);
		
		final TextField message = new TextField();
		message.setImmediate(true);
		vl.addComponent(message);
		
		NativeButton connect = new NativeButton("Connect and send");
		connect.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				RemoteChannel channel = peer.openChannel(connectTo.getValue());
				channel.send(message.getValue());					
			}
		});
		vl.addComponent(connect);
	}
}
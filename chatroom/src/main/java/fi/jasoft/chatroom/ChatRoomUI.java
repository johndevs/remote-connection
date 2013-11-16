package fi.jasoft.chatroom;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("ChatRoom")
@PreserveOnRefresh
public class ChatRoomUI extends UI{	
	
	private Label poweredby = new Label("Powered by <a href='http://www.vaadin.com'>Vaadin</a> "
			+ "and <a href='https://vaadin.com/directory#addon/remoteconnection'>Remote Connection</a>",
			ContentMode.HTML);
		
	@Override
	protected void init(VaadinRequest request){
		final VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		setContent(vl);
				
		final Label header = new Label("<span class='header'>ChatRoom</span>v0.1", ContentMode.HTML);
		header.setSizeUndefined();
		header.setStyleName("header-large");
		vl.addComponent(header);
		vl.setComponentAlignment(header, Alignment.MIDDLE_CENTER);
		
		WebBrowser browser = getUI().getSession().getBrowser();
		if(!browser.isFirefox() && !browser.isChrome()){
			Label msg = new Label("I am sorry but your browser is not supported by the chat application. "
					+ "Please use the latest stable version of either Chrome or Firefox.");
			msg.setWidth("450px");
			vl.addComponent(msg);
			vl.setComponentAlignment(msg, Alignment.MIDDLE_CENTER);
			return;
		}
		
		final TextField username = new TextField();
		username.setInputPrompt("Nickname");
		NativeButton enter = new NativeButton("Enter", new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				vl.removeAllComponents();
				
				header.setStyleName("header-small");
				header.setWidth("100%");
				vl.addComponent(header);
				
				ChatRoom room = new ChatRoom(username.getValue());
				vl.addComponent(room);				
				
				vl.addComponent(poweredby);
				vl.setComponentAlignment(poweredby, Alignment.BOTTOM_RIGHT);		
				
				vl.setExpandRatio(room, 1);
			}
		});
			
		vl.addComponent(new HorizontalLayout(username, enter));	
		vl.setComponentAlignment(vl.getComponent(1), Alignment.MIDDLE_CENTER);
		
		poweredby.setSizeUndefined();
		poweredby.setStyleName("poweredby");
		vl.addComponent(poweredby);
		vl.setComponentAlignment(poweredby, Alignment.BOTTOM_RIGHT);		
		
		vl.setExpandRatio(header, 1);
		vl.setExpandRatio(vl.getComponent(1), 2);
		
		
	}
}

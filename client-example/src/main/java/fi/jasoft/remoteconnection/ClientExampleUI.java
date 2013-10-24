package fi.jasoft.remoteconnection;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.annotations.Theme;

import fi.jasoft.remoteconnection.server.textchat.TextChat;

@Theme("ClientExample")
public class ClientExampleUI extends UI{
	
	private static final List<String> people = new LinkedList<String>();
	
	protected void init(VaadinRequest request){
		VerticalLayout vl = new VerticalLayout();
		setContent(vl);	
		
		HorizontalLayout hl = new HorizontalLayout();
		vl.addComponent(hl);
		
		hl.addComponent(new Label("Select a nickname: "));

		final TextField nick = new TextField();
		hl.addComponent(nick);
		
		hl.addComponent(new Label(" and talk to "));
		
		final ComboBox peopleSelect = new ComboBox(null, people);
		peopleSelect.setImmediate(true);
		hl.addComponent(peopleSelect);	
		
		hl.addComponent(new NativeButton("Connect", new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				String name = nick.getValue();
				String person = (String) peopleSelect.getValue();
				connectToChat(name, person);
			}
		}));
	}
	
	private void connectToChat(String me, String person){
		people.add(me);
		
		VerticalLayout vl = (VerticalLayout) getContent();
		vl.removeAllComponents();
		
		TextChat chat = new TextChat(me);
		vl.addComponent(chat);
		
		if(person != null){
			chat.connect(person);
		}
	}
}

package fi.jasoft.chatroom;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import com.vaadin.server.VaadinServlet;

@WebServlet(
    urlPatterns={"/*","/VAADIN/*"},
    initParams={
        @WebInitParam(name="ui", value="fi.jasoft.chatroom.ChatRoomUI"),
		@WebInitParam(name="widgetset", value="fi.jasoft.chatroom.ChatRoomWidgetset")
    })
public class ChatRoomServlet extends VaadinServlet { }

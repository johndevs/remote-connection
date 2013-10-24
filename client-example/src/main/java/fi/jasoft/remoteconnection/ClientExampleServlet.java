package fi.jasoft.remoteconnection;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import com.vaadin.server.VaadinServlet;

@WebServlet(
    urlPatterns={"/*","/VAADIN/*"},
    initParams={
        @WebInitParam(name="ui", value="fi.jasoft.remoteconnection.ClientExampleUI"),
		@WebInitParam(name="widgetset", value="fi.jasoft.remoteconnection.ClientExampleWidgetset")
    })
public class ClientExampleServlet extends VaadinServlet { }

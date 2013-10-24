package fi.jasoft.remoteconnection;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import com.vaadin.server.VaadinServlet;

@WebServlet(
    urlPatterns={"/*","/VAADIN/*"},
    initParams={
        @WebInitParam(name="ui", value="fi.jasoft.remoteconnection.ServerExampleUI"),
		@WebInitParam(name="widgetset", value="fi.jasoft.remoteconnection.ServerExampleWidgetset")
    })
public class ServerExampleServlet extends VaadinServlet { }

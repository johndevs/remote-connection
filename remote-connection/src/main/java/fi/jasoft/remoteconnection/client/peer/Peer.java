package fi.jasoft.remoteconnection.client.peer;

import com.google.gwt.core.client.JavaScriptObject;

public class Peer extends JavaScriptObject {
	
	public static native final Peer create(String id, PeerOptions options)
	/*-{	    
	  	var cfg = JSON.parse(options.@fi.jasoft.remoteconnection.client.peer.PeerOptions::config);
	 	return $wnd.Peer(id, {
	 		key: options.@fi.jasoft.remoteconnection.client.peer.PeerOptions::key, 
	 		host: options.@fi.jasoft.remoteconnection.client.peer.PeerOptions::host,
	 		port: options.@fi.jasoft.remoteconnection.client.peer.PeerOptions::port,
	 		secure: options.@fi.jasoft.remoteconnection.client.peer.PeerOptions::secure,
	 		config: cfg,
	 		debug: options.@fi.jasoft.remoteconnection.client.peer.PeerOptions::debug
	 	}); 		 	
	}-*/;
	
	public static final Peer create(String id){
		return create(id, new PeerOptions());
	}
	
	protected Peer(){}

	//TODO make action an enum
	public native final void addListener(String action, StringPeerListener listener)
	/*-{
	  	this.on(action, function(str){
	  		listener.@fi.jasoft.remoteconnection.client.peer.StringPeerListener::execute(Ljava/lang/String;)(str); 
	  	});
	 }-*/;
	
	//TODO make action an enum
	public native final void addListener(String action, ObjectPeerListener listener)
		/*-{
		  	this.on(action, function(obj){
		  		listener.@fi.jasoft.remoteconnection.client.peer.ObjectPeerListener::execute(Lcom/google/gwt/core/client/JavaScriptObject;)(obj); 
		  	});
		 }-*/;
	

	public native final void addListener(String action, PeerListener listener)
	/*-{
	  	this.on(action, function(){
	  		listener.@fi.jasoft.remoteconnection.client.peer.PeerListener::execute()(); 
	  	});
	 }-*/;
	
	public native final DataConnection connect(String id) 
	/*-{
  		return this.connect(id);
 	}-*/;
	
	public native final String getId() /*-{
		this.id;
	}-*/;
	
	public native final void disconnect()
	/*-{
		this.disconnect();
	}-*/;
	
	public native final void destroy()
	/*-{
		this.destroy();
	}-*/;
	
	public native final void send(String message)
	/*-{
		this.send(message);
	}-*/;
	
	public native final boolean isConnected() 
	/*-{
  		return !this.disconnected;
 	}-*/;
	
	public native final boolean isDestroyed() 
	/*-{
  		return this.destroyed;
 	}-*/;
}

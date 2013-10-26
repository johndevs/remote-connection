/*
* Copyright 2013 John Ahlroos
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
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
	
	public static native final Peer create(PeerOptions options)
	/*-{	    
	  	var cfg = JSON.parse(options.@fi.jasoft.remoteconnection.client.peer.PeerOptions::config);
	 	return $wnd.Peer({
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
	
	public static final Peer create(){
		return create(new PeerOptions());
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

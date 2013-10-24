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

public class DataConnection extends JavaScriptObject {

	protected DataConnection(){
		
	}
	
	public native final void send(String data) 
	/*-{
	 	this.send(data);
	 }-*/;
	
	//TODO make action an enum
	public native final void addListener(String action, StringPeerListener listener)
	/*-{
	  	this.on(action, function(str){
	  		listener.@fi.jasoft.remoteconnection.client.peer.StringPeerListener::execute(Ljava/lang/String;)(str); 
	  	});
	 }-*/;
	
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
	
	public final void addDataListener(StringPeerListener listener) {
		addListener("data", listener);
	}
	
	public native final String getPeerId()
	/*-{
 		return this.peer;
 	}-*/;
	
	public native final boolean isOpen()
	/*-{
		return this.open;
	}-*/;
}
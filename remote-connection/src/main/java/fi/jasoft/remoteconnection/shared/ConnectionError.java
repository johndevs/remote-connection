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
package fi.jasoft.remoteconnection.shared;

/**
 * Error triggered when something goes wrong connecting to the signalling server.
 * 
 * @author John Ahlroos
 */
public enum ConnectionError {
	BROWSER_INCOMPATIBLE("browser-incompatible"),
	INVALID_KEY("invalid-id"),
	INVALID_ID("invalid-key"),
	UNAVAILABLE_ID("unavailable-id"),
	SSL_UNAVAILABLE("ssl-unavailable"),
	SERVER_DISCONNECTED("server-disconnected"),
	SERVER_ERROR("server-error"),
	SOCKET_ERROR("socket-error"),
	SOCKET_CLOSED("socket-closed"),
	TIMEOUT("socket-timeout"),
	
	CHANNEL_ERROR("channel-error");
			
	private String nativeError;
	
	private ConnectionError(String nativeError) {
		this.nativeError = nativeError;
	}
	
	public static final ConnectionError get(String nativeError){
		for(ConnectionError type : ConnectionError.values()){
			if(type.nativeError.equals(nativeError)){
				return type;
			}
		}
		return null;
	}
}
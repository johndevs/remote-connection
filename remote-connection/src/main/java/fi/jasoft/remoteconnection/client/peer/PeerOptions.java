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

public class PeerOptions {

	public String key = "ggsb0b2ioiy919k9";
	
	public int port = 9000;
	
	public String host = "0.peerjs.com";
	
	public boolean secure = false;
	
	public String config = "{ \"iceServers\": [{ \"url\": \"stun:stun.l.google.com:19302\" }] }";
	
	public int debug = 0;	
}

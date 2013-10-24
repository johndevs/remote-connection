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

public interface RemoteConnection {

	public String getId();
	
	public RemoteChannel openChannel(String endpointPeerId);
	
	public void addDataListener(RemoteConnectionDataListener listener);
	
	public void broadcast(String message);
	
	public RemoteChannel getChannel(String endpointPeerId);
	
	public void terminate();
	
	public void connect();
	
	public boolean isConnected();
	
	public void setId(String peerId);
	
	public void setErrorHandler(RemoteConnectionErrorHandler handler);
	
	
}

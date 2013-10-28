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
 * Listener for listening to incoming {@link RemoteChannel}'s from external {@link RemoteConnection}'s
 * 
 * @author John Ahlroos
 */
public interface IncomingChannelConnectionListener {

	/**
	 * Invoked when an external {@link RemoteConnection} has established a channel
	 * 
	 * @param channel
	 * 		The channel that got established between the {@link RemoteConnection}'s
	 */
	void connected(RemoteChannel channel);
}

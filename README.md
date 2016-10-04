# RemoteConnection addon for Vaadin

# This project has reached End-Of-Life (EOL) and is no longer maintained.

The RemoteConnection addon can be used with Vaadin or GWT to establish a WebRTC communication channel between two browsers without needed to communicate with the server. WebRTC is currently supported both by the latest stable Chrome and Firefox versions. Even if it should work cross browser I have found that currently doing WebRTC between Chrome and Firefox will not work reliably and using the current version of the addon is only recommended between the same browsers.

The addon uses Peer.js (http://www.peerjs.com) as the underlaying technology for establishing the WebRTC connections and a default PeerServer Cloud service (http://peerjs.com/peerserver) as the signalling server.

Usage
-----
To use the addon in your application you will need to include the addon jar in your project. This can be done by either copying the addon jar into your WEB-INF/lib directory or using a dependency management system like Gradle or Maven to include the addon as a dependency. 

If you are building a Vaadin application you will need to recompile the widgetset after you have added the addon jar since the addon contains client side code. If you are using GWT then you need to inherit the **fi.jasoft.remoteconnection.RemoteConnectionWidgetset** in your applications module xml.

The API for the addon is *almost* identical wheather you are working with GWT or with Vaadin. In Vaadin the RemoteConnection is an UI Extension which means you will always attach it to the users UI. In GWT it is just an object you invoke. 

Lets take a look at the different steps needed for establishing a connection in your application.

The first thing you need to in your application to establish a connection between two endpoints is connect to a signalling server shared by all application clients. The signalling server is the means to how the two endpoints will find eachother. Here you can use the Peer.js provided cloud server, or use your own. By default the addon will use a development server on the Peer.js cloud for demo purposes and to get you started. But it has a very limited amount of connections and you should definitly either create your own peer.js cloud server account (free) or download their server from github and run it locally (more secure when the application is used in an intranet for instance). Here is the code to get the client connected to the peer server:

Vaadin (inside UI.init() for example ):
```java
// Get a connection instance
RemoteConnection peer = ServerRemoteConnection.register(UI.getCurrent());

// Make the connection to the signalling server
peer.connect();
```

GWT:
```java
// Get a connection instance
RemoteConnection peer = ClientRemoteConnection.register();

// Make the connection to the signalling server
peer.connect();
```

That is all what is neede to get started. It will establish a connection with the signalling server and return a connection you can use to connect to other clients on the same peer server. Each client will recieve a unique id from the signalling server which is used to identify the client with when establishing a channel between two clients on the signalling server. Lets next take a look at how we can establish the connection. The same code from this point forward can both be used in Vaadin as well as in GWT.

Lets assume we already know that another client is running somewhere on the internet connected to the signalling server and has recieved the following id 'xyz'. Now, to make a connection to that client we do the following:

```java
// Create a channel to another peer
RemoteChannel channel = peer.openChannel("xyz");

// Listen for connection
peer.addConnectedListener(new ConnectedListener() {
			
	@Override
	public void connected(String id) {
		System.out.println("Connected to "+id);				
	}
});
```

That code with set up a bi-directional channel between my connection and another connection. When the connection is established the RemoteConnection instance will recieve a notification that a new channel has been established.

Okay, we now can establish a connection which is nice because now we can start sending messages using the established channel. To do so we do the following:

```java
// Send a message thought the channel to 'zyx'
channel.send("Hello world");

// Or we can broadcast to all connected channels with
peer.broadcast("Hello all the connected peers!");

```

That is how we can send messages though the channel. We can either send a message to a specific channel or broadcast on all the channels. 

But sending messages is only half the part of communication, we also need to be able to recieve messages. For that we need to listen for them.

```java
// Listen for messages
peer.addDataListener(new RemoteConnectionDataListener() {                                        

	@Override
	public void dataRecieved(RemoteChannel channel, String data) {
		System.out.println("Recieved message from "+channel.getId()+": "+data);                           
        }
});
```

By attaching the data listener we recieve an event whenever a message arrives on a channel. We recieve the channel on which the message came as the message data itself. 

That is what is needed for the most basic kind of application. There are a lot of options you could use to tweak the experience. Now go make your own chat application! That is what I did :)




 







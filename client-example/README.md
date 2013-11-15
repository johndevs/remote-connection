Example application using purely RemoteConnection on the client side.
=====================================================================

Running the application
-----------------------
To run the example you will need to run the following command from the example directory in a console. 
```
../.gradlew vaadinRun
```
It will build the application and run a jetty server on http://localhost:8080

Using the application
----------------------
The application is a simple chat application where two users can chat with eachother over WebRTC. You can try out the application
on your own machine by opening two Firefox or Chrome private windows with the application url. Then take the connection id from 
one of the applications and paste it into to other windows applications connect field and press the connect button. After a while
you will see the connected message and you can send messages to and from each window.

You can also do this between any two machines on the internet. Just make sure you are using the same browser on both machines as webRTC
as a technology is still very young and does not reliably work across browsers yet.

The code
--------
The important bits of the example application can all be found in **fi.jasoft.remoteconnection.client.textchat.TextChatWidget**. All other
classes are just boiler plate for getting the Vaadin application running.



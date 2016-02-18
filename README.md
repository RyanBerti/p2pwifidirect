2-17-2016

This code was originally developed as a final project for the EE579 graduate level Wireless and Mobile Networks Design Lab at the University of Southern California (led by Bhaskar Krishnamachari); the project was completed and submitted in May of 2012. At that time, the project was posted to Jay Huang's github (huangjay/multihop), though the Android layout files were not included in the public repo. Between 2012 and 2016 I have received more than 50 requests for the missing files from students and researchers from around the world. This reinforces my belief that the implementation of ad-hoc networks on our modern portable network-enabled devices is both a viable topic for continued research/development, as well as a common goal. Unfortunately, at the time of development we found that the Wifi Direct implementation available through Android would only allow networks with star topologies (during the initial connection between two devices, one device was deemed to be the master and one the slave; from then on new connections from other devices could only be made to the master). This limitation has received much attention, and is detailed by Android issue #82 (https://code.google.com/p/android/issues/detail?id=82). I cannot comment whether this WifiDirect / Android behavior has changed since the development of the P2PWifiDirect project, though I hope that by posting the project in it's full form it's design can continued to be referenced and built upon. DISCLAIMER: This project has not been updated since 2012, so there may be various tweaks required to get it to build and run using the modern Android SDK. I will not be able to provide any assistance in this area, as I have not done any Android-specific Java in more than a few years :).

Relevant text from the original EE579 project website copied below:




Overview: 
 
What is multi-hop messaging?
Most regular wireless connections are single hop.
Devices act as both clients and access points.
Data is passed from one wireless device to another whenever a connection is available.
Devices act as mobile routers, exchanging data until it reaches it destination
 
What is Wifi-direct
Allows Android devices to act as both clients and access points and exchange data via a wifi connection, but leaves implementation of multi-hop routing up to the developer.
 
Many Different Ways Todo The Same Thing
There have been many different routing schemes proposed for multi-hop wireless communication, similarly many different possible applications.
A multilayer design abstracts components such that they can be easily swapped
 
 
Our Multilayer Design
 
Connection Manager – initiate and manage Wifi-Direct connections between peers.
Routing Manager – stores and forwards messages using an epidemic routing protocol.
File Manager – submits and handles file requests from other mobile devices.
 
Future Applications of Wireless Multi-hop Communications
Mobile device to mobile device (and also vehicle to vehicle) communications in the absence of a static infrastructure.
Mobile device to mobile device communications that coexist within larger cell infrastructure (free text messaging in a local area).




Design:

The 3 layers of our design include:
 
1. Connection Manager - responsible for connection and handshake with all peers.
 
2. Routing Manager - responsible for epidemic routing of all messages by a simple store and forward mechanism.
 
3. File Discover Manager - responsible for submitting and handling file requests from other devices.
 
 
 
A brief explanation of what each file does is given below.
 
P2PWifiDirectActivity.java
- This is our main activity file which has the console, the list view and the tabs for the 3 managers.
 
 
P2PConnectionAdapter.java
- This file gives the list view of all possible connections.
 
 
P2PConnectionManager.java
- This is our Connection Manager class.We first grab the MAC address,then set up the scan alarm which repeats every 5 seconds when scanning is on. We have an intent filter where we register to receive android wifi p2p intents connection intents and the scan alarm.This class has the following methods:
 
discoverPeers-	once this is called, android throws a PEERS_CHANGED_EVENT if successful
 
startDiscovery-	allow manager to discover peers and connect to other devices
 
stopDiscovery-	stop trying to connect to other devices
 
closeConnections-	removes all connections associated with the channel then calls the disconnect method of each connection
 
onFailure- this is the callback when an android framework call is not successful
 
onReceive- this is the method that gets called when a broadcast intent is thrown by the android framework that we registered for during initialization
 
After a success full discoverPeers call, we get the peer list via the onPeersChanged callback. When a connection is established/broken we grab the network info object and check if a connection was established.
 
onPeersAvailable - this is the callback from the requestPeers call. This creates a new P2PConenction object based on a null device set the device when we loop through the peers P2PConnection has overridden the equals method so we can compare two objects based on their underlying WifiP2PDevice.
 
createNewP2PConnection - this is the callback from the clienthandshake/serverhandshake asynctask. clienthandshake/serverhandshake asynctask set up the handshake mechanism between the connected phones.
 
 
P2PConnection.java
- This is where we have the connection info and thereby call either sendMessage service or the receiveMessage service.
 
 
P2PRoutingManager.java
- This file defines our routing manager class.
 
The routing manager has an intent register which registers the following intents:
 
newConnection - When we have a new connection where in all the messages are forwarded.
 
MessageReceived - this intent is passed when we have an incoming message.
 
OutgoingApplicationMessage - This intent is passed when the routing manager receives an outgoing application message that needs to be forwarded to all peers.
 
 
P2PMessage.java
- This defines the message format for the outgoing and incoming messages.
 
 
P2PMessageAdapter.java
- This provides a listview for the messages.
 
 
P2PSendMessage.java
- SendMessage service.
 
 
P2PReceiveMessage.java
- ReceiveMessage service.
 
 
P2PFileDiscoveryManager.java
- This manager registers incomingApplicationMessage or OutgoingApplicationMessage intents. It then checks where exactly the file is located in the device and returns an appropriate response.
 
 
P2PFileRequest.java
- This defines the filerequest class.
 
 
P2PFileRequestAdapter.java
- provides a listview for the requested files.
 
 
Our Contributions to multi-hop messaging over Wifi-direct
Handshaking protocol is created at the connection layer which has peers exchange necessary information (ports/IP/MAC) that Wifi Direct doesn't provide (only provides info about group owner)
A messaging service at the routing layer whose messages are easy to extend and whose service is easy to implement and use
We've shown how to pass intents and data between layers so that future developers can easily build out their own layers which implement more advanced management techniques
A file request and searching service at file manager layer allows users to send a file request to peers, and search file in their local phone for peers’ file request.

 


P2PWifiDirect references:
1. "A Multilayer Application for Multi-hop messaging on Android devices" by Ryan Berti, Abinand Kishore, Jay Huang - http://anrg.usc.edu/ee579_2012/Group02/index.html

2. "Automatic Android-based Wireless Mesh Networks" by Paul Wong, Vijay Varikota, Duong Nguyen and Ahmed Abukmail - http://www.informatica.si/index.php/informatica/article/viewFile/713/583


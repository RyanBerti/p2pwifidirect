package com.p2pwifidirect.connectionmanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.Layout;
import android.widget.TextView;

public class P2PRoutingManager extends BroadcastReceiver {
	
	final int MinutesTillTimeout = 2;
	
	TextView console;
	Context cntxt;
	P2PConnectionManager connectionmanager;
	ArrayList<P2PMessage> messagelist;
	P2PMessageAdapter adapter;
	SimpleDateFormat dateformat;
	AlarmManager alrmmgr;
	
	public P2PRoutingManager(Context c, P2PConnectionManager conmgr, TextView con){
		cntxt = c;
		connectionmanager = conmgr;
		console = con;
		messagelist = new ArrayList<P2PMessage>();
		adapter = new P2PMessageAdapter(cntxt,messagelist);
		dateformat = new SimpleDateFormat("HH:mm:ss");
		
		
		//P2PMessage msg = new P2PMessage(connectionmanager.myMAC,"ff:ff:ff:ff:ff:ff","hello world");
		//messagelist.add(msg);
		
		alrmmgr = (AlarmManager)cntxt.getSystemService(Context.ALARM_SERVICE);
    	Intent i=new Intent("MsgTimeoutAlarm");
    	PendingIntent pi=PendingIntent.getBroadcast(cntxt, 0, i, 0);
        alrmmgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60, pi); // Millisec * Second * Minute
		
		IntentFilter intntfltr = new IntentFilter();
		intntfltr.addAction("NewConnection");
		intntfltr.addAction("MessageSent");
		intntfltr.addAction("MessageReceived");
		intntfltr.addAction("OutgoingApplicationMessage");
		intntfltr.addAction("MsgTimeoutAlarm");
		cntxt.registerReceiver(this, intntfltr);
				
	}
	
	//public void setConnectionManager(P2PConnectionManager cm){ connectionmanager = cm;}
	

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		
		String action = arg1.getAction();
		
		if(action.equals("NewConnection")){
			
			try{
	    		Thread.sleep(3000);
	    	}catch (Exception e){
	    			System.out.println(e.toString());
	    	}
			
			String peerMAC = arg1.getStringExtra("peerMAC");
			appendToConsole("RMGR: New connection available - forwarding messages.");
			forwardMesagesToPeer(peerMAC);	
			
		}else if(action.equals("MessageReceived")){
			
			
			P2PMessage msg = arg1.getParcelableExtra("msg");
			//String receivermac = arg1.getParcelableExtra("rMAC");

			appendToConsole("RMGR: received message with uid " + msg.uid);
			messagelist.add(msg);
			adapter.notifyDataSetChanged();
			
			if(msg.destination.equals("ff:ff:ff:ff:ff:ff") || msg.destination.equals(connectionmanager.myMAC)){
				Intent i = new Intent("IncomingApplicationMessage");
				i.putExtra("msg", msg);
				cntxt.sendBroadcast(i);
				if(msg.destination.equals(connectionmanager.myMAC))
						return;
			}
			
			Iterator<P2PConnection> conit = connectionmanager.connections.iterator();
			while(conit.hasNext()){
				P2PConnection con = conit.next();
				//if(con.myMAC.equals(receivermac))
				//	con.startServer();
				//if(con.isConnected && !con.peerMAC.equals(msg.lasthop))
				if(con.status == P2PConnection.CONNECTED && !con.peerMAC.equals(msg.lasthop))
					con.sendMessage(msg);
			}

		}else if(action.equals("OutgoingApplicationMessage")){
			
			appendToConsole("Received outgoing application message.");
			
			String destination = arg1.getStringExtra("destination");
			String body = arg1.getStringExtra("body");
			P2PMessage msg = new P2PMessage(connectionmanager.myMAC,destination,body);
			messagelist.add(msg);
			adapter.notifyDataSetChanged();
			
			Iterator<P2PConnection> conit = connectionmanager.connections.iterator();
			while(conit.hasNext()){
				P2PConnection con = conit.next();
				appendToConsole("Checking if " + con.dev.deviceName + " is connected.");
				//if(con.isConnected)
				if(con.status == P2PConnection.CONNECTED){
					appendToConsole("Forwarding message to " + con.dev.deviceName);
					con.sendMessage(msg);
				}
			}
		}else if(action.equals("MsgTimeoutAlarm")){
			
			appendToConsole("Determining if any messages have timed-out.");

			//go through all messages and check if any have 'timed out'
			Calendar cal = Calendar.getInstance();
			
			Iterator<P2PMessage> msgit = messagelist.iterator();
			while(msgit.hasNext()){
				P2PMessage msg = msgit.next();
				cal.setTime(msg.timestamp);
				cal.add(Calendar.MINUTE, MinutesTillTimeout);
				Date timeout = cal.getTime();
				if(timeout.before(new Date())){
					appendToConsole("Removing message.");
					msgit.remove();
					adapter.notifyDataSetChanged();
				}
			}
		}
		
	}
	
	public void forwardMesagesToPeer(String peerMAC){
		Iterator<P2PMessage> it = messagelist.iterator();
		while(it.hasNext()){
			P2PMessage msg = it.next();
			if(msg.lasthop.equals(peerMAC))
				continue;
			appendToConsole("RMGR: Trying to forward message with uid " + msg.uid + " to "  + peerMAC);
			connectionmanager.sendMessageToPeer(peerMAC,msg);
		}
		
	}
	
	public void appendToConsole(String s){
    	
		console.append(dateformat.format(new Date()) + " " + s + "\n");
		console.post(new Runnable()
		    {
		        public void run()
		        {
		        	 Layout l = console.getLayout();
		        	 if(l == null)
		        		 return;
		        	 final int scrollAmount = l.getLineTop(console.getLineCount())- console.getHeight();
		        	 if(scrollAmount>0)
		        		 console.scrollTo(0, scrollAmount);
		        	 else
		        		 console.scrollTo(0,0);
		        }
		    });
	}

}

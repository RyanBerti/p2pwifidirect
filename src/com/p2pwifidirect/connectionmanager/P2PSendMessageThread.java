package com.p2pwifidirect.connectionmanager;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;

public class P2PSendMessageThread implements Runnable{
	
	Context cntxt;
	Socket mysock;
	OutputStream outs;
	P2PMessage p2pmsg;
	
	byte[] buffer;
	Date date;
    SimpleDateFormat dateFormat;
	
	public P2PSendMessageThread(Context c, Socket s, P2PMessage msg){
		
		cntxt = c;
		mysock = s;
		
		try{
			outs = s.getOutputStream();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		p2pmsg = msg;
		
		dateFormat = new SimpleDateFormat("HH:mm:ss");
    	date = new Date();
		
	}

	public void run() {
		
		try{
		
		for(int i=0;i<P2PMessage.msgarraysize;i++){
			if(P2PMessage.lenarray[i] == -1){ // -1 means we should use the previous value as the length
				if(i==0  || P2PMessage.lenarray[i-1] == -1){
					Log.w("smessage","P2P Message len array not instantiated properly, exiting receive message task.");
					return;
				}

				//Log.w("smessage", "Trying to write string " + i);
				outs.write(p2pmsg.msgarray[i].getBytes(),0,p2pmsg.msgarray[i].length());
				//Log.w("smessage", "Wrote string " + p2pmsg.msgarray[i]);


			}else{

				//Log.w("smessage", "Trying to write int " + i + " (" + p2pmsg.msgarray[i] + ")");
				buffer = ByteBuffer.allocate(P2PMessage.lenarray[i]).putInt(Integer.parseInt(p2pmsg.msgarray[i])).array();
				outs.write(buffer,0,P2PMessage.lenarray[i]);
				//Log.w("smessage", "Wrote int " + p2pmsg.msgarray[i]);

			}
		}
		
		}catch(Exception e){
			Log.w("smessage",e.toString());
			e.printStackTrace();
		}

		
	}

}

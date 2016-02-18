package com.p2pwifidirect.connectionmanager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.text.Layout;
import android.util.Log;
import android.widget.TextView;

public class P2PFileDiscoveryManager extends BroadcastReceiver {
	
	public boolean isFileExist;
	boolean isInExternal, isInInternal;
	TextView console2, console3;
	String state = Environment.getExternalStorageState();
	SimpleDateFormat dateFormat;
	P2PRoutingManager rm;
	P2PMessage msg;
	String myFile;
	Context cntxt;
	ArrayList<P2PFileRequest> reqlist;
	P2PFileRequestAdapter adapter;
	
	public P2PFileDiscoveryManager(Context c, TextView con2, TextView con3){
		cntxt = c;
		console2 = con2;
		console3 = con3;
		IntentFilter it = new IntentFilter();
		it.addAction("IncomingApplicationMessage");
		cntxt.registerReceiver(this, it);
		dateFormat = new SimpleDateFormat("HH:mm:ss");
		reqlist = new ArrayList<P2PFileRequest>();
		adapter = new P2PFileRequestAdapter(cntxt,reqlist);

	}
	
	boolean searchForFile(String filename){
		
		myFile = filename;
		if(checkInternal(myFile)){
		    appendToConsole("Found file " + filename + "on internal storage.");
			return true;
		}
		else if(checkExternal(myFile)){
		    appendToConsole("Found file " + filename + "on external storage.");
	        return true;
	    }
	    
	    appendToConsole("Sending outgoing application message for " + filename);
	    
	    Intent i = new Intent("OutgoingApplicationMessage");
	    i.putExtra("destination","ff:ff:ff:ff:ff:ff");
	    i.putExtra("body", "FILEREQUEST:" + filename);
	    cntxt.sendBroadcast(i);
	    return false;
	}
    
  boolean checkInternal(String fileName){
    File file = cntxt.getFileStreamPath(fileName);
    if (file.exists()){
    	isInInternal = true;
    }
    else{
    	isInInternal = false;
    }
    if (!isInInternal){
    	Log.d("DISCOVERY_ACTIVITY", "File is not in internal");
    }
    return isInInternal;
  }
    
  boolean checkExternal(String fileName) {
	  
	if (!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		return false;

    String sdDir = cntxt.getExternalFilesDir(null).getPath();
    File file = new File(sdDir + File.separator + fileName);
    if (file.exists()) {
    	isInExternal=true;
    }
    else{
    	isInExternal=false;
    }
    if (isInExternal){
    	Log.d("DISCOVERY_ACTIVITY", "file is in the SD");
    }else{
    	Log.d("DISCOVERY_ACTIVITY", "file is not in the SD");
    }
    return isInExternal;
  }

    
  boolean checkFileExist(){
  	if (isInInternal || isInExternal){
  		isFileExist = true;
    	appendToConsole("FMGR: " + myFile + " is found and shared");
    }else {
    	isFileExist = false;
    	appendToConsole("FMGR: Could not find " + myFile + " in the phone");
    }
    return isFileExist;
  }
  
  public void sendResponse(int reqindex){
	  
	  P2PFileRequest req = reqlist.get(reqindex);
	  reqlist.remove(reqindex);
	  adapter.notifyDataSetChanged();
	  
	  appendToConsole("Sending outgoing file sharing response for " + req.requestFile );
	    
	  Intent i = new Intent("OutgoingApplicationMessage");
	  i.putExtra("destination",req.requestSource);
	  i.putExtra("body", "FILERESPONSE");
	  cntxt.sendBroadcast(i);
  }

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		
		String fname;
		String responsedevice;
		
		String action = arg1.getAction();
		
		
		if(action.equals("IncomingApplicationMessage")){
			
			msg = arg1.getParcelableExtra("msg");
			
			if(msg.body.contains("FILEREQUEST")){
				
				appendToConsole("FMGR: Got new file request intent.");
				fname = msg.body.substring(msg.body.indexOf(":")+1);
				P2PFileRequest fr = new P2PFileRequest(fname,msg.source);
				reqlist.add(fr);
				adapter.notifyDataSetChanged();
				
				
			}else if(msg.body.contains("FILERESPONSE")){
				
				appendToConsole("FMGR: Got a file response from " + msg.source);
				//responsedevice = msg.source; // or msg.body.substring(msg.body.indexOf(":")+1);?
				
			}

		}
	}
	
	public void appendToConsole(String s){
	    console3.append(dateFormat.format(new Date()) + " " + s + "\n");
			console3.post(new Runnable()
			    {
			        public void run()
			        {
			        	 Layout l = console3.getLayout();
			        	 if(l == null)
			        		 return;
			        	 final int scrollAmount = l.getLineTop(console3.getLineCount())- console3.getHeight();
			        	 if(scrollAmount>0)
			        		 console3.scrollTo(0, scrollAmount);
			        	 else
			        		 console3.scrollTo(0,0);
			        }
			    });
		}
	
}


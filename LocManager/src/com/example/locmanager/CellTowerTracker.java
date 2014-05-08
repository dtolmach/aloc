package com.example.locmanager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

public class CellTowerTracker extends SensorModality{
 private final Context mContext;
	 
	 private int cid, lac;
	 String jsonString = null;
	 private int sd = 40000;
	    
	 public CellTowerTracker(Context context){
	        mContext = context;
	        cid = -1;
	        lac = -1;
	    }
	    
		public void getLocation() throws InterruptedException, ExecutionException, JSONException
	    {
	        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	        GsmCellLocation loc = (GsmCellLocation)tm.getCellLocation();
//	        List<CellInfo> all = tm.getAllCellInfo();
//	        
//	        if (all.isEmpty())
//	        	Toast.makeText(mContext, "No cell towers detected", Toast.LENGTH_LONG).show();
//	        else {
//	            Toast.makeText(mContext, "found cell towers", Toast.LENGTH_LONG).show();
//	        
//	            StringBuilder sb = new StringBuilder();
//	            for (int i = 0; i< all.size(); i++) {	        	
//	        	
//	            	sb.append(new Integer(i+1).toString() + ".");
//	            	sb.append((all.get(i)).toString());
//	            	sb.append("\n");
//	            }	
//	            Toast.makeText(mContext, sb, Toast.LENGTH_LONG).show();
//	        }
//	        
//	        List<NeighboringCellInfo> neighbors = tm.getNeighboringCellInfo();
//	        if (neighbors.isEmpty())
//	        	Toast.makeText(mContext, "No neighboring cell towers detected", Toast.LENGTH_LONG).show();
//	        else
//	        	Toast.makeText(mContext, "found neighboring cell towers", Toast.LENGTH_LONG).show();
//	        
//	        if (loc != null)
//	            Toast.makeText(mContext, "Location was set to: \n" + loc.getLac() +" id: " + loc.getCid(), Toast.LENGTH_LONG).show();
//	        else 
//	            Toast.makeText(mContext, "Location was set to: null" , Toast.LENGTH_LONG).show();
//	        
	        
	        cid = loc.getCid() & 0xffff;
	        lac = loc.getLac() & 0xffff;
	       
	        //create JSON body for request
    		JSONObject request = new JSONObject();
    		JSONObject cellProps = new JSONObject();
    		JSONArray array = new JSONArray();
    		cellProps.put("lac", lac);
    		cellProps.put("cid", cid);		    				    		
    		array.put(cellProps);
    		request.put("cell", array);
    		
    		jsonString = request.toString();
	    
	    }
	    
	    public int getCid() {
	    	return cid;
	    }
	    
	    public int getLac() {
	    	return lac;
	    } 
	    
	    public String getJsonRequest()
	    {
	    	return jsonString;
	    }

		@Override
		public int getSd() {
			// TODO Auto-generated method stub
			return sd;
		}

		@Override
		public IBinder onBind(Intent intent) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getLatitude() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getLongitude() {
			// TODO Auto-generated method stub
			return 0;
		}
	    
}

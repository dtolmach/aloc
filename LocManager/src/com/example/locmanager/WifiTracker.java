package com.example.locmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WifiTracker extends SensorModality{
	private static final String TAG = "WifiTracker";
    
    private Context mContext;
    protected WifiManager wifiManager;
    private String jsonString = "";
	protected boolean scanCompleted = false;
	private MainActivity activity;
	private BroadcastReceiver mReceiver;
	private int sd = 500;
    
    public WifiTracker(MainActivity activity){
        this.mContext = activity;
		this.activity = activity;
    }
    
    
    public void getLocation(){
        wifiManager = (WifiManager)mContext
                .getSystemService(Context.WIFI_SERVICE);
        
        
		if(wifiManager.isWifiEnabled() == false){
            Toast.makeText(mContext, "wifi is disabled, enabling now...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        } 
       
        Toast.makeText(mContext, "Starting scan \n", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Scan started");
        
        mReceiver = new BroadcastReceiver() {
        	public void onReceive(Context c, Intent intent)
            {
		
                List<ScanResult> wifiList = wifiManager.getScanResults();
                              
                try {
	                //create JSON body for request
	        		JSONObject request = new JSONObject();
	        		JSONArray wifiItems = new JSONArray();
	        		
	        		for (int i = 0; i <wifiList.size(); i++) {
	        			JSONObject wifi = new JSONObject();
	        			
							wifi.put("key", wifiList.get(i).BSSID);
						
//	        			wifi.put("frequency", wifiList.get(i).frequency);
//	        			wifi.put("signal", wifiList.get(i).level);
	        			wifiItems.put(wifi);
	        		}
	        				    				    		
	        		request.put("wifi", wifiItems);
	        		
	        		//Toast.makeText(mContext, jsonString, Toast.LENGTH_LONG).show();
	        		activity.onScanCompleted(request.toString());
	        		
	        		//abort any other broadcast receives
	        		
        		
                } catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        };
        
        mContext.registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        
        wifiManager.startScan();
    }

	public String getJsonString() {
		return jsonString;
	}    
    
	public void onPause()
    {
      mContext.unregisterReceiver(mReceiver);
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

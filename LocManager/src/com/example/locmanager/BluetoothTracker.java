package com.example.locmanager;

import java.util.ArrayList;
import java.util.Set;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BluetoothTracker extends SensorModality{
	private static final String TAG = "BluetoothTracker";
    private Context mContext;
    protected BluetoothAdapter adapter;
    private BroadcastReceiver mReceiver;
    private int sd = 10;
    
    public BluetoothTracker(Context context)
    {
    	this.mContext = context;
    	
    }    
    
    @SuppressLint("NewApi")
	public void getLocation()
    {
    	adapter = BluetoothAdapter.getDefaultAdapter();
    	
    	//set device to descovarable
    	Intent discoverableIntent = new
    	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    	discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
    	mContext.startActivity(discoverableIntent);
    	
    	Toast.makeText(mContext, "Starting scan \n", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Scan started");
        
        mReceiver = new BroadcastReceiver() {
        	
        	public void onReceive(Context c, Intent intent)
            {	
				//scan for paired/bonded devices
		    	Set<BluetoothDevice> devices = adapter.getBondedDevices();
		    	for (BluetoothDevice d : devices){
		    		 Toast.makeText(mContext, d.getName() + " " + d.getAddress() + "\n", Toast.LENGTH_LONG).show();
		    	}
		    
            }
        	
        };
    	
    	mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }
    
    public void onPause()
    {
      mContext.unregisterReceiver(mReceiver);
    }


	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getSd() {
		return sd;
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

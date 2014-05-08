package com.example.locmanager;

import java.util.concurrent.ExecutionException;

import org.json.JSONException;

import android.app.Service;
import android.location.Location;

public abstract class SensorModality extends Service {
	
	int k = MainActivity.k;
	public double posterior[][] = new double[k][k];

	public abstract int getSd();
	public abstract void getLocation() throws JSONException, InterruptedException, ExecutionException;
	public abstract double getLatitude();
	public abstract double getLongitude();
	
	public void setPosterior(double p[][]){
		for (int i=0; i<k; i++){
			for (int j=0; j<k; j++){
				posterior[i][j]=p[i][j];
			}
		}
	}
	
	public double[][] getPosterior()
	{
		return posterior;
	}

}

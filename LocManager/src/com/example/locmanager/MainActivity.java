package com.example.locmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract.Colors;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MainActivity extends Activity {
	
	private static final int GPS = 0;
	private static final int BLUETOOTH = 1;
	private static final int WIFI = 2;
	private static final int CELL = 3;
	private static final int LAT = 0;
	private static final int LNG = 1;
	
	private static final String TAG = "MainActivity";
	TextView mainText;
    WifiManager mainWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    WifiManager wifiManager;
    boolean wifiScan = false;
    boolean wifiFound = false;
    double wifiLat;
    double wifiLong;
    
    double gpsSD = 12;
    double wifiSD = 500;
    double cellSD = 40000;
    double bluetoothSD = 10;
    static int k = 10;
    
    static boolean done = false;
    static double latitude;
    static double longitude;
    
    DistributionView distView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        
        Button gpsbutton = (Button) findViewById(R.id.gpsbutton);
        Button wifibutton = (Button) findViewById(R.id.wifibutton);
        Button cellbutton = (Button) findViewById(R.id.cellbutton);
        Button bluetoothbutton = (Button) findViewById(R.id.bluetooth);
        Button alocbutton = (Button) findViewById(R.id.alocbutton);
        distView = (DistributionView) findViewById(R.id.view);
        
      
        /*****
         * code for handling gps and wifi button clicks
         */
        
        alocbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ArrayList<SensorModality> list = new ArrayList<SensorModality>();
				list.add(new GPSTracker(MainActivity.this));
//				list.add(new WifiTracker(MainActivity.this));
				try {
					aLoc(list, 0.9);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
        gpsbutton.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {             	
                getGPSLoc(new GPSTracker(MainActivity.this));
            }
        });
        
        bluetoothbutton.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                
               getBluetoothLoc();
            }
        });
        
        wifibutton.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
            	//Initialize wifi getLocation 
               getWifiLoc();
            }
        });
        
        cellbutton.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
               getCellLoc();              				
            }			
        });      
    }
    
    private class LocationRequest extends AsyncTask<String, Void, String> {
		
		@Override
		protected String doInBackground(String... params) {
			String result = "";
			try {
				String jsonRequest = params[0];
				URL url = new URL("https://location.services.mozilla.com/v1/search?key=whatever");
				
				final HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
				httpConn.setRequestMethod("POST");
	    		httpConn.setDoOutput(true);
	    		httpConn.setDoInput(true);
	    		httpConn.setRequestProperty("Content-Type", "application/json");
	    		httpConn.connect();
	    		
	    		OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream()); 
	    		out.write(jsonRequest);
	    		out.close();
	    		
	    		StringBuilder jsonString = new StringBuilder();
	            int httpResult = httpConn.getResponseCode();
	            if(httpResult == HttpURLConnection.HTTP_OK) {
	            	 BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getInputStream(),"utf-8"));  
	            	        String line = null;  
	            	        while ((line = br.readLine()) != null) {  
	            	            jsonString.append(line + "\n");  
	            	        }
	            	 result += jsonString.toString();
	            } 
	            
	            
			} catch (IOException e) {
				Log.i("EXCEPTION", "", e);
	    	}
			return result;
			
		}

		@Override
		  protected void onPostExecute(String result) {
		   // TODO Auto-generated method stub
		   super.onPostExecute(result);
		   
		  }
		
		
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    //pick the minimum energy sensor from available list of sensors
    public int getMinE(List<Integer> list)
	{		
		//GPS(1) > Bluetooth(2) > Wifi(3) > Cell(4)
		if (list.contains(CELL))
			return CELL;
		else if (list.contains(WIFI))
			return WIFI;
		else if (list.contains(BLUETOOTH))
			return BLUETOOTH;
		else if (list.contains(GPS))
			return GPS;
		else return -1;
	}
	
    //calculates covariance for column x with itself
	public double calcCovariance(double [] x, double mean) {
		double sum = 0;

		double covSum = 0;
		for (Double d : x)
		{
			covSum += Math.pow(d - mean, 2.0);
		}
		
		double cov = (covSum / (k - 1)) ;
		return cov;
	}
	
	public ArrayList<Double> getGPSLoc(GPSTracker gps)
	{
        if (gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
         // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();  
            ArrayList<Double> loc = new ArrayList<Double>();
            loc.add(latitude);
            loc.add(longitude);
            return loc;
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
		return null;
	}
	
	public ArrayList<Double> getBluetoothLoc()
	{
		BluetoothTracker bt = new BluetoothTracker(MainActivity.this);
        bt.getLocation();
		return null;
	}
	
	public ArrayList<Double> getWifiLoc()
	{		
        //Initialize wifi getLocation 
        WifiTracker wifi = new WifiTracker(MainActivity.this);
        wifi.getLocation();
//        while(wifiScan == false)
//        {
//        	continue;
//        }
        
//        if (wifiFound == true) {
//        	ArrayList<Double> loc = new ArrayList<Double>();
//            loc.add(wifiLat);
//            loc.add(wifiLong);
//            wifiScan = false;
//            wifiFound = false;
//            return loc;
//        }
      
		return null;
	}
	
	public ArrayList<Double> getCellLoc()
	{
		CellTowerTracker cell = new CellTowerTracker(MainActivity.this);
        try {
			cell.getLocation();
			String jsonRequest = cell.getJsonRequest();
			
			LocationRequest lr = new LocationRequest();
	        String result = lr.execute(jsonRequest).get();
	        
	        //wait for the request task to complete
//	        while (lr.getStatus() != AsyncTask.Status.FINISHED) 
//	        	continue;
	       	Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//	        String result =  lr.get();
	        if(!result.equals(null)) {
	        	JSONObject jsonResponse = new JSONObject(result);
           	 	if(jsonResponse.getString("status").equals("ok")) {
           	 		double latitude = Double.parseDouble(jsonResponse.getString("lat"));
           	 		double longitude = Double.parseDouble(jsonResponse.getString("lon"));
           	 		Toast.makeText(getApplicationContext(), "Your Location using cell is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
           	 	}
           	 } else {
	        	Toast.makeText(getApplicationContext(), "Request location returned with error code " , Toast.LENGTH_LONG).show();
           	 }       
	        
			
            
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<Double> getLoc(SensorModality s) throws JSONException, InterruptedException, ExecutionException
	{
		ArrayList<Double> loc = new ArrayList<Double>();
		if (s instanceof GPSTracker)
		{
			 GPSTracker gps = new GPSTracker(MainActivity.this);
			 if (gps.canGetLocation()){
				 loc.add(s.getLatitude());
			     loc.add(s.getLongitude());
			 }
		}
		if (s instanceof WifiTracker)
		{
			
			GPSTracker gps = new GPSTracker(MainActivity.this);
			 if (gps.canGetLocation()){
				 loc.add(s.getLatitude());
			     loc.add(s.getLongitude());
			 }
//			WifiTracker wifi = new WifiTracker(MainActivity.this);
//			wifi.getLocation();
//			Thread.sleep(10000);;
////			int timer = 0;
////			while(done == false && timer < 10) {
////				try{
////					Thread.sleep(1000);
////					timer ++;
////					
////				} catch (InterruptedException ex){};
////			}
//			
//			loc.add(latitude);
//			loc.add(longitude);
		}
		return loc;
		
	}
	
	/**
	 * calculates distance in meters between two points in latitude and longitude
	 * 
	 * @param lat1 in degrees
	 * @param lng1 in degrees
	 * @param lat2 in degrees
	 * @param lng2 in degrees
	 * @return distance in meters
	 */
	public static double calcDistMeters(double lat1, double lng1, double lat2, double lng2) 
	{
	    double earthRadius = 6371000;  //earth radius in meters
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;
	    
	    return dist;
	}
	
	
	/**
	 * Calculates bearing and distance between initial(1) and final(2) geolocations
	 * 
	 * @param lat1 in degrees
	 * @param lng1 in degrees
	 * @param lat2 in degrees
	 * @param lng2 in degrees
	 * @return distance in meters and bearing angle in radians
	 */
	public double[] calcDistBrng(double lat1, double lng1, double lat2, double lng2)
	{
		//angle traveling
		//θ = atan2(sin(Δlong)*cos(lat2), cos(lat1)*sin(lat2) − sin(lat1)*cos(lat2)*cos(Δlong))
		double lat1rad = Math.toRadians(lat1);
		double lng1rad = Math.toRadians(lng1);
		double lat2rad = Math.toRadians(lat2);
		double lng2rad = Math.toRadians(lng2);
		
		double x = Math.sin(lng2rad - lng1rad) * Math.cos(lat2rad);
		double y = Math.cos(lat1rad) * Math.sin(lat2rad) - Math.sin(lat1rad) * Math.cos(lat2rad) * Math.cos(lng2rad - lng1rad);
		double brng = Math.atan2(y,x);
		
		//calculate distance traveled
		double dist = calcDistMeters(lat1,lng1, lat2, lng2);
				
		double[] result = new double[2];
		
		result[0] = dist; //in meters
		result[1] = brng; // in radians
		return result;
	}
	
	/**
	 * Calculates new geolocation from initial geolocation, bearing, and distance
	 * 
	 * @param lat in degrees
	 * @param lng in degrees
	 * @param dist in meters
	 * @param brng in radians
	 * @return new latitude and longitude in degrees
	 */
	public double[] calcLatLong(double lat, double lng, double dist, double brng)
	{
		
		double latrad = Math.toRadians(lat);
		double lngrad = Math.toRadians(lng);
		double angdist = dist / 6371000.0;
		
		double newLat = Math.asin( Math.sin(latrad)*Math.cos(angdist) + Math.cos(latrad)
				*Math.sin(angdist)*Math.cos(brng));
		
		double a = Math.atan2(Math.sin(brng)*Math.sin(angdist)*Math.cos(latrad),
				Math.cos(angdist)-Math.sin(latrad)*Math.sin(newLat));
		
		double newLong = lngrad + a;
		double newLongDeg = Math.toDegrees((newLong+ 3*Math.PI) % (2*Math.PI) - Math.PI);
		
		double result[] = new double[2];
		result[0] = Math.toDegrees(newLat);
		result[1] = newLongDeg;
		return result;	
		
	}

	
	/**
	 * Calculates Gaussian distribution 
	 * @param sd
	 * @return
	 */
	public double[][] calcGaussianDistribution(double sd, int x0,  int y0) 
	{
		double result[][] = new double[k][k];
		for (int i=0; i<k; i++){
			for (int j=0; j<k; j++){
				result[i][j] = (1/(sd * sd * (2 * Math.PI))) * 
    					Math.exp(-1 * (Math.pow(i - x0, 2) + Math.pow(j-y0, 2)) / (2 * sd * sd));
			}
		}
		return result;
	}
	
	public double[][] getSensorModel(SensorModality s)
	{		
		double sd = s.getSd();
		int x0 = k/2;
		int y0 = k/2;
//		double sd = 2;
		
		double sensorModel[][] = calcGaussianDistribution(sd, x0, y0);
//		for (int i=0; i<k; i++){
//			for (int j=0; j<k; j++){
//				sensorModel[i][j] = (1/(sd * sd * (2 * Math.PI))) * 
//    					Math.exp(-1 * (Math.pow(i - x0, 2) + Math.pow(j-y0, 2)) / (2 * sd * sd));
//			}
//		}
		
		return sensorModel;
	}
	
	public double[][] multDistributions(double[][] p, double[][] s){
		double result[][] = new double[k][k];
		
//		int i=0,j=0,l=0,m=0, sx=0, sy=0;
		
		for(int i = 0; i<k; i++) {
			Arrays.fill(result[i], 0.0);
		}
//		try{
		
			for(int i=0; i<k; i++) {
				for(int j=0; j<k; j++) {
					if(p[i][j] > 0) {
						int sx = 0;					
						for(int l=i-(k/2); l<i+(k/2); l++) {						
							if(l>=0 && l<k){
								int sy = 0;
								for(int m=j-(k/2); m<j+(k/2); m++) {
									if (m>=0 && m<k){
										Log.i("ALOC", "result[ " + l+ "][: " +m +"]: " + result[l][m]);
										result[l][m] += p[i][j] * s[sx][sy];
										
									}										
									sy++;
								}
							}
							sx++;
						}
					}
				}
			}
		
//		} catch (ArrayIndexOutOfBoundsException e) {
//			Log.i("ALOC", "i: " + i+ " j: " +j +" l: " +l+ " m: "+m+ " sx: " + sx + " sy: " + sy);
//		}
		return result;
	}
	
	public void updatePrediction(double[][] p, double[][] g)
	{
		for (int i=0; i<k; i++){
			for (int j=0; j<k; j++){
				p[i][j] = g[i][j];
			}
		}
	}
	
	public void updatePrior(double[][] p, double[][]posterior)
	{
		
		for (int i=0; i<k; i++){
			for (int j=0; j<k; j++){
				p[i][j] = posterior[i][j];
			}
		}
	}
	
	public void writeToFile(String filename, double d[][]) throws IOException
	{
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

		if (!file.exists()) {
		   file.createNewFile();
		}
		//FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);
		FileWriter writer = new FileWriter(file);
		for (int i=0; i<k; i++){
			for (int j=0; j<k; j++) {
				 writer.write(String.valueOf(d[i][j]) + " ");
				 writer.flush();
			}
		}

		writer.close();
	}
	
	public void aLoc(List<SensorModality> sensors, double d) throws JSONException, InterruptedException, ExecutionException, IOException {
		
//		GPSTracker gps = new GPSTracker(this);
//		gps.getLocation();
//		double lat = gps.getLatitude();
//		double lng = gps.getLongitude();
//		double[] initLoc = {lat,lng};
//		int gridx = k/2;
//		int gridy = k/2;
		
		double maxbrng = Math.PI / 4; //45 degrees
		double maxdist = 5; //10 meters
		
		List<double[]> measured = new ArrayList<double[]>();		
//		double[] gridPredicted = new double[2];
//		gridPredicted[0] = k/2;
//		gridPredicted[1] = k/2;
		double[] predicted = new double[2];
		
//		measured.add(initLoc);
		
		//set prediction and prior distributions to uniform distributions
		double prediction[][] = new double[k][k];
		double prior[][] = new double[k][k];
		for (int i=0; i<k; i++) {
			Arrays.fill(prediction[i], 1/Math.pow(k,2));
			Arrays.fill(prior[i], 1/Math.pow(k,2));
		}	
	
		int limit = 2;
		int t = 0;	
		while (t < limit){			
			List<Integer> passed = new ArrayList<Integer>();
			for (int s=0; s<sensors.size(); s++) {		
				int sd = sensors.get(s).getSd();
				double sensorm[][] = getSensorModel(sensors.get(s)); 
				writeToFile("sensorModel.txt", sensorm);	
				writeToFile("initPrior.txt", prior);
				double posteriorSums[] = new double[k];
				
				double pHat[][] = sensorm;
				double posterior[][] = sensorm;
				
				if (t > 0) { // when the prior and predicted are not uniform 
					pHat = multDistributions(prediction, sensorm); // calculatePHat(prediction, sensorModel);
					writeToFile("pHat.txt", pHat);
				
					posterior = multDistributions(prior, sensorm); // calculatePosterior(prior, sensorModel);
					writeToFile("posterior.txt", posterior);
				}
				
				int lowx = k;
				int highx = 0;			
				
				Arrays.fill(posteriorSums, 0.0);
				for(int i=0; i<k; i++) {					
					for(int j=0; j<k; j++) {
						posteriorSums[i] += posterior[i][j];
					}
					
					if (posteriorSums[i] >= 0.001 && i<lowx)
						lowx = i;
					
					if (posteriorSums[i] < 0.001 && i>highx)
						highx = i;
					
				}
				
				if(lowx == k) {
					lowx = 0;
				}
				
				if(highx == 0) {
					highx = k;
				}
				
//				sensors.get(s).setPosterior(posterior);
				
				double err = 0;
				if (t > 0) {
					for (int z1 = 0; z1<k; z1++) {
						for (int z2 = 0; z2<k; z2++) {
							double trace = 0;
							if (pHat[z1][z2] > 0) {
								Log.i("ALOC", "calculating trace i: " + z1 + " j: " + z2);
								for (int i=lowx; i<highx; i++) {
									double variance[] = posterior[i];
									double variance_mean = posteriorSums[i]/k;
									double cov = calcCovariance(variance, variance_mean);
									trace += cov;						
								}
								err += pHat[z1][z2] * trace;
							}
						}
					}
				} else {
					err = pHat[0][0] * k * posterior[0][0] * k;
				}
				
				Log.i("ALOC", "err: " + err);
				
				//if the sensor passed accuracy add that sensor
				if (err <= Math.pow(d, 2.0)) {
					passed.add(s);
				}
			}								

			//get the passed sensor with least energy usage
			int s = getMinE(passed);
			if (s == -1)
			{
				Log.i("ALOC", "no sensors were selected ");
				break;
			}
			
			//obtain zi(t)
			ArrayList<Double> measurment = getLoc(sensors.get(0));
			
			double sensorModel[][] = getSensorModel(sensors.get(s)); 
			writeToFile("newSensor.txt", sensorModel);
			
			double prior2[][] = new double[k][k];
			for(int q=0; q<k; q++){
				Arrays.fill(prior2[q], 0.0);
			}
			prior2[k/2][k/2] = 1.0;
			writeToFile("prior2.txt", prior2);
			double new_posterior[][] =  multDistributions(prior2, getSensorModel(sensors.get(s)));			
			writeToFile("newPosterior.txt", new_posterior);
			
			updatePrior(prior, new_posterior);
			
			double[] xHat = {0.0,0.0};
			for (int i=0; i<k; i++){
				for (int j=0; j<k; j++){
					xHat[0] += i * new_posterior[i][j];
					xHat[1] += j * new_posterior[i][j];
				}
			}
			
			double angle = Math.atan((xHat[1] - k/2)/(xHat[0] - k/2));
			double dst = (xHat[0] - k/2) / Math.cos(Math.toDegrees(angle));
			double xHatGeo[] = calcLatLong(measurment.get(0), measurment.get(1), angle, dst);
			measured.add(xHatGeo);
			
			t += 1;
			
			if (t > 1) {
				int size = measured.size();
				double diff = calcDistMeters(measured.get(t-1)[0], measured.get(t-1)[1], predicted[0], predicted[1]);
				double[] distAndBrng = calcDistBrng(measured.get(size-2)[0], measured.get(size-2)[1], measured.get(size-1)[0], measured.get(size-1)[1]);
				double[] nextPrediction = calcLatLong(measured.get(size-1)[0], measured.get(size-1)[1], distAndBrng[0], distAndBrng[1]);
				predicted[0] = nextPrediction[0];
				predicted[1] = nextPrediction[1];
				
				//get predicted location on a grid from our measured x and y
				double xPredicted = Math.floor((k/2) + Math.cos(distAndBrng[1]) * distAndBrng[0]);
				double yPredicted = Math.floor((k/2) + Math.sin(distAndBrng[1]) * distAndBrng[0]);
				
				updatePrediction(prediction, calcGaussianDistribution(diff, (int)xPredicted, (int)yPredicted));
				writeToFile("newPrediction.txt", prediction);
				
			} else {
				int size = measured.size();
				double[] nextPrediction = calcLatLong(measured.get(size-1)[0], measured.get(size-1)[1], maxdist, maxbrng);
				predicted[0] = nextPrediction[0];
				predicted[1] = nextPrediction[1];
				//get predicted location on a grid from our measured x and y
				double xPredicted = Math.floor((k/2) + Math.cos(maxbrng) * maxdist);
				double yPredicted = Math.floor((k/2) + Math.sin(maxbrng) * maxdist);
				
				Log.i("ALOC", "xPredicted = " + xPredicted + " yPredicted= " + yPredicted);
				
				for (int i=0; i<k; i++) {
					Arrays.fill(prediction[i], 0);
				}
				prediction[(int) xPredicted][(int) yPredicted] = 1;
				writeToFile("newPrediction.txt", prediction);
				
			}
					
		}
	}
	
	//used when wifi scan completes
	public void onScanCompleted(String jsonRequest) {
		Log.i(TAG, "scan completed " + jsonRequest);
		 try {
             LocationRequest lr = new LocationRequest();
		        String result = lr.execute(jsonRequest).get();
		        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
		        //wifiScan = true;
		        if(!result.equals(null)) {
		        	JSONObject jsonResponse = new JSONObject(result);
	           	 	if(jsonResponse.getString("status").equals("ok")) {
	           	 		//wifiFound = true;
	           	 		latitude = Double.parseDouble(jsonResponse.getString("lat"));
	           	 		longitude = Double.parseDouble(jsonResponse.getString("lon"));
	           	 		done = true;
//	           	 		Toast.makeText(getApplicationContext(), "Your Location using wifi is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
	           	 	}
	           	 } else {
		        	Toast.makeText(getApplicationContext(), "Request location returned with error code " , Toast.LENGTH_LONG).show();
	           	 }   
         } catch (JSONException e) {
         	Log.i("EXCEPTION", "", e);
         } catch (InterruptedException e) {
				// TODO Auto-generated catch block
        	 Log.i("EXCEPTION", "", e);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				Log.i("EXCEPTION", "", e);
			}
	}

}

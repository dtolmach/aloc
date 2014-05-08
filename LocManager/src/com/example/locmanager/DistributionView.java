package com.example.locmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.StringTokenizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DistributionView extends View{
	int k = 10;
	double dist[][] = new double[k][k];
	double max = 0;
	
	
	public DistributionView(Context cxt, AttributeSet attrs) throws IOException {
        super(cxt, attrs);
        setMinimumHeight(100);
        setMinimumWidth(100);
        
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "newPrediction.txt");
        FileReader reader = new FileReader(file);
        BufferedReader buff = new BufferedReader(reader);
        String s = buff.readLine();
//        Log.i.("DISTRIBUTION", s);
        double d[][] = new double[k][k];
        StringTokenizer st = new StringTokenizer(s);
//        Log.i("DISTRIBUTION","" + st.countTokens());
        
        for(int i=0; i<k; i++) {
        	for(int j=k-1; j>=0; j--) {        		
        		d[i][j] = Double.parseDouble(st.nextToken());
        	}
        }
        setDist(d);       
        
        //OutputStreamWriter out = new OutputStreamWriter(openFileOutput("myaddress.txt",0));

//    	int sd = 2;
//    	int x0 = (int) Math.floor(k/2);
//    	int y0 = (int) Math.floor(k/2);
//    	double sum = 0;	
//    	
//    	for (int i=0; i<k; i++){
//    		for (int j=0; j<k; j++){
//    			
//    			dist[i][j] = (1/(sd * sd * (2 * Math.PI))) * 
//    					Math.exp(-1 * (Math.pow(i - x0, 2) + Math.pow(j-y0, 2)) / (2 * sd * sd));
//    			sum += dist[i][j];
//    			if (dist[i][j] > max) max = dist[i][j];
//    			
////    			dist[i][j] = 1.0 / (Math.pow(sd, 2) * Math.sqrt(2*Math.PI))*Math.exp( -1 *
////    					( (Math.pow(i-x0, 2) / (2*Math.pow(sd,2) ) ) )
////    							+( (Math.pow(j-y0, 2) / (2*Math.pow(sd,2) ) ) ) ) ;
//    		}
//    		Log.i("DISTRIBUTION", Arrays.toString(dist[i]) + " ");
//    		
//    		
//    	}
//    	Log.i("DISTRIBUTION", "sum : " + sum);
    	
    }
	
	
	public void setDist(double d[][])
	{
		max = 0;
		for (int i=0; i<k; i++){
			for (int j=0; j<k; j++){				
				dist[i][j] = d[i][j];
				Log.i("DISTRIBUTION", "result[ " + i+ "][" +j +"]: " + d[i][j]);
				if (dist[i][j] > max) max = dist[i][j];
			}
		}
//		invalidate();
	}

    @Override
    protected void onDraw(Canvas cv) {		
		int width = cv.getWidth() / k;
		int height = cv.getHeight() / k;
		
		cv.drawColor(Color.BLACK);
		
		for (int x=0; x<k; x++){
			for (int y=0; y<k; y++){
				int gray = (int) Math.floor(dist[x][y] / max * 255);
				Paint color = new Paint();
				color.setARGB(255, gray, gray, gray);
				cv.drawRect(x*width-width/2, y*height-height/2,
						 x*width + width/2, y*height + height/2, color);
				 
			}
		}
    }
    
    
}

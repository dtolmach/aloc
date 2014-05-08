package com.example.locmanager;

public interface AsyncResponse {
	void processFinish(String output);
}


//File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "pHat.txt");
//FileReader reader = new FileReader(file);
//BufferedReader buff = new BufferedReader(reader);
//String s = buff.readLine();
//Log.i("DISTRIBUTION", s);
//double d[][] = new double[k][k];
//StringTokenizer st = new StringTokenizer(s);
//Log.i("DISTRIBUTION","" + st.countTokens());
//
//for(int i=0; i<k; i++) {
//	for(int j=0; j<k; j++) {        		
//		d[i][j] = Double.parseDouble(st.nextToken());
//	}
//}
//setDist(d);  
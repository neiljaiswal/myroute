package com.example.myroute;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private GoogleMap map;
	private Marker userMarker;
	public LocationManager locMan;
	GPSTracker gps;
	double latitude;
	double longitude;
	TextView resultView;
	private Spinner routes;
	private Button btnSubmit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StrictMode.enableDefaults();
		
		resultView = (TextView) findViewById(R.id.result);
		
		getLocation();
		getData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void getLocation(){
		if(map==null){
			//get the map
	    	map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			//check in case map/ Google Play services not available
			if(map!=null){ 
				
			    final Handler handler = new Handler(); 
			    Timer timer2 = new Timer(); 
			    TimerTask timertask = new TimerTask() {
			        public void run() { 
			            handler.post(new Runnable() {
			                public void run() {
			                	
			                	//Remove the user everytime it updates
			                	if(userMarker != null) {
			        	    		userMarker.remove();
			        	    	}
			                	
			                	 locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
								//*************get last location*************//
								gps = new GPSTracker(MainActivity.this);
					          // check if GPS enabled    
								if(gps.canGetLocation()){
					               
									latitude = gps.getLatitude();
									longitude = gps.getLongitude();
								}else{
									// can't get location
									// GPS or Network is not enabled
									// Ask user to enable GPS/network in settings
									gps.showSettingsAlert();
								}
					            //******************************************//
								LatLng lastLatLng = new LatLng(latitude, longitude);
								//resultView.setText(latitude + " and " + longitude);
								userMarker = map.addMarker(new MarkerOptions()
							        .position(lastLatLng)
							        .title("You're here now!")
							        .snippet("Follow the route ;)")
							        .icon(BitmapDescriptorFactory
							        .fromResource(R.drawable.ic_launcher)));

								    // Move the camera instantly to hamburg with a zoom of 15.
								    map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 15));
			                }
			            });
			        }
			    };
			    timer2.schedule(timertask, 3000, 3000); // Update every 3s!
			}
	    }
	}
	
	public void getData(){
    	String result = "";
    	resultView.setText("go!");
    	InputStream isr = null;
    	try {
	        HttpClient client = new DefaultHttpClient();  
	        HttpGet get = new HttpGet("http://192.168.0.207/TrackMyRoute/api/router/routes");
	        HttpResponse responseGet = client.execute(get);  
	        HttpEntity resEntityGet = responseGet.getEntity();  
	        if (resEntityGet != null) {  
	           //do something with the response
	           resultView.setText("fail");
	           isr = resEntityGet.getContent();
	        }
		} catch (Exception e) {
		    e.printStackTrace();
		    resultView.setText("fail at get");
		}
    //convert response to string
    try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(isr,"UTF-8"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
            }
            isr.close();
     
            result=sb.toString();
            resultView.setText("string: " + result);
    }
    catch(Exception e){
            Log.e("log_tag", "Error  converting result "+e.toString());
            //resultVieww.setText("fail at sb");
    }
     
    //parse json data
    try {
    	   
    	   JSONArray arr = new JSONArray(result);
    	   JSONObject jObj = arr.getJSONObject(0);
    	   routes = (Spinner) findViewById(R.id.spinner2);
    	   List<String> list = new ArrayList<String>();
    	   String data = "";
    	   int n = arr.length();
           for (int i = 0; i < n; i++) {
               // GET INDIVIDUAL JSON OBJECT FROM JSON ARRAY
               JSONObject jo = arr.getJSONObject(i);
                
               data = data + " " + jo.getString("route_name");
                
               list.add(jo.getString("route_name"));
           }
           ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
        	android.R.layout.simple_spinner_item, list);
           dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	routes.setAdapter(dataAdapter);
        	
        	resultView.setText("\n" + data);
    	   
    	  } catch (JSONException e) {
    	   // TODO Auto-generated catch block
    	   e.printStackTrace();
    	   //resultVieww.setText("fail at json");
    	   
    	  }
    
    }
	
	
	public void selectRoute(View view) {
		 
		routes = (Spinner) findViewById(R.id.spinner2);
		resultView.setText(routes.getSelectedItem().toString());
 
		
	}
}
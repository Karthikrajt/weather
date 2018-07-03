package report.weather.karthikraj.weather;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends AppCompatActivity  implements ConnectivityReceiver.ConnectivityReceiverListener {

    // json object response url
    private String url_current_weather = "http://api.openweathermap.org/data/2.5/group?id=1264527,1275339,1277333,1261481&appid=91bd4f6284d00a43726dddae59c899db&units=";


    private static String TAG = HomeScreen.class.getSimpleName();

    DatabaseHelper myDb;

    // Progress dialog
    private ProgressDialog pDialog;

    private RecyclerView current_weather_view;

    private weather_adapter adapter;
    private List<city_weather_details> w_List;




    // temporary string to show the parsed response
    private String jsonResponse;
    final Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myDb = new DatabaseHelper(this);

        Cursor res = myDb.getAllData();
        if (res.getCount() == 0) {
            myDb.insertData("Celsius");
        }

        checkConnection();
        current_weather_view = (RecyclerView) findViewById(R.id.recycler_view);

        w_List = new ArrayList<>();
        adapter = new weather_adapter(this, w_List);


        GPSTracker gpsTracker = new GPSTracker(this);

        if (gpsTracker.getIsGPSTrackingEnabled())
        {
            String stringLatitude = String.valueOf(gpsTracker.latitude);
            TextView fieldLatitude = (TextView)findViewById(R.id.fieldLatitude);
             fieldLatitude.setText("Currrent Latitude: "+stringLatitude);

            String stringLongitude = String.valueOf(gpsTracker.longitude);
            TextView fieldLongitude = (TextView)findViewById(R.id.fieldLongitude);
            fieldLongitude.setText("Currrent Longitude: "+stringLongitude);

        }
        else
        {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }


        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        current_weather_view.setLayoutManager(mLayoutManager);
        current_weather_view.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        current_weather_view.setItemAnimator(new DefaultItemAnimator());
        current_weather_view.setAdapter(adapter);

      //

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if(ConnectivityReceiver.isConnected()) {
                    get_current_weather(url_current_weather + get_unit());
                }
                else
                {
                    checkConnection();
                }

            }
        });

        if(ConnectivityReceiver.isConnected()) {
            get_current_weather(url_current_weather + get_unit());
        }
    }

    private void prepare_weather_list(int city_id,
                                      String city_name,
                                      double temp,
                                      double temp_min,
                                      double temp_max,
                                      double pressure,
                                      double humidity,
                                      double wind_speed,
                                      double wind_deg,
                                      String weather_main,
                                      String weather_description,
                                      String icon)
    {
        city_weather_details c_w_d = new city_weather_details(city_id,city_name,temp,temp_min,temp_max,pressure,humidity,wind_speed,wind_deg,weather_main,weather_description,icon);
        w_List.add(c_w_d);

        adapter.notifyDataSetChanged();
    }




    public String current_weather_url()
    {
        return url_current_weather+get_unit();
    }



    public String get_unit()
    {
        Cursor res = myDb.getAllData();
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append(res.getString(1));
        }
        if (buffer.toString().trim().equals("Kelvin")) {
            return "kelvin";
        } else if (buffer.toString().trim().equals("Celsius")) {
            return "metric";
        } else {
            return "imperial";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Cursor res = myDb.getAllData();
            StringBuffer buffer = new StringBuffer();
            while (res.moveToNext()) {
                buffer.append(res.getString(1));
            }
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.setting_layout);

            final Spinner spin_unit = (Spinner) dialog.findViewById(R.id.spin_unit);

            try {
                if (buffer.toString().trim().equals("Kelvin")) {
                    spin_unit.setSelection(0);
                } else if (buffer.toString().trim().equals("Celsius")) {
                    spin_unit.setSelection(1);
                } else {
                    spin_unit.setSelection(2);
                }
            } catch (Exception e) {
                Log.d("error", e.toString());
            }

            Button Save_unit = (Button) dialog.findViewById(R.id.tem_save);
            // if button is clicked, close the custom dialog
            Save_unit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean isUpdate = myDb.updateData("1", spin_unit.getSelectedItem().toString());
                    if (isUpdate == true)
                        Toast.makeText(HomeScreen.this, "Unit value updated.", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(HomeScreen.this, "Unit value not updated.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });

            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    private void get_current_weather(String url) {
        showpDialog();
        w_List.clear();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
                url, null, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                      JSONArray weather_jsonArray = new JSONArray(response.getString("list"));
                       for(int i=0; i< weather_jsonArray.length();i++)
                       {
                            JSONObject explrObject = weather_jsonArray.getJSONObject(i);
                            JSONArray weather_list = new JSONArray(explrObject.getString("weather"));
                            JSONObject main_list = new JSONObject(explrObject.getString("main"));
                            JSONObject wind_list = new JSONObject(explrObject.getString("wind"));

                            Double de_list = 0.0;



                            prepare_weather_list(explrObject.getInt("id"),explrObject.getString("name"),main_list.getDouble("temp"),main_list.getDouble("temp_min"),main_list.getDouble("temp_max"),main_list.getDouble("pressure"),main_list.getDouble("humidity"),wind_list.getDouble("speed"),de_list,weather_list.getJSONObject(0).getString("main"),weather_list.getJSONObject(0).getString("description"),weather_list.getJSONObject(0).getString("icon"));
                       }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               // VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                Log.d("Error_volley", error.getMessage());

                hidepDialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);


    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        AppController.getInstance().setConnectivityListener(this);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */

    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    public String wind_direction(double deg){
        if (deg>11.25 && deg<33.75){
            return "NNE";
        }else if (deg>33.75 && deg<56.25){
            return "ENE";
        }else if (deg>56.25 && deg<78.75){
            return "E";
        }else if (deg>78.75 && deg<101.25){
            return "ESE";
        }else if (deg>101.25 && deg<123.75){
            return "ESE";
        }else if (deg>123.75 && deg<146.25){
            return "SE";
        }else if (deg>146.25 && deg<168.75){
            return "SSE";
        }else if (deg>168.75 && deg<191.25){
            return "S";
        }else if (deg>191.25 && deg<213.75){
            return "SSW";
        }else if (deg>213.75 && deg<236.25){
            return "SW";
        }else if (deg>236.25 && deg<258.75){
            return "WSW";
        }else if (deg>258.75 && deg<281.25){
            return "W";
        }else if (deg>281.25 && deg<303.75){
            return "WNW";
        }else if (deg>303.75 && deg<326.25){
            return "NW";
        }else if (deg>326.25 && deg<348.75){
            return "NNW";
        }else{
            return "N";
        }
    }
}
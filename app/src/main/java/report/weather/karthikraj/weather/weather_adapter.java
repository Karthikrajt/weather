package report.weather.karthikraj.weather;

/**
 * Created by KarthikT on 7/2/2018.
 */
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class weather_adapter extends RecyclerView.Adapter<weather_adapter.MyViewHolder> {

    private Context mContext;
    private List<city_weather_details> weather_list;
    public int menu_postion;

    private ProgressDialog pDialog;

    private String url_current_weather = "http://api.openweathermap.org/data/2.5/forecast?appid=91bd4f6284d00a43726dddae59c899db&units=";

     Dialog dialog;

    DatabaseHelper myDb;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count,temp,min_temp,max_temp;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);

            temp = (TextView) view.findViewById(R.id.tem);
            min_temp = (TextView) view.findViewById(R.id.min_temp);
            max_temp = (TextView) view.findViewById(R.id.max_temp);


            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }


    public weather_adapter(Context mContext, List<city_weather_details> weather_list) {
        this.mContext = mContext;
        this.weather_list = weather_list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_info, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        myDb = new DatabaseHelper(mContext);

        pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);


        menu_postion = position;
        final city_weather_details list = weather_list.get(position);
        holder.title.setText(list.getCity_name());
        holder.count.setText(list.getWeather_description());

        holder.temp.setVisibility(View.GONE);

        holder.temp.setText(""+list.getTemp()+symbol());

        holder.min_temp.setText(""+list.getTemp_min()+symbol());

        holder.max_temp.setText(""+list.getTemp_max()+symbol());

        Glide.with(mContext).load("http://openweathermap.org/img/w/"+list.getIcon()+".png").into(holder.thumbnail);

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // showPopupMenu(holder.overflow);

                PopupMenu popup = new PopupMenu(mContext, holder.overflow);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_weather, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_add_favourite:





                                 final Dialog dialog = new Dialog(mContext,android.R.style.Theme_DeviceDefault_NoActionBar);
                                dialog.setContentView(R.layout.activity_weather_detailed);

                                get_current_weather(url_current_weather+get_unit()+"&id="+list.getCity_id(),dialog);

                                final TextView wea_desc = (TextView) dialog.findViewById(R.id.wea_desc);

                                final TextView wea_temp = (TextView) dialog.findViewById(R.id.wea_temp);

                                final TextView min_temp = (TextView) dialog.findViewById(R.id.min_temp);

                                final TextView max_temp = (TextView) dialog.findViewById(R.id.max_temp);

                                final TextView wea_humidity = (TextView) dialog.findViewById(R.id.wea_humidity);

                                final TextView wea_pres = (TextView) dialog.findViewById(R.id.wea_pres);

                              final TextView wea_wind = (TextView) dialog.findViewById(R.id.wea_wind);

                                final TextView wea_deg = (TextView) dialog.findViewById(R.id.wea_deg);

                                wea_desc.setText(""+list.getWeather_description());

                                wea_temp.setText(""+list.getTemp()+symbol());

                                min_temp.setText(""+list.getTemp_min()+symbol());
                                max_temp.setText(""+list.getTemp_max()+symbol());
                                wea_humidity.setText(""+list.getHumidity()+"%");
                                wea_pres.setText(""+list.getPressure());

                                wea_wind.setText(""+list.getWind_speed()+" Km/h");

                                wea_deg.setText(""+list.getWind_deg());

                                Button wea_close = (Button) dialog.findViewById(R.id.wea_close);
                                // if button is clicked, close the custom dialog
                                wea_close.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {


                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();

                                return true;
                            case R.id.action_play_next:

                                String message = "City: "+list.getCity_name()+"\n"+"Temperature:"+list.getTemp()+symbol()+"\n"+"Min Temperature: "+list.getTemp_min()+symbol()+"\n"+"Max Temperature: "+list.getTemp_max()+symbol()+"\n"+" Description: "+list.getWeather_description();

                                share_info(message);

                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popup.show();

           }
        });
    }


    private void get_current_weather(String url, final Dialog Dialog) {
        showpDialog();
         JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                 url, null, new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {

                try {

                     TextView dwr_date1 = (TextView) Dialog.findViewById(R.id.dwr_date1);

                     TextView dwr_temp1 = (TextView) Dialog.findViewById(R.id.dwr_temp1);

                     TextView dwr_date2 = (TextView) Dialog.findViewById(R.id.dwr_date2);

                     TextView dwr_temp2 = (TextView) Dialog.findViewById(R.id.dwr_temp2);

                     TextView dwr_date3 = (TextView) Dialog.findViewById(R.id.dwr_date3);

                     TextView dwr_temp3 = (TextView) Dialog.findViewById(R.id.dwr_temp3);

                    ImageView dwr_img1 = (ImageView) Dialog.findViewById(R.id.dwr_img1);

                    ImageView dwr_img2 = (ImageView) Dialog.findViewById(R.id.dwr_img2);

                    ImageView dwr_img3 = (ImageView) Dialog.findViewById(R.id.dwr_img3);


                    JSONArray weather_jsonArray = new JSONArray(response.getString("list"));

                    JSONObject explrObject = weather_jsonArray.getJSONObject(8);
                    dwr_date1.setText(explrObject.getString("dt_txt"));

                    JSONObject main_list = new JSONObject(explrObject.getString("main"));
                    dwr_temp1.setText(""+main_list.getDouble("temp")+symbol());

                    JSONArray weather_list = new JSONArray(explrObject.getString("weather"));

                    Glide.with(mContext).load("http://openweathermap.org/img/w/"+weather_list.getJSONObject(0).getString("icon")+".png").into(dwr_img1);

                    explrObject = weather_jsonArray.getJSONObject(16);

                    dwr_date2.setText(explrObject.getString("dt_txt"));

                     main_list = new JSONObject(explrObject.getString("main"));
                    dwr_temp2.setText(""+main_list.getDouble("temp")+symbol());



                     weather_list = new JSONArray(explrObject.getString("weather"));

                    Glide.with(mContext).load("http://openweathermap.org/img/w/"+weather_list.getJSONObject(0).getString("icon")+".png").into(dwr_img2);

                     explrObject = weather_jsonArray.getJSONObject(24);

                    dwr_date3.setText(explrObject.getString("dt_txt"));

                     main_list = new JSONObject(explrObject.getString("main"));
                    dwr_temp3.setText(""+main_list.getDouble("temp")+symbol());

                     weather_list = new JSONArray(explrObject.getString("weather"));

                    Glide.with(mContext).load("http://openweathermap.org/img/w/"+weather_list.getJSONObject(0).getString("icon")+".png").into(dwr_img3);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext.getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(mContext.getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                Log.d("Error_volley", error.getMessage());

                hidepDialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);


    }

    public String symbol()
    {
        Cursor res = myDb.getAllData();
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append(res.getString(1));
        }
        if (buffer.toString().trim().equals("Kelvin")) {
            return " \u212A";
        } else if (buffer.toString().trim().equals("Celsius")) {
            return " \u2103";
        } else {
            return " \u2109";
        }
    }


    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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



    public void share_info(String message)
    {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Weather Report");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        mContext.startActivity(Intent.createChooser(sharingIntent, mContext.getResources().getString(R.string.app_name)));
    }



    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_weather, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()) {

                case R.id.action_add_favourite:
                    Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_play_next:
                    Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
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

    @Override
    public int getItemCount() {
        return weather_list.size();
    }
}
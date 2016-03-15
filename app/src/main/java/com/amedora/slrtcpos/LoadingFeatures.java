package com.amedora.slrtcpos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.amedora.slrtcpos.model.*;
import com.amedora.slrtcpos.utils.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Amedora on 12/24/2015.
 */
public class LoadingFeatures extends Activity {
    RequestQueue kQueue, mQueue,dQueue,bQueue;
    Button btnLoad;
    TextView tvLoadFeature;
    boolean tk =false;
    boolean tl = false; boolean rt =false;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_loading_features);
        mQueue = Volley.newRequestQueue(getApplicationContext());
        dQueue = Volley.newRequestQueue(getApplicationContext());
        bQueue = Volley.newRequestQueue(getApplicationContext());
        btnLoad =(Button) findViewById(R.id.btnLoad);
        tvLoadFeature = (TextView)findViewById(R.id.tvLoadingFeatures);
        tvLoadFeature.setText("Setting up your APP! Please Wait...");
        btnLoad.setVisibility(View.INVISIBLE);
        LoadF();
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadF();
            }
        });
    }

    protected void LoadF(){
        insertBuses();
        insertTerminals();
        getRoutes();

        if((tl == true && rt ==true)){
            Intent intent = new Intent(LoadingFeatures.this,RegisterActivity1.class);
            startActivity(intent);
        }else{
            btnLoad.setVisibility(View.GONE);
            btnLoad.setVisibility(View.VISIBLE);
            tvLoadFeature.setVisibility(View.INVISIBLE);
        }
    }
    public void getTicketing(){
        Apps apps = new Apps();
        String url ="http://41.77.173.124:81/srltcapi/public/ticketing/data/"+apps.getRoute_id();

        JsonArrayRequest jsonTicket = new JsonArrayRequest(Request.Method.GET,url,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    // Iterator<String> iter = response.keys();
                    int ja = response.length();
                    for(int i=0; i<ja; i++){
                        //String key = iter.next();
                        JSONObject term = (JSONObject) response.get(i);
                        //JSONArray jsonArrayTerminals = response.getJSONArray("data");
                        DatabaseHelper db = new DatabaseHelper(getApplicationContext());

                        Ticketing t = db.getTicketingByTicketId(term.getString("ticket_id"));
                        if(db.ifExistsTicketing(t)){

                        }else{
                            Ticketing ticketing = new Ticketing();
                            ticketing.setDriver(term.getString("driver"));
                            ticketing.setConductor(term.getString("conductor"));
                            ticketing.setQty(term.getInt("qty"));
                            ticketing.setBoard_stage(term.getString("board_stage"));
                            ticketing.setHighlight_stage(term.getString("highlight_stage"));
                            ticketing.setTicketing_id(term.getInt("ticket_id"));
                            ticketing.setScode(term.getString("scode"));
                            ticketing.setBus_no(term.getString("plate_no"));
                            ticketing.setRoute(term.getString("route_name"));
                            ticketing.setTripe(term.getString("trip_type"));
                            ticketing.setSerial_no(term.getString("serial_no"));
                            ticketing.setStatus(term.getInt("status"));

                            db.createTicketing(ticketing);
                        }
                        tk=true;

                    }

                }catch (Exception e){
                    tk=false;
                    Toast.makeText(LoadingFeatures.this, "Network Connection error! Please Click on refresh", Toast.LENGTH_SHORT).show();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tk = false;
            }
        });
        kQueue.add(jsonTicket);
    }
    public void getRoutes(){
        String url = "http://41.77.173.124:81/srltcapi/public/route/index";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    // Iterator<String> iter = response.keys();
                    int ja = response.length();
                    for(int i=0; i<ja; i++){
                        //String key = iter.next();
                        JSONObject term = (JSONObject) response.get(i);

                        //JSONArray jsonArrayTerminals = response.getJSONArray("data");
                        DatabaseHelper dbr = new DatabaseHelper(getApplicationContext());

                        Route r = dbr.getRouteByName(term.getString("short_name"));
                        if(dbr.ifExistsRoute(r)){
                        }else {
                            Toast.makeText(LoadingFeatures.this, "Setting up your APP! please wait... Loading Routes;", Toast.LENGTH_SHORT).show();
                            DatabaseHelper dbRoute = new DatabaseHelper(getApplicationContext());
                            Route route = new Route();
                            route.setRoute_id(term.getInt("id"));
                            route.setDescription(term.getString("description"));
                            route.setShort_name(term.getString("short_name"));
                            route.setName(term.getString("name"));
                            route.setDistance(term.getString("distance"));
                            dbRoute.createRoute(route);
                        }
                    }

                    rt = true;
                }catch (Exception e){
                    rt = false;
                    Toast.makeText(LoadingFeatures.this, e.getMessage() + "Please Click on refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                rt = false;
                Toast.makeText(LoadingFeatures.this, "Network Connection error! Please Click on refresh", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonArrayRequest);

    }
    private void insertTerminals(){
        //RequestQueue requestQueue = new RequestQueue(m)
        //mQueue = Volley.newRequestQueue(getApplicationContext());
        String url ="http://41.77.173.124:81/srltcapi/public/terminals/index";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    // Iterator<String> iter = response.keys();
                    int ja = response.length();
                    for(int i=0; i<ja; i++){
                        //String key = iter.next();
                        JSONObject term = (JSONObject) response.get(i);
                        //JSONArray jsonArrayTerminals = response.getJSONArray("data");
                        DatabaseHelper dbterminal = new DatabaseHelper(getApplicationContext());
                        Terminal t = dbterminal.getTerminalByName(term.getString("short_name"));
                        if(dbterminal.ifExists(t)){

                        }else{
                            Toast.makeText(LoadingFeatures.this, "Setting up your APP! please wait... Loading Station;", Toast.LENGTH_SHORT).show();
                            Terminal terminal = new Terminal();
                            terminal.setTerminal_id(term.getInt("id"));
                            terminal.setShort_name(term.getString("short_name"));
                            terminal.setName(term.getString("name"));
                            terminal.setDistance(term.getString("distance"));
                            terminal.setOne_way_from_fare(term.getDouble("one_way_from_fare"));
                            terminal.setOne_way_to_fare(term.getDouble("one_way_to_fare"));
                            terminal.setDistance(term.getString("distance"));
                            terminal.setRoute_id(term.getInt("route_id"));
                            terminal.setGeodata(term.getString("geodata"));
                            dbterminal.createTerminal(terminal);
                        }

                    }
                    tl =true;
                }catch (Exception e){
                    tl =false;
                    Toast.makeText(LoadingFeatures.this, e.getMessage() + "Please Click on refresh", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tl =false;
                Toast.makeText(LoadingFeatures.this, "Network Connection error! Please Click on refresh", Toast.LENGTH_SHORT).show();
            }
        });
        dQueue.add(jsonArrayRequest);
    }
    private void insertBuses(){
        //RequestQueue requestQueue = new RequestQueue(m)
        String url ="http://41.77.173.124:81/srltcapi/public/buses/index";
        JsonArrayRequest jsonArrayRequestBus = new JsonArrayRequest(Request.Method.GET,url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    // Iterator<String> iter = response.keys();
                    int ja = response.length();
                    for(int i=0; i<ja; i++){
                        //String key = iter.next();
                        JSONObject term = (JSONObject) response.get(i);
                        //JSONArray jsonArrayTerminals = response.getJSONArray("data");
                        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                        Bus b = db.getBusByPlateNo(term.getString("plate_no"));
                        if(db.ifExistsBus(b)){

                        }else{

                            Toast.makeText(LoadingFeatures.this, " Loading Buses", Toast.LENGTH_SHORT).show();
                            Bus bus = new Bus();
                            bus.setBus_id(term.getInt("id"));
                            bus.setDriver(term.getString("driver"));
                            bus.setPlate_no(term.getString("plate_no"));
                            bus.setConductor(term.getString("conductor"));
                            bus.setRoute_id(term.getInt("route_id"));

                            long u = db.createBus(bus);
                            String numberAsString = new Double(u).toString();
                            String counte = new Double(i).toString();
                            Toast.makeText(LoadingFeatures.this, numberAsString + ", " + counte, Toast.LENGTH_SHORT).show();
                        }

                    }

                }catch (Exception e){
                    VolleyLog.d("Error: " + e.getMessage());
                    Toast.makeText(LoadingFeatures.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error: " + error.getMessage());
                Toast.makeText(LoadingFeatures.this, error.toString(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                //pDialog.hide();
            }
        });
        int socketTimeout = 10000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonArrayRequestBus.setRetryPolicy(policy);
        bQueue.add(jsonArrayRequestBus);
    }
}

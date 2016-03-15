package com.amedora.slrtcpos.app;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.amedora.slrtcpos.model.Terminal;
import com.amedora.slrtcpos.utils.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Amedora on 12/11/2015.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    ContentResolver mContentResolver;

    String ERRORMSG;boolean success=false;
    //Account newAccount = new Account("dummyaccount", "com.sportsteamkarma");

    private String urlJsonObj = "http://41.77.173.124:81/busticketAPI/buses/index";
    Context context;
    DatabaseHelper db;
    Context mContext;
    RequestQueue mQueue;
    String TAG ="BUS-TICKET";
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context*/
        android.os.Debug.waitForDebugger();
        //mContentResolver = context.getContentResolver();
        mContext = context;
        //mContentResolver = context.getContentResolver();
        mQueue = Volley.newRequestQueue(context);
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(Context context,boolean autoInitialize,boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
      //  mContentResolver = context.getContentResolver();
        mContext = context;
       // mContentResolver = context.getContentResolver();
        mQueue = Volley.newRequestQueue(context);
    }

    /*
    * Specify the code you want to run in the sync adapter. The entire
    * sync adapter runs in a background thread, so you don't have to set
    * up your own background processing.
    */
    @Override
    public void onPerformSync(Account account, Bundle extras,String authority,ContentProviderClient provider,SyncResult syncResult) {
    /*
     * Put the data transfer code here.
     */
        Log.d("SYNC ERR", "Insert Terminals Did not get called");
        insertTerminals();
    }

    private void insertTerminals(){
        //RequestQueue requestQueue = new RequestQueue(m)
        mQueue = Volley.newRequestQueue(mContext);
        String url ="http://41.77.173.124:81/busticketAPI/terminals/index";
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
                        DatabaseHelper db = new DatabaseHelper(mContext);
                        Terminal t = db.getTerminalByName(term.getString("short_name"));
                        if(db.ifExists(t)){

                        }else{
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
                            db.createTerminal(terminal);
                        }
                    }
                }catch (Exception e){
                    VolleyLog.d(TAG, "Error: " + e.getMessage());
                   // Toast.makeText(SyncAdapter.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                //Toast.makeText(TicketingHomeActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                //pDialog.hide();
            }
        });
        mQueue.add(jsonArrayRequest);
    }


}

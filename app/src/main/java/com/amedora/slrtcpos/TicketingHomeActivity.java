package com.amedora.slrtcpos;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.amedora.slrtcpos.model.Apps;
import com.amedora.slrtcpos.model.Bus;
import com.amedora.slrtcpos.model.Terminal;
import com.amedora.slrtcpos.model.Ticket;
import com.amedora.slrtcpos.model.Trip;
import com.amedora.slrtcpos.utils.DatabaseHelper;
import com.amedora.slrtcpos.utils.DrawerAdapter;
import com.amedora.slrtcpos.utils.Installation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Amedora on 12/4/2015.
 */
public class TicketingHomeActivity extends AppCompatActivity {
    DatabaseHelper db = new DatabaseHelper(this);
    Spinner spBoard, spBuses, spTrips, spHighlight;
    String[] dias;
    Button btnGenerate;
    String bus, board, highlight, trip;
    public static String TAG_NAME, TAG_SHORT_NAME;
    ArrayList<HashMap<String, String>> terminalList, busList, TicketList;
    public static final String TAG ="My App";
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    public static final String AUTHORITY = "com.amedora.slrtcpos.app";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "busticket.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    RequestQueue mQueue,rQSyncTicket,rQSyncBalance,rqTrip;
    // Instance fields
    Account mAccount;
    ContentResolver mResolver;
    public static final long SECONDS_PER_MINUTE = 2L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 2L;
    public static final long SYNC_INTERVAL =SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;
    Apps apps;
    Toolbar myToolbar;


    //Section for drawer
    String TITLES[] = {"Home","Account","Ticket","Sync","Trip","Logout"};
    int ICONS[] = {R.drawable.ic_home,R.drawable.ic_action_account,R.drawable.ic_ticket,R.drawable.ic_refresh,R.drawable.ic_bus,R.drawable.ic_action_logout};

    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view


    String EMAIL = "amedora09@gmail.com";
    int PROFILE = R.drawable.logo;

    private Toolbar toolbar;                              // Declaring the Toolbar Object

    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;
                                      // Declaring DrawerLayout

    ProgressDialog dialog  =null;
    String NAME="";
    ActionBarDrawerToggle mDrawerToggle;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_ticket_home);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //synchTrips();
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        spBoard = (Spinner) findViewById(R.id.spBoard);
        spBuses = (Spinner) findViewById(R.id.spBusNo);
        spTrips = (Spinner) findViewById(R.id.spTripType);
        spHighlight = (Spinner) findViewById(R.id.spHighlight);
        mAccount = CreateSyncAccount(this);
        mQueue = Volley.newRequestQueue(getApplicationContext());
        rQSyncTicket = Volley.newRequestQueue(getApplicationContext());
        rQSyncBalance = Volley.newRequestQueue(getApplicationContext());
        rqTrip = Volley.newRequestQueue(getApplicationContext());
        insertTerminals();
        insertBuses();
        getTickets();
        apps = db.getApp(Installation.appId(getApplicationContext()));
        EMAIL =apps.getRoute_name();
        NAME = "CODE: "+apps.getAgent_code().toUpperCase() +" Trip: "+apps.getTripCount();
        // Get the content resolver for your app
       mResolver = getContentResolver();
        /*
         * Turn on periodic syncing
         */

        mTitle = mDrawerTitle = getTitle();
       ContentResolver.addPeriodicSync(CreateSyncAccount(this), AUTHORITY, Bundle.EMPTY, SYNC_INTERVAL);

        String[] tdata = populateTerminals();
        String[] bdata = populateBuses();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, tdata);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBoard.setAdapter(adapter);/**/

        ArrayAdapter Hadapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, tdata);
        Hadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHighlight.setAdapter(Hadapter);

        ArrayAdapter gadapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, bdata);
        gadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBuses.setAdapter(gadapter);

        ArrayAdapter<CharSequence> tadapter = ArrayAdapter.createFromResource(this, R.array.tripTypes, android.R.layout.simple_spinner_item);
        tadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /*ArrayAdapter badapter =new ArrayAdapter(this,android.R.layout.simple_spinner_item,bdata);
        badapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);*/
        spTrips.setAdapter(tadapter);

        spBuses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                //((TextView) parent.getChildAt(0)).setTextSize(25);
                bus = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spBoard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                //((TextView) parent.getChildAt(0)).setTextSize(25);
                board = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spHighlight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                //((TextView) parent.getChildAt(0)).setTextSize(25);
                highlight = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spTrips.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                //((TextView) parent.getChildAt(0)).setTextSize(25);
                trip = parent.getItemAtPosition(position).toString();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnGenerate = (Button) findViewById(R.id.btnGenerate);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // get a new activity to show invoice preview
                sendData();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new DrawerAdapter(TITLES,ICONS,NAME,EMAIL,PROFILE);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

                                      // Setting the adapter to RecyclerView
        //mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);
               // Drawer object Assigned to the view

        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
                invalidateOptionsMenu();
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
                invalidateOptionsMenu();
            }
        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State


        final GestureDetector mGestureDetector = new GestureDetector(TicketingHomeActivity.this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    Drawer.closeDrawers();
                    if(recyclerView.getChildPosition(child) == 1){

                    }else if(recyclerView.getChildPosition(child) == 2){
                        Intent intent = new Intent(TicketingHomeActivity.this,AccountActivity.class);
                        startActivity(intent);
                    }else if(recyclerView.getChildPosition(child) == 3){
                        Intent intent = new Intent(TicketingHomeActivity.this,TicketListActivity.class);
                        startActivity(intent);
                    }else if(recyclerView.getChildPosition(child) == 4){

                        syncTickets();

                        dialog = ProgressDialog.show(TicketingHomeActivity.this, "", "Synchronizing App Data. Please wait...", true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        synchAccount();
                                        insertBuses();
                                        //Toast.makeText(TicketingHomeActivity.this, "Looper startes ", Toast.LENGTH_SHORT).show();
                                        handler.removeCallbacks(this);
                                        Looper.myLooper().quit();
                                    }
                                }, 30000);
                                Looper.loop();
                            }
                        }).start();
                    }else if(recyclerView.getChildPosition(child) == 5){
                        if(apps.getLicenceNo() != null){
                            Intent intent = new Intent(TicketingHomeActivity.this,TripHomeActivity.class);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(TicketingHomeActivity.this,TestPrintActivity.class);
                            startActivity(intent);
                            Toast.makeText(TicketingHomeActivity.this, "Application is in default mode you cannot use this menu", Toast.LENGTH_SHORT).show();
                        }
                    }else if(recyclerView.getChildPosition(child) == 6){
                        Intent intent = new Intent(TicketingHomeActivity.this,LogoutActivity.class);
                        startActivity(intent);
                       // Toast.makeText(TicketingHomeActivity.this, "The Item Clicked is: " + recyclerView.getChildPosition(child), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });
        List<Ticket> issuedTickets = db.getIssuedTickets();
        Log.e("SYNC LOG", issuedTickets.toString());
    }



    public String[] populateTerminals() {
        terminalList = new ArrayList<HashMap<String, String>>();
        List<Terminal> terminal = db.getTerminalsByRouteId(apps.getRoute_id());

        TicketList = new ArrayList<HashMap<String, String>>();
        List<Ticket> ticket = db.getAllTickets();
        dias = new String[terminal.size()];

        for (int i = 0; i < terminal.size(); i++) {
            Terminal s = terminal.get(i);
            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            // adding each child node to HashMap key => value

            map.put(TAG_SHORT_NAME, String.valueOf(s.getShort_name()));
            // adding HashList to ArrayList
            terminalList.add(map);
            // add sqlite id to array
            // used when deleting a website from sqlite
            dias[i] = String.valueOf(s.getShort_name());
        }
        return dias;
    }

    public String[] populateBuses() {
        busList = new ArrayList<HashMap<String, String>>();
        List<Bus> bus = db.getBusesByRouteId(apps.getRoute_id());
        if(bus != null){

        }else{
            bus = db.getAllBuses();
        }
        dias = new String[bus.size()];
        for (int i = 0; i < bus.size(); i++) {
            Bus s = bus.get(i);
            //creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            //adding each child node to HashMap key => value
            map.put(TAG_SHORT_NAME, String.valueOf(s.getPlate_no()));
            // adding HashList to ArrayList
            busList.add(map);
            // add sqlite id to array
            // used when deleting a website from sqlite
            dias[i] = String.valueOf(s.getPlate_no());
        }
        return dias;
    }

    private void sendData() {
        Intent intent = new Intent(TicketingHomeActivity.this, GenerateTicketActivity.class);
        intent.putExtra("Board", board);
        intent.putExtra("Highlight", highlight);
        intent.putExtra("Trip", trip);
        intent.putExtra("Bus", bus);
        startActivity(intent);
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.d("SYNC ERR", "The account exists or some other error occurred. Log this, report it,");
        }
        return newAccount;
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
                        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
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
                    Toast.makeText(TicketingHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(TicketingHomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                //pDialog.hide();
            }
        });
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonArrayRequest.setRetryPolicy(policy);
        mQueue.add(jsonArrayRequest);
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
                            Toast.makeText(TicketingHomeActivity.this, " Beginning Bus Synchronization", Toast.LENGTH_SHORT).show();

                            b.setDriver(term.getString("driver"));

                            b.setRoute_id(term.getInt("route_id"));

                            long u = db.updateBus(b);
                            String numberAsString = new Double(u).toString();
                            String counte = new Double(i).toString();
                            Toast.makeText(TicketingHomeActivity.this,"Updating bus details "+numberAsString+", "+counte, Toast.LENGTH_SHORT).show();


                        }else{

                            Toast.makeText(TicketingHomeActivity.this, " Beginning Ticket Synchronization", Toast.LENGTH_SHORT).show();
                            Bus bus = new Bus();
                            bus.setBus_id(term.getInt("id"));
                            bus.setDriver(term.getString("driver"));
                            bus.setPlate_no(term.getString("plate_no"));
                            bus.setConductor(term.getString("conductor"));
                            bus.setRoute_id(term.getInt("route_id"));

                            long u = db.createBus(bus);
                            String numberAsString = new Double(u).toString();
                            String counte = new Double(i).toString();
                            Toast.makeText(TicketingHomeActivity.this, numberAsString + ", " + counte, Toast.LENGTH_SHORT).show();
                        }

                    }

                }catch (Exception e){
                    VolleyLog.d(TAG, "Error: " + e.getMessage());
                    Toast.makeText(TicketingHomeActivity.this, "Network Error 1", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(TicketingHomeActivity.this, "Network Error 2", Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                //pDialog.hide();
            }
        });
        int socketTimeout = 10000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonArrayRequestBus.setRetryPolicy(policy);
        mQueue.add(jsonArrayRequestBus);
    }

    private void getTickets(){
        String url ="http://41.77.173.124:81/srltcapi/public/tickets/data/"+Installation.appId(getApplicationContext());
        JsonArrayRequest jsonArrayRequestTicket = new JsonArrayRequest(Request.Method.GET,url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try{
                    VolleyLog.d(TAG, "MSG: Beginning Ticket Synchronization " );
                    Toast.makeText(TicketingHomeActivity.this, " Beginning Ticket Synchronization", Toast.LENGTH_SHORT).show();
                    int ja = response.length();
                    if(ja >1){
                        for(int w=0; w<ja; w++){
                            // String key = iter.next();
                            JSONObject jsonObject = (JSONObject) response.get(w);
                            DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                            Ticket t = db.getTicketBySerialNo(jsonObject.getString("serial_no"));
                            if(!db.ifExists(t)){
                                Ticket ticket = new Ticket();
                                ticket.setTicket_id(jsonObject.getInt("id"));
                                ticket.setSerial_no(jsonObject.getString("serial_no"));
                                ticket.setBatch_code(jsonObject.getString("stack_id"));
                                ticket.setRoute_id(jsonObject.getInt("route_id"));
                                ticket.setStatus(jsonObject.getInt("status"));
                                ticket.setAmount(jsonObject.getDouble("amount"));
                                ticket.setScode(jsonObject.getString("code"));
                                ticket.setTerminal_id(jsonObject.getInt("terminal_id"));
                                ticket.setTicket_type(jsonObject.getString("ticket_type"));
                                long u = db.createTicket(ticket);
                                String numberAsString = new Double(u).toString();
                                String counte = new Double(w).toString();
                                Toast.makeText(TicketingHomeActivity.this, numberAsString + ", " + counte, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }else{
                        String gg  ="Please load your account \r\n" ;
                                gg +="there is no ticket in your account";
                        Toast.makeText(TicketingHomeActivity.this, gg, Toast.LENGTH_SHORT).show();
                    }

                }catch(Exception e){
                    VolleyLog.d(TAG, "Error: " + e.getMessage());
                    Toast.makeText(TicketingHomeActivity.this, "Network Error 1", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(TicketingHomeActivity.this, "Network Error 2", Toast.LENGTH_SHORT).show();
            }
        });


        int socketTimeout = 1200000;//2 min - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonArrayRequestTicket.setRetryPolicy(policy);
        mQueue.add(jsonArrayRequestTicket);
    }

    private void synchTrips(){
        List<Trip> registeredTrip = db.getAllTrip();
        if(registeredTrip.size() >=1){
            HashMap<String,String> params = new HashMap<String,String>();
            JSONObject jsonObj = new JSONObject();
            String url ="http://41.77.173.124:81/busticketAPI/triplog/batchsync";
            params.put("triplogs",registeredTrip.toString());
            JSONObject j = new JSONObject(params);
            JsonObjectRequest jsonObjectRequestTrips = new JsonObjectRequest(Request.Method.POST, url, j, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{

                        if(true){
                            JSONArray jsonArray = response.getJSONArray("data");
                            if(jsonArray.length() > 0){
                                Toast.makeText(TicketingHomeActivity.this, "Sending Trip log to server ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                        Log.e("Sysnc Err", ex.getMessage());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            int socketTimeout = 30000;//30 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequestTrips.setRetryPolicy(policy);
            rqTrip.add(jsonObjectRequestTrips);
        }
    }

    private void syncTickets(){
        List<Ticket> issuedTickets = db.getIssuedTickets(); // get tickets issued with status 1
        if(issuedTickets.size()>=1){
            HashMap<String, String> params = new HashMap<String, String>();

            String url ="http://41.77.173.124:81/busticketAPI/tickets/batchsync/"+Installation.appId(getApplicationContext());
            params.put("tickets", issuedTickets.toString());
            JSONObject j = new JSONObject(params);
            Log.d("BATCH", issuedTickets.toString());
            //params.put("app_id",)
            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST,url, j, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        String code = response.getString("code");
                        Log.d("RES", response.toString());//Boolean.getBoolean(response.getString("success")) == true
                        if(code.equals("200")){
                            JSONArray jsonArray = response.getJSONArray("data");
                            if(jsonArray.length() > 0){
                                for(int i = 0; i < jsonArray.length(); i++){
                                    DatabaseHelper dbr = new DatabaseHelper(getApplicationContext());
                                    Ticket r = dbr.getTicketBySerialNo(jsonArray.getJSONObject(i).getString("serial_no"));
                                    if(dbr.ifExists(r)){
                                        r.setStatus(jsonArray.getJSONObject(i).getInt("status"));
                                        long u = db.updateTicket(r);
                                        if(u > 0){
                                            Toast.makeText(TicketingHomeActivity.this, "Synchronising.." + i, Toast.LENGTH_SHORT).show();
                                        }
                                    }else {
                                        Toast.makeText(TicketingHomeActivity.this, "Synchronising.. Data Up to date" + i, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }else {
                                Toast.makeText(TicketingHomeActivity.this, "Synchronising.. Data Up to date", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(TicketingHomeActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                        Log.e("Sysnc Err", ex.getMessage());
                    }
                    dialog.dismiss();
                    //Toast.makeText(TicketingHomeActivity.this,"Record Updated to server Successfully", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    //Log.d("VEE", error.getMessage());
                    Toast.makeText(TicketingHomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    //VolleyLog.d(error.getMessage(),"VVVV");
                }
            });
            int socketTimeout = 120000;//30 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonArrayRequest.setRetryPolicy(policy);
            rQSyncTicket.add(jsonArrayRequest);


        }else{
            Toast.makeText(TicketingHomeActivity.this, "No ticket data available to synchronize", Toast.LENGTH_SHORT).show();
        }
        //
    }

    public void synchAccount(){
        String url ="http://41.77.173.124:81/srltcapi/public/account/synch/"+apps.getApp_id();

        JsonObjectRequest uAccount = new JsonObjectRequest(Request.Method.GET,url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try{
                    JSONObject jsonObject= new JSONObject(response.toString());

                    String code = jsonObject.getString("code");

                    if(code.equals("200")){
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        JSONObject myapp = jsonArray.getJSONObject(0);

                        apps.setRoute_id(myapp.getInt("route_id"));
                        apps.setTerminal_id(myapp.getInt("station_id"));
                        apps.setRoute_name(myapp.getString("route_name"));
                        apps.setTerminal(myapp.getString("station_name"));
                        apps.setBalance(myapp.getDouble("balance"));
                        apps.setStatus(myapp.getInt("status"));
                        apps.setAgent_code(myapp.getString("agent_code"));
                        //apps.setIs_logged_in(cursor.getColumnIndex(KEY_APP_IS_LOGGED_IN));
                        //apps.setBusID(cursor.getInt(cursor.getColumnIndex(KEY_APP_BUS_ID)));
                        //apps.setScheduleID(cursor.getInt(cursor.getColumnIndex(KEY_APP_SCHEDULE_ID)));
                        //apps.setDriverFname(cursor.getString(cursor.getColumnIndex(KEY_APP_DRIVER_FNAME)));
                        //apps.setDriverLname(cursor.getString(cursor.getColumnIndex(KEY_APP_DRIVER_LNAME)));
                        //apps.setDriverID(cursor.getInt(cursor.getColumnIndex(KEY_APP_DRIVER_ID)));
                        //apps.setTripCount(cursor.getInt(cursor.getColumnIndex(KEY_APP_TRIPS)));

                        apps.setAppMode(myapp.getInt("app_mode"));


                        if(db.updateApp(apps)>0){
                            Toast.makeText(TicketingHomeActivity.this, "Account Synchronized Successfully", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(TicketingHomeActivity.this, "Unexpected Error! Record could not be synchronized", Toast.LENGTH_SHORT).show();
                        }


                    }else {
                        //errTv.setText("License Number does not exist in SRLTC Database");
                        Toast.makeText(TicketingHomeActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                    }



                }catch (Exception ex){
                    ex.printStackTrace();
                    Log.e("Sysnc Err", ex.getMessage());
                    Toast.makeText(TicketingHomeActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    VolleyLog.d("Driver Err", ex.getMessage());
                }

                Toast.makeText(TicketingHomeActivity.this, "Account Updated", Toast.LENGTH_SHORT).show();
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TicketingHomeActivity.this, "Account balance could not be updated Unexpected Errors", Toast.LENGTH_SHORT).show();
            }
        });
        int socketTimeout = 20000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        uAccount.setRetryPolicy(policy);
        rQSyncBalance.add(uAccount);
    }

    private void syncAccount(){
        List<Ticket> issuedTickets = db.getIssuedTickets(); // get tickets issued with status 1
        if(issuedTickets.size()>=1){
            HashMap<String, String> params = new HashMap<String, String>();
            JSONObject jsonBody = new JSONObject();
            //jsonBody.put(ticket);

            String url ="http://41.77.173.124:81/srltcapi/public/account/balancesync/"+Installation.appId(getApplicationContext());
            StringRequest post = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {

                        apps.setBalance(Double.parseDouble(response.toString()));

                            //listener.onResponse(response.toString());
                        if(db.updateApp(apps) > 0){
                            Toast.makeText(TicketingHomeActivity.this, "Account Updated! Your current balance is " + response.toString(), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(TicketingHomeActivity.this, "Unexpected Error! Your current balance was not updated ", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Error: ", e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            int socketTimeout = 20000;//30 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            post.setRetryPolicy(policy);
            rQSyncBalance.add(post);


        }
        dialog.dismiss();
    }

    private void postTicket(){
        List<Ticket> usedTickets = db.getUsedTickets();
        int k =0;
        if(usedTickets.size()>=1){
            for(Ticket ticket : usedTickets){
                String url ="http://41.77.173.124:81/srltcapi/public/tickets/update/"+ticket.getTicket_id()+"/1";
                JsonObjectRequest jUpdateTicket = new JsonObjectRequest(Request.Method.GET,url,new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.getString("msg") == "success"){
                                Toast.makeText(TicketingHomeActivity.this, "Record Updated ton server Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }catch(Exception e){
                            Toast.makeText(TicketingHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TicketingHomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                int socketTimeout = 30000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                jUpdateTicket.setRetryPolicy(policy);
                mQueue.add(jUpdateTicket);
            }
        }

    }

    private void postTicketing(){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("ticket_id", "AbCdEfGh123456");
        //JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
               // new Response.Listener<JSONObject>()
    }

    private void getAppBalance(){
        String url = "http://41.77.173.124:81/srltcapi/public/account/update/"+apps.getApp_id()+"/"+apps;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,url,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.getString("code").equals("200") ){
                        Toast.makeText(TicketingHomeActivity.this, "Record Updated to server Successfully", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){
                    Toast.makeText(TicketingHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        int socketTimeout = 35000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjReq.setRetryPolicy(policy);
        mQueue.add(jsonObjReq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_tickets:
               Intent intent = new Intent(TicketingHomeActivity.this,TicketListActivity.class);
                startActivity(intent);
                break;
            case R.id.action_ticketing:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = Drawer.isDrawerOpen(mRecyclerView);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}

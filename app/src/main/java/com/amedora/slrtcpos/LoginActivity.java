package com.amedora.slrtcpos;

/**
 * Created by USER on 1/17/2016.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amedora.slrtcpos.model.Bus;
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
import com.amedora.slrtcpos.utils.DatabaseHelper;
import com.amedora.slrtcpos.utils.Installation;

import org.json.JSONArray;
import org.json.JSONObject;

//import butterknife.InjectView;
public class LoginActivity extends AppCompatActivity {
private static final String TAG = "LoginActivity";
private static final int REQUEST_SIGNUP = 0;
    DatabaseHelper db;
    TextView tvlogin;
    Apps app;


EditText _emailText;
EditText _passwordText;
Button _loginButton;
    String email ;
    String password;
    RequestQueue rqRequest,kQueue,bQueue;

@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_login);

_passwordText = (EditText) findViewById(R.id.input_password);
rqRequest = Volley.newRequestQueue(this);
_loginButton = (Button) findViewById(R.id.btn_login);
tvlogin = (TextView)findViewById(R.id.tvloginError);
db = new DatabaseHelper(getApplicationContext());
app = db.getApp(Installation.appId(getApplicationContext()));
kQueue = Volley.newRequestQueue(getApplicationContext());
    bQueue = Volley.newRequestQueue(getApplicationContext());
synchAccount();
    insertBuses();
_loginButton.setOnClickListener(new View.OnClickListener() {

    @Override
    public void onClick(View v) {
        insertBuses();
        login();
    }
});

}

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed

                        if (loginLocal()) {
                            onLoginSuccess();
                        } else {
                            onLoginFailed();
                        }

                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }


    @Override
    public void onBackPressed() {
        // disable going back to the LoginActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
        if(app.getAppMode()== 1){//app.getLicenceNo().isEmpty() &&
            if( app.getLicenceNo() == null){
                Intent myIntent = new Intent(LoginActivity.this,RegisterDriverActivity.class);
                startActivity(myIntent);
                finish();
            }else if(app.getLicenceNo() != null){
                if(app.getDriverLoggedIn() ==0){

                    Intent myIntent = new Intent(LoginActivity.this,LoginDriverActivity.class);
                    startActivity(myIntent);
                    finish();
                }else{
                    // if driver is logged in we need to also check if trip is closed
                    /**
                     * If current trip exist means trip is open
                     * else we ask driver to login to open a trip
                     */
                    if(db.getCurrTrip() != null){
                        Intent myIntent = new Intent(LoginActivity.this,TicketingHomeActivity.class);
                        startActivity(myIntent);
                        finish();
                    }else{
                        Intent myIntent = new Intent(LoginActivity.this,LoginDriverActivity.class);
                        startActivity(myIntent);
                        finish();
                    }

                }

            }else{
                Intent myIntent = new Intent(LoginActivity.this,TicketingHomeActivity.class);
                startActivity(myIntent);
                finish();
            }

        }else if(app.getAppMode() ==0){
            Intent myIntent = new Intent(LoginActivity.this,TicketingHomeActivity.class);
            startActivity(myIntent);
            finish();
        }else{
            Toast.makeText(LoginActivity.this, "Your application is disabled", Toast.LENGTH_LONG).show();
        }

    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;
        String password = _passwordText.getText().toString();

        /*if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }
*/
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public boolean loginLocal(){
        DatabaseHelper fb = new DatabaseHelper(getApplicationContext());

        String password = _passwordText.getText().toString();
        final Apps appLogin = fb.getApp(Installation.appId(getApplicationContext()));


        if( password.equals(appLogin.getPassword())){
            Toast.makeText(this, "Login Successful on local", Toast.LENGTH_LONG).show();
            appLogin.setIs_logged_in(1);
            DatabaseHelper kb =  new DatabaseHelper(getApplicationContext());
            if(kb.updateApp(appLogin) >0){
                serverLogin();
                return true;
            }else{
                tvlogin.setText("Login was not successful please contact Administrator");
                return false;
            }



        }else{
            tvlogin.setText("Invalid username or password combination please try again");
            return false;
        }
    }


    public void serverLogin(){
        String url = "http://41.77.173.124:81/srltcapi/public/account/login/"+Installation.appId(this);
        StringRequest strUpdateAccount = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        strUpdateAccount.setRetryPolicy(policy);
        rqRequest.add(strUpdateAccount);
    }


    public void synchAccount(){
        String url ="http://41.77.173.124:81/srltcapi/public/account/synch/"+app.getApp_id();

        JsonObjectRequest uAccount = new JsonObjectRequest(Request.Method.GET,url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try{
                    JSONObject jsonObject= new JSONObject(response.toString());

                    String code = jsonObject.getString("code");

                    if(code.equals("200")){
                        Toast.makeText(LoginActivity.this, "Connected to server... Synchronizing Application Data", Toast.LENGTH_SHORT).show();
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        JSONObject myapp = jsonArray.getJSONObject(0);
                        //Toast.makeText(LoginActivity.this,myapp.toString(), Toast.LENGTH_SHORT).show();

                        app.setRoute_id(myapp.getInt("route_id"));
                        app.setTerminal_id(myapp.getInt("station_id"));
                        app.setRoute_name(myapp.getString("route_name"));
                        app.setTerminal(myapp.getString("station_name"));
                        app.setBalance(myapp.getDouble("balance"));
                        app.setStatus(myapp.getInt("status"));
                        app.setAgent_code(myapp.getString("agent_code"));
                        //apps.setIs_logged_in(cursor.getColumnIndex(KEY_APP_IS_LOGGED_IN));
                        //apps.setBusID(cursor.getInt(cursor.getColumnIndex(KEY_APP_BUS_ID)));
                        //apps.setScheduleID(cursor.getInt(cursor.getColumnIndex(KEY_APP_SCHEDULE_ID)));
                        //apps.setDriverFname(cursor.getString(cursor.getColumnIndex(KEY_APP_DRIVER_FNAME)));
                        //apps.setDriverLname(cursor.getString(cursor.getColumnIndex(KEY_APP_DRIVER_LNAME)));
                        //apps.setDriverID(cursor.getInt(cursor.getColumnIndex(KEY_APP_DRIVER_ID)));
                        //apps.setTripCount(cursor.getInt(cursor.getColumnIndex(KEY_APP_TRIPS)));

                        app.setAppMode(myapp.getInt("app_mode"));


                        if(db.updateApp(app)>0){
                            Toast.makeText(LoginActivity.this, "Application Data Synchronized Successfully", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(LoginActivity.this, "Unexpected Error! synchronized Application data could not be update ", Toast.LENGTH_SHORT).show();
                        }


                    }else {
                        //errTv.setText("License Number does not exist in SRLTC Database");
                        Toast.makeText(LoginActivity.this, "Server Error! Application data could not be synchronized ", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    Log.e("Sysnc Err", ex.getMessage());
                    Toast.makeText(LoginActivity.this, "No connection to server was established! could not synchronized account data", Toast.LENGTH_SHORT).show();
                    VolleyLog.d(TAG, ex.getMessage());
                }

                Toast.makeText(LoginActivity.this, "Account Updated", Toast.LENGTH_SHORT).show();
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "Account balance could not be updated Unexpected Errors", Toast.LENGTH_SHORT).show();
            }
        });
        int socketTimeout = 20000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        uAccount.setRetryPolicy(policy);
        kQueue.add(uAccount);
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

                            Toast.makeText(LoginActivity.this, " Loading Buses", Toast.LENGTH_SHORT).show();
                            Bus bus = new Bus();
                            bus.setBus_id(term.getInt("id"));
                            bus.setDriver(term.getString("driver"));
                            bus.setPlate_no(term.getString("plate_no"));
                            bus.setConductor(term.getString("conductor"));
                            bus.setRoute_id(term.getInt("route_id"));

                            long u = db.createBus(bus);
                            String numberAsString = new Double(u).toString();
                            String counte = new Double(i).toString();
                            Toast.makeText(LoginActivity.this, numberAsString + ", " + counte, Toast.LENGTH_SHORT).show();
                        }

                    }

                }catch (Exception e){
                    VolleyLog.d("Error: " + e.getMessage());
                    Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error: " + error.getMessage());
                Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                //pDialog.hide();
            }
        });
        int socketTimeout = 1000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonArrayRequestBus.setRetryPolicy(policy);
        bQueue.add(jsonArrayRequestBus);
    }

}

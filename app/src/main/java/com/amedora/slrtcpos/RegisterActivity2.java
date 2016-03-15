package com.amedora.slrtcpos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amedora.slrtcpos.model.Bus;
import com.amedora.slrtcpos.utils.*;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by Amedora on 7/10/2015.
 */
public class RegisterActivity2 extends Activity {
    EditText edtPassword,edtConfirm;
    String StoreName,StoreContact,mailmy,imei;
    Button btnNext,btnBack;
    HttpPost httppost;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    ProgressDialog dialog  =null;
    String AppID = "";
    RequestQueue vQueue;
    final static String TAG ="Register 2";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen2);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtConfirm  =   (EditText)findViewById(R.id.edtConfirm);
        Bundle bundle = getIntent().getExtras();
        StoreName 		= bundle.getString("SName");
        StoreContact 		= bundle.getString("SContact");
        mailmy 		= bundle.getString("Email");
        Log.e("MAil", getIntent().getExtras().getString("Email"));
        btnBack     = (Button) findViewById(R.id.btnregback2);
        btnNext     = (Button) findViewById(R.id.btnregnext2);


        vQueue = Volley.newRequestQueue(getApplicationContext());
        edtConfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Validation.isPasswordMatch(edtPassword, edtConfirm, true);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkValidation()){
                    sendData();
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Password mismatch please ensure that you enter the same character in the two input fields",
                            Toast.LENGTH_LONG).show();
                }

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    private void  sendData(){
        insertBuses();
        Intent intent = new Intent(RegisterActivity2.this,RegisterActivity3.class);
        //TelephonyManager tmgt = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //imei = tmgt.getDeviceId();
        intent.putExtra("SContact",getIntent().getExtras().getString("SContact"));

        intent.putExtra("SName",getIntent().getExtras().getString("SName"));
        intent.putExtra("Email", getIntent().getExtras().getString("Email"));
        intent.putExtra("Password", edtPassword.getText().toString());
        startActivity(intent);
    }

    private boolean checkValidation() {
        boolean ret = true;
        if (!Validation.hasText(edtPassword)) ret = false;
        if (!Validation.isPasswordMatch(edtPassword, edtConfirm, true)) ret = false;
        //if (!Validation.isPhoneNumber(edtLname, false)) ret = false;
        return ret;
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

                            Toast.makeText(RegisterActivity2.this, " Beginning Ticket Synchronization", Toast.LENGTH_SHORT).show();
                            Bus bus = new Bus();
                            bus.setBus_id(term.getInt("id"));
                            bus.setDriver(term.getString("driver"));
                            bus.setPlate_no(term.getString("plate_no"));
                            bus.setConductor(term.getString("conductor"));
                            bus.setRoute_id(term.getInt("route_id"));

                            long u = db.createBus(bus);
                            String numberAsString = new Double(u).toString();
                            String counte = new Double(i).toString();
                            Toast.makeText(RegisterActivity2.this, numberAsString + ", " + counte, Toast.LENGTH_SHORT).show();
                        }

                    }

                }catch (Exception e){
                    VolleyLog.d(TAG, "Error: " + e.getMessage());
                    Toast.makeText(RegisterActivity2.this, "Network Error 1", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(RegisterActivity2.this, "Network Error 2", Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                //pDialog.hide();
            }
        });
        int socketTimeout = 1000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonArrayRequestBus.setRetryPolicy(policy);
        vQueue.add(jsonArrayRequestBus);
    }

}

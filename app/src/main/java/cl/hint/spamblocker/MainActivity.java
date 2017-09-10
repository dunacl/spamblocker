package cl.hint.spamblocker;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SQLiteDB sqlite_obj;


    // Variables
    Button btnGetPhones;
    Button btnGetPhonesDB;

    TextView tvPhoneList;
    TextView tvPhoneListDB;

    public String baseURL = "http://200.73.210.145:8081/phones";
    RequestQueue requestQueue;
    String url;


    // It holds the list of Blacklist objects fetched from Database
    public static List blockList =  new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqlite_obj = new SQLiteDB(MainActivity.this);

        this.btnGetPhones = (Button) findViewById(R.id.btn_sync);
        this.btnGetPhonesDB = (Button) findViewById(R.id.btn_db);
        this.tvPhoneList = (TextView) findViewById(R.id.tv_phone_list);
        this.tvPhoneListDB = (TextView) findViewById(R.id.tv_phone_db);
        this.tvPhoneList.setMovementMethod(new ScrollingMovementMethod());
        this.tvPhoneListDB.setMovementMethod(new ScrollingMovementMethod());

        requestQueue = Volley.newRequestQueue(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getPhoneList();
        this.getPhoneListDB();
        Log.d("DB", "Initial List "+ blockList.toString());
    }

    private void clearPhoneList() {
        this.tvPhoneList.setText("");
        this.tvPhoneListDB.setText("");
    }


    private void  addToPhoneList(int id, String phoneNumber, String reason) {
        String strRow = id + " / " + phoneNumber + " / " + reason;
        String currentText = tvPhoneList.getText().toString();
        this.tvPhoneList.setText(currentText + "\n\n" + strRow);
    }

    private void  addToPhoneListDB(int id, String phoneNumber, String reason) {
        String strRowDB = id + " / " + phoneNumber + " / " + reason;
        String currentText = tvPhoneListDB.getText().toString();
        this.tvPhoneListDB.setText(currentText + "\n\n" + strRowDB);
    }


    private void setPhoneListText(String str) {
        this.tvPhoneList.setText(str);
    }

    private void getPhoneList() {
        // Database
        sqlite_obj.open();
        sqlite_obj.deleteAll();
        Log.d("DB", "SE BORRO TODO");

        this.url = this.baseURL;

        // Next, we create a new JsonArrayRequest. This will use Volley to make a HTTP request
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONArray jsonArray = null;
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                try {

                                    JSONObject jsonObj = response.getJSONObject(i);

                                    jsonArray = jsonObj.getJSONArray("data");
                                    for(int ii = 0; ii < jsonArray.length(); ii++) {
                                        try {
                                            JSONObject jsonInArray = jsonArray.getJSONObject(ii);

                                            int id = jsonInArray.getInt("id");
                                            String phoneNumber = jsonInArray.get("phonenumber").toString();
                                            String reason = jsonInArray.get("reason").toString();
                                            addToPhoneList(id, phoneNumber, reason);
                                            sqlite_obj.insert(id, phoneNumber, reason);

                                        } catch (JSONException e) {
                                            // If there is an error then output this to the logs.
                                            Log.e("Volley", "Invalid JSON Object.");
                                        }
                                    }

                                } catch (JSONException e) {
                                    // If there is an error then output this to the logs.
                                    Log.e("Volley", "Invalid JSON Object.");
                                }
                            }
                            Log.d("DB", "Closing BD after Sync");
                            sqlite_obj.close();

                        } else {
                            // The user didn't have any repos.
                            setPhoneListText("Nothing found.");
                        }

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setPhoneListText("Error while calling REST API");
                        Log.e("Volley", error.toString());
                    }
                }
        );

        requestQueue.add(arrReq);
    }

    public void getPhonesClicked(View v) {

        clearPhoneList();
        getPhoneList();
    }

    public void getPhonesDBClicked(View v) {

        clearPhoneList();
        getPhoneListDB();
        Log.d("DB", blockList.toString());
    }

    public void getPhoneListDB(){
        blockList.clear();
        sqlite_obj.open();
        Cursor c = sqlite_obj.select();
        if (c.moveToFirst())
        {
            do {
                addToPhoneListDB(c.getInt(0), c.getString(1), c.getString(2));
                blockList.add(c.getString(1));
                Log.d("DB", "STR: " + c.getString(1));
            } while (c.moveToNext());
        } else {
            Log.d("DB", "No data");
        }
        //sqlite_obj.close();
    }
}

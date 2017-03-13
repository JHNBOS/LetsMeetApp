package nl.jhnbos.letsmeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ShowInfoActivity extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    private static final String GET_USER_URL = "http://jhnbos.nl/android/getUser.php";
    private String email;

    //LAYOUT
    private TextView inputFirstName;
    private TextView inputLastName;
    private TextView inputEmail;
    private View viewColor;
    private Button btnReturn;

    //OBJECTS
    private User user;
    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);

        //create the delegate
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);

        //use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_show_info);

        //add the Toolbar
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Intent get extra
        email = this.getIntent().getStringExtra("Email");

        //User
        user = new User();

        //Instantiating variables
        inputFirstName = (TextView) findViewById(R.id.input_infoFirstName);
        inputLastName = (TextView) findViewById(R.id.input_infoLastName);
        inputEmail = (TextView) findViewById(R.id.input_infoEmail);
        viewColor = (View) findViewById(R.id.view_color);
        btnReturn = (Button) findViewById(R.id.btn_return);

        //Listener
        btnReturn.setOnClickListener(this);

        //GetUser
        getUserJSON getUserJSON = null;

        try {
            getUserJSON = new getUserJSON();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getUserJSON.execute();
    }

    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                Intent intent = new Intent(ShowInfoActivity.this, MainActivity.class);
                intent.putExtra("Email", email);

                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnReturn) {
            Intent intent = new Intent(ShowInfoActivity.this, MainActivity.class);
            intent.putExtra("Email", email);

            startActivity(intent);
        }
    }


    //END OF LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    //INITIALIZE USER
    private void initUser(String response) {
        try {
            JSONArray jArray = new JSONArray(response);
            JSONArray ja = jArray.getJSONArray(0);

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);

                user.setID(jo.getInt("id"));
                user.setFirstName(jo.getString("first_name"));
                user.setLastName(jo.getString("last_name"));
                user.setPassword(jo.getString("password"));
                user.setEmail(jo.getString("email"));
                user.setColor(jo.getString("color"));

            }

            inputFirstName.setText(user.getFirstName());
            inputLastName.setText(user.getLastName());
            inputEmail.setText(user.getEmail());

            String color = "#" + user.getColor();
            int colorInt = Color.parseColor(color);

            viewColor.setBackgroundColor(colorInt);

            //Set non editable
            inputFirstName.setEnabled(false);
            inputLastName.setEnabled(false);
            inputEmail.setEnabled(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    //GET USER
    private class getUserJSON extends AsyncTask<Void, Void, String> {
        String url = GET_USER_URL + "?email='" + URLEncoder.encode(email, "UTF-8") + "'";
        ProgressDialog loading;

        private getUserJSON() throws UnsupportedEncodingException {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = new ProgressDialog(ShowInfoActivity.this, R.style.AppTheme_Dialog);
            loading.setIndeterminate(true);
            loading.setMessage("Retrieving User...");
            loading.show();
        }

        @Override
        protected String doInBackground(Void... v) {
            RequestHandler rh = new RequestHandler();
            String res = rh.sendGetRequest(url);
            return res;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loading.dismiss();

            initUser(s);
        }
    }


    //END OF METHODS


}

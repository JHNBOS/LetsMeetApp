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
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import yuku.ambilwarna.AmbilWarnaDialog;

public class SettingsActivity extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    private static final String USER_UPDATE_URL = "http://jhnbos.nl/android/updateUser.php";
    private static final String GET_USER_URL = "http://jhnbos.nl/android/getUser.php";
    private String email;
    private String chosenColor;

    //LAYOUT
    private EditText inputNewPassword;
    private EditText inputFirstName;
    private EditText inputLastName;
    private View viewColor;
    private Button btnColorPicker;
    private Button btnUpdate;

    //INTEGERS
    private int currentColor;

    //OBJECTS
    private User user;
    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //create the delegate
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);

        //use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_settings);

        //add the Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //INITIALIZING VARIABLES
        email = getIntent().getStringExtra("Email");
        user = new User();
        chosenColor = "";

        inputNewPassword = (EditText) findViewById(R.id.input_newPassword);
        inputFirstName = (EditText) findViewById(R.id.input_sfirstName);
        inputLastName = (EditText) findViewById(R.id.input_slastName);
        viewColor = (View) findViewById(R.id.sview_color);
        btnColorPicker = (Button) findViewById(R.id.btn_newColor);
        btnUpdate = (Button) findViewById(R.id.btn_changeSettings);

        //LISTENERS
        btnColorPicker.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

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
    public void onClick(View v) {
        if (v == btnColorPicker) {
            openDialog(false);
        }

        if (v == btnUpdate) {
            runUpdate();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("Email", email);

                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //END OF LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    private void updateUser(final String url, final HashMap<String, String> parameters) {
        class GetJSON extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(SettingsActivity.this, R.style.AppTheme_Dialog);
                loading.setIndeterminate(true);
                loading.setMessage("Updating User...");
                loading.show();
            }

            @Override
            protected String doInBackground(Void... v) {
                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(url, parameters);
                return res;

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();

                if (!s.equals(user.getEmail())) {
                    Toast.makeText(SettingsActivity.this, s, Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.putExtra("Email", email);

                    Toast.makeText(SettingsActivity.this, "Updated user!", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }

            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }

    private void openDialog(boolean supportsAlpha) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, currentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                currentColor = color;
                String numbers = String.format("%x", color);

                String hex = numbers.substring(Math.max(0, numbers.length() - 6));

                chosenColor = hex.toUpperCase();
                viewColor.setBackgroundColor(Color.parseColor("#" + chosenColor));
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }
        });
        dialog.show();
    }

    //Run updateUser
    private void runUpdate() {
        String email = user.getEmail();
        int id = user.getID();

        if ((chosenColor == "" || chosenColor.isEmpty())
                && (inputNewPassword.getText().toString() == "" || inputNewPassword.getText().toString().isEmpty())
                && (inputFirstName.getText().toString() == "" || inputFirstName.getText().toString().isEmpty())
                && (inputLastName.getText().toString() == "" || inputLastName.getText().toString().isEmpty())) {
            Toast.makeText(getApplicationContext(), "Please fill in a field to change!", Toast.LENGTH_SHORT).show();
        } else {
            String color = null;
            String password = null;
            String fname = null;
            String lname = null;

            if (chosenColor.toString() == "" || chosenColor.toString().isEmpty()) {
                color = user.getColor();
            } else {
                color = chosenColor.toString();
            }

            if ((inputNewPassword.getText().toString().isEmpty() || inputNewPassword.getText().toString() == "")) {
                password = user.getPassword();

            } else {
                password = inputNewPassword.getText().toString();
            }

            if ((inputFirstName.getText().toString().isEmpty() || inputFirstName.getText().toString() == "")) {
                fname = user.getFirstName();

            } else {
                fname = inputFirstName.getText().toString();
            }

            if ((inputLastName.getText().toString().isEmpty() || inputLastName.getText().toString() == "")) {
                lname = user.getLastName();

            } else {
                lname = inputLastName.getText().toString();
            }

            String suffix = null;

            try {
                suffix = "?first_name='" + URLEncoder.encode(fname, "UTF-8") + "'"
                        + "&last_name='" + URLEncoder.encode(lname, "UTF-8") + "'"
                        + "&color='" + URLEncoder.encode(color, "UTF-8") + "'"
                        + "&password='" + URLEncoder.encode(password, "UTF-8") + "'"
                        + "&email='" + URLEncoder.encode(email, "UTF-8") + "'"
                        + "&id='" + URLEncoder.encode(String.valueOf(id), "UTF-8") + "'";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String cURL = USER_UPDATE_URL + suffix;

            final HashMap<String, String> parameter = new HashMap<>();
            parameter.put("first_name", fname);
            parameter.put("last_name", lname);
            parameter.put("color", color);
            parameter.put("password", password);
            parameter.put("email", email);
            parameter.put("id", String.valueOf(id));

            updateUser(cURL, parameter);
        }

    }

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
            inputNewPassword.setText(user.getPassword());

            String color = "#" + user.getColor();
            int colorInt = Color.parseColor(color);

            viewColor.setBackgroundColor(colorInt);

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
            loading = new ProgressDialog(SettingsActivity.this, R.style.AppTheme_Dialog);
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

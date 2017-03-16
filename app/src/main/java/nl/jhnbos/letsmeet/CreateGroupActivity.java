package nl.jhnbos.letsmeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateGroupActivity extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    private static final String ADDGROUP_URL = "http://jhnbos.nl/android/addGroup.php";
    private static final String ADDGROUPMEMBER_URL = "http://jhnbos.nl/android/addGroupMember.php";

    //OBJECTS
    public ArrayList<String> controlList;
    private String currentUser;
    private String groupName;
    private HTTP http;
    private AppCompatDelegate delegate;

    //LAYOUT
    private EditText inputName;
    private Button btnCreateGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        //USE DELEGATE FOR TOOLBAR
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_create_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //INITIALIZING VARIABLES
        controlList = new ArrayList<>();
        currentUser = getIntent().getStringExtra("Email");
        http = new HTTP();
        inputName = (EditText) findViewById(R.id.input_group);
        btnCreateGroup = (Button) findViewById(R.id.btn_createGroup);

        //LISTENERS
        btnCreateGroup.setOnClickListener(this);
    }

    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

    @Override
    public void onClick(View v) {
        //WHEN CLICKED ON CREATE BUTTON
        if (v == btnCreateGroup) {

            groupName = inputName.getText().toString();
            String url1 = null;
            String url2 = null;

            try {
                url1 = ADDGROUP_URL + "?name=" + URLEncoder.encode(groupName, "UTF-8")
                        + "&email=" + URLEncoder.encode(currentUser, "UTF-8");

                url2 = ADDGROUPMEMBER_URL + "?name=" + URLEncoder.encode(groupName, "UTF-8")
                        + "&email=" + URLEncoder.encode(currentUser, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            try {
                if (groupName == "" || groupName.isEmpty()) {
                    Toast.makeText(CreateGroupActivity.this, "Please fill in a name for the group!", Toast.LENGTH_LONG).show();
                } else {
                    addGroup(url1, url2, groupName, currentUser);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //END OF LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    private void addGroup(final String url, final String url2, final String group, final String email) {
        //ADD GROUP TO THE DATABASE
        class GetJSON extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(CreateGroupActivity.this, R.style.AppTheme_Dialog);
                loading.setIndeterminate(true);
                loading.setMessage("Creating Group...");
                loading.show();
            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String, String> params = new HashMap<>();
                params.put("name", group);
                params.put("email", email);

                HashMap<String, String> params2 = new HashMap<>();
                params2.put("name", group);
                params2.put("email", email);

                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(url, params);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                RequestHandler rh2 = new RequestHandler();
                String res2 = rh2.sendPostRequest(url2, params2);

                return res;

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();

                if (!s.equals(currentUser)) {
                    Toast.makeText(CreateGroupActivity.this, s, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Group Created!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateGroupActivity.this, MainActivity.class);
                    intent.putExtra("Email", email);

                    startActivity(intent);
                    finish();
                }

            }
        }

        GetJSON gj = new GetJSON();
        gj.execute();
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

    //END OF METHODS
}
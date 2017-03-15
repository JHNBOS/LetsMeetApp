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
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class AddMemberActivity extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    public static final String ADD_GROUPMEMBER_URL = "http://jhnbos.nl/android/addGroupMember.php";
    public static final String GET_ALL_CONTACTS_URL = "http://jhnbos.nl/android/getAllNonGroupMembers.php";
    private String group;
    private String email;

    //LISTS
    private ArrayList<String> contactsList;
    private ArrayList<String> selectedList;

    //LAYOUT
    private ListView listView;
    private Button btnAddMember;

    //OBJECTS
    private ArrayAdapter<String> adapter;
    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        //USE DELEGATE FOR TOOLBAR
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_add_member);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //INITIALIZING VARIABLES
        group = this.getIntent().getStringExtra("Group");
        email = this.getIntent().getStringExtra("Email");
        listView = (ListView) findViewById(R.id.gmlist);
        btnAddMember = (Button) findViewById(R.id.btnAddMember);
        contactsList = new ArrayList<>();
        selectedList = new ArrayList<>();

        //LISTENERS
        btnAddMember.setOnClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //ADAPTER
        adapter = new ArrayAdapter<String>(this, R.layout.list_item_multiple, contactsList);
    }

    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

    @Override
    public void onClick(View v) {
        if (v == btnAddMember) {
            try {

                SparseBooleanArray checked = listView.getCheckedItemPositions();

                for (int i = 0; i < checked.size(); i++) {
                    int position = checked.keyAt(i);

                    if (checked.valueAt(i)) {
                        selectedList.add(((TextView) listView.getChildAt(position)).getText().toString());
                    }
                }

                for (int i = 0; i < selectedList.size(); i++) {
                    String cEmail = selectedList.get(i);
                    String url = ADD_GROUPMEMBER_URL + "?name=" + URLEncoder.encode(group, "UTF-8")
                            + "&email=" + URLEncoder.encode(cEmail, "UTF-8");

                    addGroupMember(url, group, cEmail);
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
                // app icon in action bar clicked; goto parent activity.
                super.onBackPressed();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String url1 = null;

        try {
            url1 = GET_ALL_CONTACTS_URL + "?email='" + URLEncoder.encode(email, "UTF-8") + "'";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getContacts(url1);

        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    //END OF LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    private void showContacts(String response) {
        try {
            JSONArray jArray = new JSONArray(response);
            JSONArray ja = jArray.getJSONArray(0);

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);

                contactsList.add(jo.getString("contact_email"));
            }

            listView.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //ADD GROUPMEMBER(S)
    private void addGroupMember(final String url, final String group, final String cEmail) {
        class GetJSON extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(AddMemberActivity.this, R.style.AppTheme_Dialog);
                loading.setIndeterminate(true);
                loading.setMessage("Adding Groupmember(s)...");
                loading.show();
            }

            @Override
            protected String doInBackground(Void... v) {

                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("name", group);
                parameters.put("email", cEmail);

                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(url, parameters);

                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();

                if (!s.contains(group)) {
                    Toast.makeText(AddMemberActivity.this, s, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddMemberActivity.this, "Member Added!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddMemberActivity.this, MainActivity.class);
                    intent.putExtra("Email", email);

                    startActivity(intent);
                    finish();
                }

            }
        }

        GetJSON gj = new GetJSON();
        gj.execute();
    }

    //GET CONTACTS
    private void getContacts(final String url) {
        class GetJSON extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(AddMemberActivity.this, R.style.AppTheme_Dialog);
                loading.setIndeterminate(true);
                loading.setMessage("Retrieving Contacts...");
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

                contactsList.clear();
                showContacts(s);
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

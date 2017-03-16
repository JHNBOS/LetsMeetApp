package nl.jhnbos.letsmeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ShowMembersActivity extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    private static final String GET_ALL_MEMBERS_URL = "http://jhnbos.nl/android/getFullMembers.php";
    private static final String DELETE_GROUPMEMBER_URL = "http://jhnbos.nl/android/deleteGroupMember.php";
    private static final String GET_GROUP_URL = "http://jhnbos.nl/android/getGroup.php";
    private String group;
    private String currentUser;

    //LISTS
    private ArrayList<User> memberList;

    //LAYOUT
    private ListView lv;
    private Button returnButton;

    //OBJECTS
    private ArrayAdapter<User> adapter;
    private StringRequest stringRequest1;
    private HTTP http;
    private AppCompatDelegate delegate;
    private Group currentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_members);

        //USE DELEGATE FOR TOOLBAR
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_show_members);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //GET STRINGS FROM INTENT
        group = this.getIntent().getStringExtra("Group");
        currentUser = this.getIntent().getStringExtra("Email");

        //INITIALIZING VARIABLES
        lv = (ListView) findViewById(R.id.memberlistView);
        returnButton = (Button) findViewById(R.id.btn_mreturn);
        memberList = new ArrayList<User>();
        currentGroup = new Group();
        http = new HTTP();

        //ADAPTER
        adapter = new ArrayAdapter(ShowMembersActivity.this, R.layout.list_item_twoline, android.R.id.text1, memberList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(memberList.get(position).getFirstName() + " " + memberList.get(position).getLastName());
                text2.setText(memberList.get(position).getEmail());

                return view;
            }

        };

        //LISTENERS
        returnButton.setOnClickListener(this);
        registerForContextMenu(lv);
    }

    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

    @Override
    public void onClick(View v) {
        //WHEN CLICKED ON RETURN BUTTON
        if (v == returnButton) {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String url1 = null;
        String url2 = null;

        try {
            url1 = GET_ALL_MEMBERS_URL + "?name='" + URLEncoder.encode(group, "UTF-8") + "'";
            url2 = GET_GROUP_URL + "?name='" + URLEncoder.encode(group, "UTF-8") + "'";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getData(url1);
        getGroups(url2);

        adapter.clear();
        adapter.notifyDataSetChanged();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.members_context_menu, menu);

        if (!currentGroup.getCreator().equals(currentUser)){
            menu.getItem(0).setVisible(false);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        //WHEN LONG CLICKED ON MEMBER
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                String selected = memberList.get((int) info.id).getEmail();
                ShowDialog(selected);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //END OF LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    public void getData(String url1) {
        //RETRIEVE MEMBER INFO FROM THE DATABASE
        stringRequest1 = new StringRequest(url1, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONArray jArray = new JSONArray(response);
                    JSONArray ja = jArray.getJSONArray(0);

                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = ja.getJSONObject(i);

                        User user = new User();
                        user.setID(jo.getInt("id"));
                        user.setFirstName(jo.getString("first_name"));
                        user.setLastName(jo.getString("last_name"));
                        user.setEmail(jo.getString("email"));
                        user.setColor(jo.getString("color"));

                        memberList.add(user);
                    }

                    lv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ShowMembersActivity.this, "Error while reading from url", Toast.LENGTH_SHORT).show();
            }
        });
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest1);
    }

    private void getGroups(final String url) {
        //RETRIEVE GROUP FROM IN THE DATABASE
        class GetJSON extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(ShowMembersActivity.this, R.style.AppTheme_Dialog);
                loading.setIndeterminate(true);
                loading.setMessage("Retrieving Group...");
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

                showGroups(s);
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }

    private void showGroups(String response) {
        //INITIALIZE GROUP
        try {
            JSONArray jArray = new JSONArray(response);
            JSONArray ja = jArray.getJSONArray(0);

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);

                currentGroup.setID(jo.getInt("id"));
                currentGroup.setName(jo.getString("name"));
                currentGroup.setCreator(jo.getString("creator"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void ShowDialog(final String data) {
        //SHOW AN ALERTDIALOG WHEN REMOVING A MEMBER
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowMembersActivity.this);
        builder.setTitle("Remove Member?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                removeGroupMember(data);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeGroupMember(String email) {
        //REMOVE SELECTED GROUPMEMBER FROM THE DATABASE
        try {
            http.sendPost(DELETE_GROUPMEMBER_URL + "?name='" + URLEncoder.encode(group, "UTF-8")
                    + "'&email='" + URLEncoder.encode(email, "UTF-8") + "'");
            onResume();
        } catch (Exception e) {
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

    //END OF METHODS
}

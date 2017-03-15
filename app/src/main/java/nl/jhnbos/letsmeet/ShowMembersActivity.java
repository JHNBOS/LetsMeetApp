package nl.jhnbos.letsmeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowMembersActivity extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    private static final String GET_ALL_MEMBERS_URL = "http://jhnbos.nl/android/getFullMembers.php";
    private static final String DELETE_GROUPMEMBER_URL = "http://jhnbos.nl/android/deleteGroupMember.php";
    private String group;

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

    private Boolean done = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_members);

        //create the delegate
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);

        //use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_show_members);

        //add the Toolbar
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Instantiating variables
        group = this.getIntent().getStringExtra("Group");
        lv = (ListView) findViewById(R.id.memberlistView);
        returnButton = (Button) findViewById(R.id.btn_mreturn);
        memberList = new ArrayList<User>();

        http = new HTTP();

        //ADAPTER
        adapter = new ArrayAdapter(ShowMembersActivity.this, R.layout.list_item_twoline, android.R.id.text1, memberList){
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

        //Listeners
        returnButton.setOnClickListener(this);
        registerForContextMenu(lv);
    }

    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

    @Override
    public void onClick(View v) {
        if (v == returnButton) {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String url1 = null;

        try {
            url1 = GET_ALL_MEMBERS_URL + "?name='" + URLEncoder.encode(group, "UTF-8") + "'";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getData(url1);

        adapter.clear();
        adapter.notifyDataSetChanged();
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.members_context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
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


    //SHOW DIALOG WHEN DELETING GROUP
    private void ShowDialog(final String data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowMembersActivity.this);
        builder.setTitle("Remove Member?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                //dialog.dismiss();
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

    //REMOVE GROUPMEMBER
    private void removeGroupMember(String email) {
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

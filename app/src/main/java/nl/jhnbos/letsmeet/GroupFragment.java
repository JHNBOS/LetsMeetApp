package nl.jhnbos.letsmeet;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    //STRINGS
    public static final String GET_ALL_GROUPS_URL = "http://jhnbos.nl/android/getAllGroups.php";
    public static final String DELETE_EVENTS_URL = "http://jhnbos.nl/android/deleteEvents.php";
    public static final String DELETE_GROUP_URL = "http://jhnbos.nl/android/deleteGroup.php";
    public static final String DELETE_GROUPMEMBERS_URL = "http://jhnbos.nl/android/deleteGroupMembers.php";
    public static final String DELETE_GROUPMEMBER_URL = "http://jhnbos.nl/android/deleteGroupMember.php";
    private static final String GET_USER_URL = "http://jhnbos.nl/android/getUser.php";
    private String email;

    //LISTS
    public ArrayList<String> groupsList;
    public ArrayList<String> memberList;
    public HashMap<String, String> controlList;

    //LAYOUT
    public ListView lv;
    public Button createGroup;

    //OBJECTS
    public ArrayAdapter<String> adapter;
    public User user;
    private HTTP http;

    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout rl = (LinearLayout) inflater.inflate(R.layout.fragment_group, container, false);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Instantiating variables
        email = getActivity().getIntent().getStringExtra("Email");
        lv = (ListView) rl.findViewById(R.id.glist);
        createGroup = (Button) rl.findViewById(R.id.createGroupButton);

        groupsList = new ArrayList<>();
        controlList = new HashMap<>();
        user = new User();

        http = new HTTP();

        //Listeners
        createGroup.setOnClickListener(this);
        lv.setOnItemLongClickListener(this);
        lv.setOnItemClickListener(this);
        lv.setLongClickable(true);

        registerForContextMenu(lv);

        //ADAPTER
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, groupsList);

        // Inflate the layout for this fragment
        return rl;
    }


    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    //SHOW DIALOG WHEN DELETING GROUP
    private void ShowDialog(final String data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle("Remove Group?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                //dialog.dismiss();
                removeGroup(data);

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

    //SHOW GROUPS IN LISTVIEW
    private void showGroups(String response) {
        try {
            JSONArray jArray = new JSONArray(response);
            JSONArray ja = jArray.getJSONArray(0);

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);

                groupsList.add(jo.getString("name"));
                controlList.put(jo.getString("name"), jo.getString("creator"));
            }

            lv.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //REMOVE GROUP
    private void removeGroup(String group) {
        try {
            for (Map.Entry<String, String> entry : controlList.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();

                if (value != email) {
                    http.sendPost(DELETE_GROUP_URL + "?name='" + URLEncoder.encode(group, "UTF-8") + "'");
                    removeGroupMembers(group);
                } else {
                    removeGroupMember(group, email);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //REMOVE GROUPMEMBERS
    private void removeGroupMembers(String group) {
        try {
            http.sendPost(DELETE_GROUPMEMBERS_URL + "?name='" + URLEncoder.encode(group, "UTF-8") + "'");
            removeEvents(group);
            onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //REMOVE GROUPMEMBER
    private void removeGroupMember(String group, String email) {
        try {
            http.sendPost(DELETE_GROUPMEMBER_URL + "?name='" + URLEncoder.encode(group, "UTF-8")
                    + "'&email='" + URLEncoder.encode(email, "UTF-8") + "'");
            onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //REMOVE EVENTS
    private void removeEvents(String group) {
        try {
            http.sendPost(DELETE_EVENTS_URL + "?group_name='" + URLEncoder.encode(group, "UTF-8") + "'");
            onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //GET GROUPS
    private void getGroups(final String url) {
        class GetJSON extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(getActivity(), R.style.AppTheme_Dialog);
                loading.setIndeterminate(true);
                loading.setMessage("Retrieving Groups...");
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

                groupsList.clear();
                showGroups(s);
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }

    //GET GROUPS
    private void getUser(final String url) {
        class GetJSON extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //loading = new ProgressDialog(getActivity(), R.style.AppTheme_Dialog);
                //loading.setIndeterminate(true);
                //loading.setMessage("Retrieving User...");
                //loading.show();
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
                //loading.dismiss();

                initUser(s);
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }


    //END OF METHODS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS
    @Override
    public void onResume() {
        super.onResume();

        String url1 = null;
        String url2 = null;

        try {
            url1 = GET_ALL_GROUPS_URL + "?email='" + URLEncoder.encode(email, "UTF-8") + "'";
            url2 = GET_USER_URL + "?email='" + URLEncoder.encode(email, "UTF-8") + "'";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getGroups(url1);
        getUser(url2);

        adapter.clear();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        if (v == createGroup) {
            Intent createGroupIntent = new Intent(getActivity(), CreateGroupActivity.class);

            createGroupIntent.putExtra("Email", email);
            createGroupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            createGroupIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(createGroupIntent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.group_context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                String selected = groupsList.get((int) info.id);
                ShowDialog(selected);

                return true;
            case R.id.addMember:
                Intent addMemberIntent = new Intent(getActivity(), AddMemberActivity.class);

                addMemberIntent.putExtra("Group", groupsList.get((int) info.id));
                addMemberIntent.putExtra("Email", email);

                startActivity(addMemberIntent);

                return true;
            case R.id.showMember:
                Intent showMemberIntent = new Intent(getActivity(), ShowMembersActivity.class);

                showMemberIntent.putExtra("Group", groupsList.get((int) info.id));

                startActivity(showMemberIntent);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent weekviewIntent = new Intent(getActivity(), Week.class);

        weekviewIntent.putExtra("Group", groupsList.get(position));
        weekviewIntent.putExtra("Email", user.getEmail());

        startActivity(weekviewIntent);
    }

    //END OF LISTENERS

}

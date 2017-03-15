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
import java.util.HashMap;

public class AddContactActivity extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    private static final String ADDCONTACT_URL = "http://jhnbos.nl/android/addContact.php";
    private String currentUser;

    //LAYOUT
    private EditText inputContact;
    private Button btnAddContact;

    //OBJECTS
    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        //USE DELEGATE FOR TOOLBAR
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_add_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //INITIALIZING VARIABLES
        inputContact = (EditText) findViewById(R.id.input_contact);
        btnAddContact = (Button) findViewById(R.id.btn_addContact);
        currentUser = getIntent().getExtras().getString("Email");

        //Listeners
        btnAddContact.setOnClickListener(this);
    }


    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

    @Override
    public void onClick(View v) {
        if (v == btnAddContact) {
            String contactEmail = inputContact.getText().toString();
            String url = null;

            try {
                url = ADDCONTACT_URL + "?name=" + URLEncoder.encode(contactEmail, "UTF-8")
                        + "&email=" + URLEncoder.encode(currentUser, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                if (contactEmail == "" || contactEmail.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a existing email!", Toast.LENGTH_LONG).show();
                } else if (!contactEmail.contains("@") || !contactEmail.contains(".")) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid email!", Toast.LENGTH_LONG).show();
                } else {
                    addContact(url, contactEmail, currentUser);
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

    //END OF LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    //ADD CONTACT
    private void addContact(final String url, final String contact_email, final String email) {
        class GetJSON extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(AddContactActivity.this, R.style.AppTheme_Dialog);
                loading.setIndeterminate(true);
                loading.setMessage("Adding Contact...");
                loading.show();
            }

            @Override
            protected String doInBackground(Void... v) {

                HashMap<String, String> params = new HashMap<>();
                params.put("contact_email", contact_email);
                params.put("email", email);


                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(url, params);
                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();

                if (!s.equals(contact_email)) {
                    Toast.makeText(AddContactActivity.this, s, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddContactActivity.this, "Contact Added!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddContactActivity.this, MainActivity.class);
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

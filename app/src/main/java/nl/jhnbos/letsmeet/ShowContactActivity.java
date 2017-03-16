package nl.jhnbos.letsmeet;

import android.app.Activity;
import android.graphics.Color;
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

public class ShowContactActivity extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    private User contact;

    //LAYOUT
    private EditText inputFirstName;
    private EditText inputLastName;
    private EditText inputEmail;
    private View viewColor;
    private Button btnReturn;

    //OBJECTS
    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_contact);

        //uSE DELEGATE FOR TOOLBAR
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_show_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //GET CONTACT FROM INTENT
        contact = (User) this.getIntent().getSerializableExtra("Contact");

        //INITIALIZING VARIABLES
        inputFirstName = (EditText) findViewById(R.id.input_cinfoFirstName);
        inputLastName = (EditText) findViewById(R.id.input_cinfoLastName);
        inputEmail = (EditText) findViewById(R.id.input_cinfoEmail);
        viewColor = (View) findViewById(R.id.cview_color);
        btnReturn = (Button) findViewById(R.id.btn_creturn);

        //LISTNERS
        btnReturn.setOnClickListener(this);

        //SET TEXT OF TEXTBOXES
        inputFirstName.setText(contact.getFirstName());
        inputLastName.setText(contact.getLastName());
        inputEmail.setText(contact.getEmail());

        //SET BACKGROUND COLOR OF COLORBOX
        String color = "#" + contact.getColor();
        int colorInt = Color.parseColor(color);
        viewColor.setBackgroundColor(colorInt);

        //SET TEXTBOXES TO NON EDITABLE
        inputFirstName.setEnabled(false);
        inputLastName.setEnabled(false);
        inputEmail.setEnabled(false);
    }

    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

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
    public void onClick(View v) {
        super.onBackPressed();
    }


    //END OF LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

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

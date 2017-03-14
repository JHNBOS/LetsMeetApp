package nl.jhnbos.letsmeet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements AppCompatCallback {

    //STRINGS
    private String email;

    //LAYOUT
    private TabLayout tabLayout;

    //OBJECTS
    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create the delegate
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);

        //use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_main);

        //add the Toolbar
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Instantiating variables
        email = this.getIntent().getStringExtra("Email");

        setupTabs();
    }


    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    private void setupTabs() {
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Groups"), 0, true);
        tabLayout.addTab(tabLayout.newTab().setText("Contacts"), 1, false);

        //replace default fragment
        replaceFragment(new GroupFragment());

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onTabTapped(tab.getPosition());

                if (tab.getPosition() == 0) {
                    replaceFragment(new GroupFragment());
                } else if (tab.getPosition() == 1) {
                    replaceFragment(new ContactFragment());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabTapped(tab.getPosition());
            }
        });
    }

    private void onTabTapped(int position) {
        switch (position) {
            case 0:
                // Do something when first tab is tapped here
                break;
            default:
                //Toast.makeText(this, "Tapped " + position, Toast.LENGTH_SHORT);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_main, fragment);

        transaction.commit();
    }

    //END OF METHODS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                settingsIntent.putExtra("Email", email);

                startActivity(settingsIntent);
                return true;
            /*
            case R.id.details:
                Intent infoIntent = new Intent(MainActivity.this, ShowInfoActivity.class);
                infoIntent.putExtra("Email", email);

                startActivity(infoIntent);
                return true;
                */
            case android.R.id.home:
                ShowDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ShowDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle("Logging out...");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                //dialog.dismiss();
                Intent login = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(login);
                finish();
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
    //END OF LISTENERS
}

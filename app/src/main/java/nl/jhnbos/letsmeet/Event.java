package nl.jhnbos.letsmeet;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.net.URLEncoder;
import java.sql.Timestamp;

public class Event extends Activity implements View.OnClickListener, AppCompatCallback {

    //STRINGS
    private static final String ADDEVENT_URL = "http://jhnbos.nl/android/addEvent.php";
    public String event_title;
    public String location;
    public String creator;
    public String group;
    public String color;
    public String name;
    public Timestamp start;
    public Timestamp end;
    private String startDate;
    private String endDate;
    private String ev_loc;
    private String ev_start;
    private String ev_end;
    private String ev_creator;
    private String ev_group;
    private String ev_title;

    //LAYOUT
    private Button btn_createEvent;
    private EditText inputTitle;
    private EditText inputLocation;
    private DatePicker input_startDate;
    private DatePicker input_endDate;
    private TimePicker input_startTime;
    private TimePicker input_endTime;

    //OBJECTS
    private HTTP http;
    private User user;
    private AppCompatDelegate delegate;

    public Event() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        //create the delegate
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);

        //use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_event);

        //add the Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //INITIALIZING VARIABLES
        creator = getIntent().getExtras().getString("EmailC");
        name = getIntent().getExtras().getString("Name");
        user = (User) getIntent().getSerializableExtra("User");
        http = new HTTP();

        inputTitle = (EditText) findViewById(R.id.input_title);
        inputLocation = (EditText) findViewById(R.id.input_location);
        input_startDate = (DatePicker) findViewById(R.id.input_startDate);
        input_endDate = (DatePicker) findViewById(R.id.input_endDate);
        input_startTime = (TimePicker) findViewById(R.id.input_startTime);
        input_endTime = (TimePicker) findViewById(R.id.input_endTime);
        btn_createEvent = (Button) findViewById(R.id.btn_createEvent);

        //SET 24 HOUR
        input_startTime.setIs24HourView(true);
        input_endTime.setIs24HourView(true);

        //LISTNERERS
        btn_createEvent.setOnClickListener(this);
    }


    /*-------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    //ADD GROUP
    private void addEvent() {
        try {

            String response = http.sendPost(
                    ADDEVENT_URL + "?title=" + URLEncoder.encode(ev_title, "UTF-8")
                            + "&loc=" + URLEncoder.encode(ev_loc, "UTF-8")
                            + "&start=" + URLEncoder.encode(ev_start.toString(), "UTF-8")
                            + "&end=" + URLEncoder.encode(ev_end.toString(), "UTF-8")
                            + "&creator=" + URLEncoder.encode(ev_creator, "UTF-8")
                            + "&group=" + URLEncoder.encode(ev_group, "UTF-8")
                            + "&color=" + URLEncoder.encode(user.getColor(), "UTF-8")
            );

            if (!response.equals(ev_title)) {
                Toast.makeText(Event.this, response, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Event.this, response, Toast.LENGTH_SHORT).show();
                super.onBackPressed();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //END OF METHODS
    /*-------------------------------------------------------------------------*/
    //BEGIN OF GETTERS AND SETTERS

    public String getEvent_title() {
        return event_title;
    }

    public void setEvent_title(String event_title) {
        this.event_title = event_title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    //END OF GETTERS AND SETTERS
    /*-------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        //IF PRESSED ON CREATE EVENT BUTTON
        if (v == btn_createEvent) {
            //Start date and time
            int startDay = input_startDate.getDayOfMonth();
            int startMonth = (input_startDate.getMonth() + 1);
            int startYear = input_startDate.getYear();

            int startHour = input_startTime.getHour();
            int startMinute = input_startTime.getMinute();

            //End date and time
            int endDay = input_endDate.getDayOfMonth();
            int endMonth = (input_endDate.getMonth() + 1);
            int endYear = input_endDate.getYear();

            int endHour = input_endTime.getHour();
            int endMinute = input_endTime.getMinute();

            startDate = startYear + "-" + startMonth + "-" + startDay + " " + startHour + ":" + startMinute + ":00";
            endDate = endYear + "-" + endMonth + "-" + endDay + " " + endHour + ":" + endMinute + ":00";

            //Set event info
            ev_loc = inputLocation.getText().toString();
            ev_start = startDate.toString();
            ev_end = endDate.toString();
            ev_creator = getIntent().getExtras().getString("EmailC");
            ev_group = getIntent().getExtras().getString("GroupC");
            ev_title = inputTitle.getText().toString();


            if (ev_title == "" || ev_title.isEmpty()) {
                Toast.makeText(Event.this, "Please fill in all fields!", Toast.LENGTH_LONG).show();
            } else if (startDate == endDate) {
                Toast.makeText(Event.this, "Start and end cannot be the same time!", Toast.LENGTH_LONG).show();
            } else if (startDay == endDay && startMonth == endMonth && startYear == endYear
                    && startHour == endHour && startMinute > endMinute) {
                Toast.makeText(Event.this, "End time cannot be before start time!", Toast.LENGTH_LONG).show();
            } else {
                addEvent();
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

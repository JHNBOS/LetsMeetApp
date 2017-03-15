package nl.jhnbos.letsmeet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
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
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Week extends Activity implements WeekView.ScrollListener, WeekView.EventClickListener, WeekView.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewClickListener, AppCompatCallback {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    //STRINGS
    private static final String GET_EVENTS_URL = "http://jhnbos.nl/android/getAllEvents.php";
    private static final String DELETE_EVENT_URL = "http://jhnbos.nl/android/deleteEvent.php";
    private static final String GET_USER_URL = "http://jhnbos.nl/android/getUser.php";
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;
    private String contact;
    private String group;

    //OBJECTS
    private ArrayList<Event> eventList;
    private List<WeekViewEvent> events;
    private List<WeekViewEvent> matchedEvents;
    private User user;
    private HTTP http;
    private AppCompatDelegate delegate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week);

        //Get objects and strings from intent
        group = getIntent().getExtras().getString("Group");
        user = new User();
        contact = getIntent().getExtras().getString("Email");

        //create the delegate
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);

        //use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_week);

        //add the Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setTitle(group);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ALLOW HTTP
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Lists
        eventList = new ArrayList<>();
        events = new ArrayList<WeekViewEvent>();
        http = new HTTP();

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        //Set empty view listener
        mWeekView.setEmptyViewClickListener(this);

        //Set scroll listener
        mWeekView.setScrollListener(this);

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(true);

        mWeekView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }


    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF LISTENERS

    @Override
    public void onResume() {
        super.onResume();

        GetEventJSON getEventJSON = null;

        try {
            getEventJSON = new GetEventJSON();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        eventList.clear();
        events.clear();

        try {
            getUser(contact);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getEventJSON.execute();

        mWeekView.notifyDatasetChanged();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.week, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setupDateTimeInterpreter(id == R.id.action_week_view);

        switch (id) {
            case R.id.create_event:
                Intent createEvent = new Intent(this, Event.class);
                createEvent.putExtra("User", user);
                createEvent.putExtra("GroupC", group);
                createEvent.putExtra("EmailC", contact);

                startActivity(createEvent);
                return true;
            case R.id.refresh_week:
                startActivity(this.getIntent());

                return true;
            case R.id.action_day_view:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_three_day_view:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_week_view:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    mWeekView.setNumberOfVisibleDays(7);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                }
                return true;
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                super.onBackPressed();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //END OF LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF ANDROID WEEKVIEW LISTENERS

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        showEventInfo(event);
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        ShowDialog(event);
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        //mWeekView.notifyDatasetChanged();
    }

    //Setup date showing
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE");
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat("d/MM");

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                return weekday.toUpperCase() + " " + format.format(date.getTime());
            }


            @Override
            public String interpretTime(int hour) {
                if (hour == 24) {
                    hour = 0;
                }
                if (hour == 0) {
                    hour = 0;
                }
                return hour + ":00";
            }


        });
    }

    //Check if event matches onMonthChange parameters
    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == (month - 1)) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }

    //Check for events when changing month
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        showEvents(newMonth, newYear);

        return matchedEvents;
    }


    public void onFirstVisibleDayChanged(Calendar calendar, Calendar calendar1) {
        //mWeekView.notifyDatasetChanged();
    }


    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }


    public WeekView getWeekView() {
        return mWeekView;
    }

    //END OF ANDROID WEEKVIEW LISTENERS
    /*-----------------------------------------------------------------------------------------------------*/
    //BEGIN OF METHODS

    //SHOW EVENTS
    private void showEvents(int month, int year) {
        int idset = 0;
        int c = 0;

        matchedEvents = new ArrayList<>();

        Calendar startCal = null;
        Calendar endCal = null;

        for (int i = 0; i < eventList.size(); i++) {
            startCal = Calendar.getInstance();
            endCal = (Calendar) startCal.clone();

            String Title = eventList.get(i).getEvent_title();
            String User = eventList.get(i).creator;
            String Location = eventList.get(i).getLocation();
            Timestamp Start = eventList.get(i).getStart();
            Timestamp End = eventList.get(i).getEnd();

            startCal.setTime(Start);
            endCal.setTime(End);

            //+2 is april
            //+1 is march
            //+0 is february => correct
            startCal.set(Calendar.MONTH, (startCal.get(Calendar.MONTH) + 0));
            endCal.set(Calendar.MONTH, (endCal.get(Calendar.MONTH) + 0));

            int Colour = Color.parseColor(eventList.get(i).getColor());

            WeekViewEvent event = new WeekViewEvent(idset++, Title, User, startCal, endCal);
            event.setLocation(Location);
            event.setColor(Colour);

            if (!events.contains(event)) {
                Log.d("Event: ", event.getName());
                events.add(event);
            }

            for (WeekViewEvent we : events) {
                if (eventMatches(we, year, month)) {
                    if (!matchedEvents.contains(we)) {
                        Log.d("we: ", we.getName());
                        matchedEvents.add(we);
                    }
                }
            }

            startCal = null;
            endCal = null;
            event = null;

        }

        mWeekView.notifyDatasetChanged();

    }


    //INITIALIZE USER
    private void initUser(String response) {

    }

    //Get Events From JSON and create new Event object with the data
    private void addEvents(String response) {
        try {
            JSONArray jArray = new JSONArray(response);
            JSONArray ja = jArray.getJSONArray(0);

            Event e = null;
            Date start = null;
            Date end = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                e = new Event();

                try {
                    start = sdf.parse(jo.get("start").toString());
                    end = sdf.parse(jo.get("end").toString());
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }

                e.setEvent_title(jo.getString("title"));
                e.setLocation(jo.getString("location"));
                e.setStart(new Timestamp(start.getTime()));
                e.setEnd(new Timestamp(end.getTime()));
                e.setColor("#" + jo.getString("color"));
                e.setCreator(jo.getString("creator"));

                eventList.add(e);

                e = null;
                start = null;
                end = null;
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    //DELETE EVENT
    private void deleteEventJSON(final WeekViewEvent event) {
        class deleteEvent extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(Week.this, R.style.AppTheme_Dialog);
                loading.setIndeterminate(true);
                loading.setMessage("Removing Event...");
                loading.show();
            }

            @Override
            protected String doInBackground(Void... v) {
                String url = null;

                try {
                    url = DELETE_EVENT_URL
                            + "?title='" + URLEncoder.encode(event.getName(), "UTF-8") + "'";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RequestHandler rh = new RequestHandler();
                HashMap<String, String> params = new HashMap<>();
                params.put("title", event.getName());
                String res = rh.sendPostRequest(url, params);

                matchedEvents.remove(event);

                return res;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                Log.d("s", s);

                matchedEvents = null;

                if (s.equals(event.getName())) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                onResume();
                loading.dismiss();
            }
        }

        deleteEvent de = new deleteEvent();
        de.execute();
        onResume();
    }

    //SHOW DIALOG WHEN DELETING EVENT
    private void ShowDialog(final WeekViewEvent data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Week.this, R.style.AppTheme_Dialog);
        builder.setTitle("Remove Event?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                //dialog.dismiss();
                deleteEventJSON(data);

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

    private void showEventInfo(WeekViewEvent event) {

        User creator = null;
        try {
            creator = getUser(event.getUser().toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Week.this, R.style.AppTheme_Dialog);
        builder.setTitle("Event Info");

        float dpi = this.getResources().getDisplayMetrics().density;

        final TextView input = new TextView(this);
        input.setTextColor(getResources().getColor(R.color.white));
        input.setTextSize(16);

        builder.setView(input, (int) (25 * dpi), (int) (8 * dpi), (int) (14 * dpi), (int) (8 * dpi));

        String sdate = String.valueOf(event.getStartTime().get(Calendar.DAY_OF_MONTH));
        String smonth = String.valueOf(event.getStartTime().get(Calendar.MONTH));
        String syear = String.valueOf(event.getStartTime().get(Calendar.YEAR));
        String shour = String.valueOf(event.getStartTime().get(Calendar.HOUR_OF_DAY));
        String sminute = String.valueOf(event.getStartTime().get(Calendar.MINUTE));

        String edate = String.valueOf(event.getEndTime().get(Calendar.DAY_OF_MONTH));
        String emonth = String.valueOf(event.getEndTime().get(Calendar.MONTH));
        String eyear = String.valueOf(event.getEndTime().get(Calendar.YEAR));
        String ehour = String.valueOf(event.getEndTime().get(Calendar.HOUR_OF_DAY));
        String eminute = String.valueOf(event.getEndTime().get(Calendar.MINUTE));

        if (sdate.length() < 2) {
            sdate = "0" + sdate;
        }

        if (edate.length() < 2) {
            edate = "0" + edate;
        }

        if (smonth.length() < 2) {
            smonth = "0" + smonth;
        }

        if (emonth.length() < 2) {
            emonth = "0" + emonth;
        }

        if (shour.length() < 2) {
            shour = "0" + shour;
        }

        if (ehour.length() < 2) {
            ehour = "0" + ehour;
        }
        if (sminute.length() < 2) {
            sminute = "0" + sminute;
        }

        if (eminute.length() < 2) {
            eminute = "0" + eminute;
        }

        String start = sdate + "-" + smonth + "-" + syear + " " + shour + ":" + sminute;
        String end = edate + "-" + emonth + "-" + eyear + " " + ehour + ":" + eminute;

        String name = creator.getFirstName() + " " + creator.getLastName();

        input.setText("User: " + name + "\n" + "Title: " + event.getName() + "\n" + "Location: "
                + event.getLocation() + "\n" + "Start: " + start + "\n" + "End: " + end);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
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

    //GET USER
    private User getUser(final String email) throws UnsupportedEncodingException {
        class getUserJSON extends AsyncTask<Void, Void, String> {
            String url = GET_USER_URL + "?email='" + URLEncoder.encode(email, "UTF-8") + "'";
            ProgressDialog loading;

            private getUserJSON() throws UnsupportedEncodingException {
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = new ProgressDialog(Week.this, R.style.AppTheme_Dialog);
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

                //initUser(s);
                try {
                    JSONArray jArray = new JSONArray(s);
                    JSONArray ja = jArray.getJSONArray(0);

                    Log.d("User JSONArray", ja.toString());

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
        }

        getUserJSON gu = new getUserJSON();
        gu.execute();

        return user;
    }

    //GET GROUPS
    private class GetEventJSON extends AsyncTask<Void, Void, String> {
        String url = GET_EVENTS_URL + "?group='" + URLEncoder.encode(group, "UTF-8") + "'";
        ProgressDialog loading;

        private GetEventJSON() throws UnsupportedEncodingException {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = new ProgressDialog(Week.this, R.style.AppTheme_Dialog);
            loading.setIndeterminate(true);
            loading.setMessage("Retrieving Events...");
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

            addEvents(s);
        }
    }


    //END OF METHODS


}


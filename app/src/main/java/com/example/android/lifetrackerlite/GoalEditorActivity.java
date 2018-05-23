package com.example.android.lifetrackerlite;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;

import java.util.Calendar;
import java.util.Date;

public class GoalEditorActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentGoalUri;
    private static final int GOAL_EDIT_LOADER = 1;

    private int mGoalType;
    private int mStartYear;
    private int mStartMonth;
    private int mStartDay;

    //Views for DatePicker used for Goal Start Date
    public TextView mDateDisplay;
    private EditText mNameEditText;
    private Spinner mGoalTypeSpinner;
    private Button mPickDate;
    private Button mAddGoal;
    private Button mDeleteGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_editor);

        //Get the intent that created activity to determine if activity should be in "insert mode"
        //for inserting a new goal or "edit mode" for editing an existing goal.
        Intent intent = getIntent();
        mCurrentGoalUri = intent.getData();

        //Find views to read user input from
        mGoalTypeSpinner = (Spinner) findViewById(R.id.spinner_goal_type);
        setupSpinner();

        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mDateDisplay = (TextView) findViewById(R.id.goal_start_date_display);
        mPickDate = (Button) findViewById(R.id.goal_start_date_button);
        mAddGoal = (Button) findViewById(R.id.add_goal_editor);
        mDeleteGoal = (Button) findViewById(R.id.delete_goal_editor);

        if (mCurrentGoalUri == null) {
            setTitle(R.string.add_goal_activity_title);
            mDeleteGoal.setVisibility(View.GONE);
            mAddGoal.setText(R.string.add_goal_button);
        } else {
            setTitle(getString(R.string.edit_goal_activity_title));
            getLoaderManager().initLoader(GOAL_EDIT_LOADER, null, this);
            mDeleteGoal.setVisibility(View.VISIBLE);
            mAddGoal.setText(R.string.save_goal_button);
        }

        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        mAddGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentGoalUri == null) {
                    //Insert Goal when Add Goal button is clicked if in "Insert Mode"
                    insertGoal();
                } else {
                    //Update Gal when Add Goal button is clicked  if in "Edit Mode"
                    updateGoal();
                }
                finish();
            }
        });

        mDeleteGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteGoal();
            }
        });


    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter goalTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_goal_types, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        goalTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGoalTypeSpinner.setAdapter(goalTypeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGoalTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.goal_type_other))) {
                        mGoalType = GoalsHabitsEntry.GOAL_TYPE_OTHER; // Other
                    } else if (selection.equals(getString(R.string.goal_type_fitness))) {
                        mGoalType = GoalsHabitsEntry.GOAL_TYPE_FITNESS; // Fitness
                    } else {
                        mGoalType = GoalsHabitsEntry.GOAL_TYPE_READ; // Read
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGoalType = GoalsHabitsEntry.GOAL_TYPE_OTHER; // Unknown
            }
        });
    }

    public void insertGoal() {

        //TODO insert sanity checks for blank dates
        //Get values from editor entry views
        String nameString = mNameEditText.getText().toString().trim();
        long startDate = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        //Log.d("TestDate", "date unix time: " + startDate);
        ContentValues values = new ContentValues();
        values.put(GoalsHabitsEntry.COLUMN_GOAL_NAME, nameString);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, GoalsHabitsEntry.GOAL);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_TYPE, mGoalType);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, startDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, GoalsHabitsEntry.GOAL_COMPLETED_NO);

        //Insert values into database
        Uri uri = getContentResolver().insert(GoalsHabitsEntry.CONTENT_URI, values);

        Toast.makeText(this, "Goal Inserted", Toast.LENGTH_SHORT).show();

    }

    private void updateGoal() {

        //TODO insert sanity checks for blank dates
        //Get values from editor entry views
        String nameString = mNameEditText.getText().toString().trim();
        long startDate = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        //Log.d("TestDate", "date unix time: " + startDate);
        ContentValues values = new ContentValues();
        values.put(GoalsHabitsEntry.COLUMN_GOAL_NAME, nameString);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, GoalsHabitsEntry.GOAL);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_TYPE, mGoalType);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, startDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, GoalsHabitsEntry.GOAL_COMPLETED_NO);

        int rowsUpdated = getContentResolver().update(mCurrentGoalUri, values, null, null);

        Toast.makeText(this, "Goal Updated", Toast.LENGTH_SHORT).show();
    }

    public void deleteGoal() {
        // If Delete Button is clicked from within Editor Activity, pet is deleted.

        int rowsDeleted = getContentResolver().delete(mCurrentGoalUri, null, null);
        Toast.makeText(this, "Goal Deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                GoalsHabitsEntry._ID,
                GoalsHabitsEntry.COLUMN_GOAL_NAME,
                GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT,
                GoalsHabitsEntry.COLUMN_GOAL_TYPE,
                GoalsHabitsEntry.COLUMN_GOAL_START_DATE,
                GoalsHabitsEntry.COLUMN_GOAL_END_DATE,
                GoalsHabitsEntry.COLUMN_GOAL_COMPLETED};

        return new CursorLoader(this,
                mCurrentGoalUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.getCount() >= 1) {
            while (cursor.moveToNext()) {

                // Load the data from the cursor for the single goal you are editing
                String goalName = cursor.getString(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_NAME));
                int goalType = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_TYPE));
                int startDate = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_START_DATE));

                mNameEditText.setText(goalName, TextView.BufferType.EDITABLE);
                //TODO Determine goal type spinner position... Find a way to remove this code by auomatically determining spinner position
                int goalTypeSpinnerPosition = 0;
                switch (goalType) {
                    case GoalsHabitsEntry.GOAL_TYPE_FITNESS:
                        goalTypeSpinnerPosition = 1;
                        break;
                    case GoalsHabitsEntry.GOAL_TYPE_READ:
                        goalTypeSpinnerPosition = 2;
                        break;
                    default:
                        break;
                }
                mGoalTypeSpinner.setSelection(goalTypeSpinnerPosition);

            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Clear Edit Views upon reset.

        mNameEditText.setText("", TextView.BufferType.EDITABLE);
        mGoalTypeSpinner.setAdapter(null);

    }

    private long dateToUnixTime(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR, 5);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis() / 1000;
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        //Set date display when date set by DatePicker
        mStartYear = year;
        mStartMonth = month;
        mStartDay = day;

        //Add one to month since months indexed starting at 0
        month = month + 1;
        mDateDisplay.setText(year + "-" + month + "-" + day);

    }

    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), (GoalEditorActivity) getActivity(), year, month, day);
        }


    }
}

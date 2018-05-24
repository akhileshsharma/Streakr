package com.example.android.lifetrackerlite;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;

import java.text.SimpleDateFormat;

public class GoalAdapter extends CursorAdapter{
    //Cursor adapter for the goals data

    public GoalAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        //Inflate new views from Goals Habits Item view
        return LayoutInflater.from(context).inflate(R.layout.list_goalshabits_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //Set goal name
        TextView goalNameType = (TextView) view.findViewById(R.id.goal_name);
        String goalNameTypeText = cursor.getString(cursor.getColumnIndexOrThrow(LTContract.GoalsHabitsEntry.COLUMN_GOAL_NAME)) + "\n";
        goalNameTypeText += GoalsHabitsEntry.getGoalTypeString(cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_TYPE)));
        goalNameType.setText(goalNameTypeText);

        //Set goal details string
        TextView goalDetails = (TextView) view.findViewById(R.id.goal_details);
        String goalDetailString = "";
        //Convert unix start date to string and add to details
        long startDate = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_START_DATE)) * 1000;
        SimpleDateFormat startSdf = new SimpleDateFormat("MMMM d, yyyy");
        String startDateString = startSdf.format(startDate);
        goalDetailString += "S: " + startDateString + "\n";
        //Convert unix end date to string and add to details
        long endDate = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_END_DATE)) * 1000;
        SimpleDateFormat endSdf = new SimpleDateFormat("MMMM d, yyyy");
        String endDateString = endSdf.format(startDate);
        goalDetailString += "E: " + endDateString;


        goalDetails.setText(goalDetailString);

        //Set streak length
        long millis = System.currentTimeMillis();
        long streakLengthMillis = millis - startDate;
        long streakLengthDays = streakLengthMillis / (1000*60*60*24);
        TextView streakLength = (TextView) view.findViewById(R.id.streak_length);
        streakLength.setText(Long.toString(streakLengthDays));

    }
}

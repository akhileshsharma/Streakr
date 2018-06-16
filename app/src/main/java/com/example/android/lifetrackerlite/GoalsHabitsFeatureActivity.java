package com.example.android.lifetrackerlite;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.helper.GoalItemTouchHelperCallback;
import com.example.android.lifetrackerlite.helper.ItemTouchHelperAdapter;
import com.example.android.lifetrackerlite.helper.OnStartDragListener;

public class GoalsHabitsFeatureActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, GoalRecyclerAdapter.ListItemClickListener, OnStartDragListener{

    private static final String TAG = GoalsHabitsFeatureActivity.class.getSimpleName();

    private static final int GOALSHABITS_LOADER = 0;

    private RecyclerView mRecyclerView;
    private GoalRecyclerAdapter mAdapter;
    private ItemTouchHelper mItemtouchHelper;
    private Integer mNumberGoals = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals_habits_feature);

        // Add new goal button
        FloatingActionButton addGoalButton = findViewById(R.id.add_goal);
        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //New intent to open the editor activity in "insert mode"
                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);

                // If number of goals  is not fully loaded (i.e. is still the default of -2), return
                // If number of goals is loaded, pass over the data of number of cursor goals to editor activity
                //  This data is used to determine goal order (used for drag and drop functionality)
                if (mNumberGoals == -2) {
                    Toast.makeText(GoalsHabitsFeatureActivity.this, GoalsHabitsFeatureActivity.this.getResources().getString(R.string.please_wait_data_load),
                            Toast.LENGTH_SHORT);
                    return;
                }
                intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_ORDER,  mNumberGoals);

                //Send over an extra int to indicate editing a goal
                intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, GoalsHabitsEntry.GOAL);
                startActivity(intent);

            }
        });

        // Add new habit button
        FloatingActionButton addHabitButton = findViewById(R.id.add_habit);
        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //New intent to open the editor activity in "insert mode"
                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);

                // If number of goals  is not fully loaded (i.e. is still the default of -2), return
                // If number of goals is loaded, pass over the data of number of cursor goals to editor activity
                //  This data is used to determine goal order (used for drag and drop functionality)
                if (mNumberGoals == -2) {
                    Toast.makeText(GoalsHabitsFeatureActivity.this, GoalsHabitsFeatureActivity.this.getResources().getString(R.string.please_wait_data_load),
                            Toast.LENGTH_SHORT);
                    return;
                }
                intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_ORDER,  mNumberGoals);

                //Send over an extra int to indicate editing a habit
                intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, GoalsHabitsEntry.HABIT);
                startActivity(intent);

            }
        });

        // Find recyclerView for goal/habit list and set linearLayoutManager and recyclerAdapter on recyclerView
        mRecyclerView = findViewById(R.id.recycler_view_goals_habits);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(GoalsHabitsFeatureActivity.this));
        mAdapter = new GoalRecyclerAdapter(this, null, this, this);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback =
                new GoalItemTouchHelperCallback(mAdapter);
        mItemtouchHelper = new ItemTouchHelper(callback);
        mItemtouchHelper.attachToRecyclerView(mRecyclerView);

        //Initialize loader for goals data
        getLoaderManager().initLoader(GOALSHABITS_LOADER, null, this);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        //Load all goals data to display

        String[] projection = {
                GoalsHabitsEntry._ID,
                GoalsHabitsEntry.COLUMN_GOAL_ORDER,
                GoalsHabitsEntry.COLUMN_GOAL_NAME,
                GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT,
                GoalsHabitsEntry.COLUMN_GOAL_TYPE,
                GoalsHabitsEntry.COLUMN_GOAL_START_DATE,
                GoalsHabitsEntry.COLUMN_GOAL_END_DATE,
                GoalsHabitsEntry.COLUMN_GOAL_COMPLETED};

        return new CursorLoader(this, GoalsHabitsEntry.CONTENT_URI, projection, null, null, GoalsHabitsEntry.COLUMN_GOAL_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mAdapter.swapCursor(cursor);
        mNumberGoals = cursor.getCount();
        Log.d(TAG, "" + mNumberGoals);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);

    }

    // When a goal/habit item is clicked, open up that goal/habit in "edit mode"
    @Override
    public void onListItemClick(int clickedItemIndex) {

        // New intent to open GoalEditorActivity class for goal clicked
        Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);

        // Set the URI on the intent of the goal clicked to be the id of the clicked item
        // Add 1 to the clickedItemIndex so that URI ID matches correct goal
        Uri currentGoalUri = ContentUris.withAppendedId(GoalsHabitsEntry.CONTENT_URI, clickedItemIndex + 1);

        //Set the data on the intent to be the current URI
        intent.setData(currentGoalUri);

        // Launch the intent to the editor activity to open the activity in "edit mode"
        startActivity(intent);

    }

    // Start dragging the viewHolder when the startDrag callback is received from the GoalRecyclerAdapter
    // The drag is initiated when the view is touched
    //TODO indicate where the view  should be touched for drag to start
    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemtouchHelper.startDrag(viewHolder);
    }

}

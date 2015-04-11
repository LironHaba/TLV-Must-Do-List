package com.shenkar.mobile.linitudo;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.share.model.ShareContent;
import com.facebook.share.widget.ShareButton;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.shenkar.mobile.linitudo.db.TaskContract;
import com.shenkar.mobile.linitudo.db.TaskDBHelper;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import com.facebook.FacebookSdk;

public class MainActivity extends ListActivity {
    private TaskDBHelper helper;
    private ListAdapter listAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        //Get a Tracker (should auto-report)
        ((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);

        updateUI();

    }

    private void updateUI() {
        helper = new TaskDBHelper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getReadableDatabase();
        Cursor cursor = sqlDB.query(TaskContract.TABLE,
                new String[]{TaskContract.Columns._ID, TaskContract.Columns.TASK, TaskContract.Columns.DATE},
                null,null,null,null,null);

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.task_view,
                cursor,
                new String[] { TaskContract.Columns.TASK},
                new int[] { R.id.taskTextView},
                0
        );
        this.setListAdapter(listAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onDoneButtonClick(View view) {
        View v = (View) view.getParent();
        TextView taskTextView = (TextView) v.findViewById(R.id.taskTextView);
        String task = taskTextView.getText().toString();

        String sql = String.format("DELETE FROM %s WHERE %s = '%s'",
                TaskContract.TABLE,
                TaskContract.Columns.TASK,
                task);


        helper = new TaskDBHelper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        sqlDB.execSQL(sql);
        updateUI();
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Add a task");
//                builder.setMessage("What do you want to do?");
                //final EditText inputField = new EditText(this);
//                builder.setView(inputField);
                //LayoutInflater inflater = getLayoutInflater();
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogView = inflater.inflate(R.layout.dialog, null);
                builder.setView(dialogView);
                final EditText date = (EditText) dialogView.findViewById(R.id.date);
                final EditText task = (EditText) dialogView.findViewById(R.id.task);
                final Spinner location = (Spinner) dialogView.findViewById(R.id.location);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String taskData = task.getText().toString();
                        String locationData = location.getSelectedItem().toString();

                        Log.d("MainActivity", taskData);


                        helper = new TaskDBHelper(MainActivity.this);
                        SQLiteDatabase db = helper.getWritableDatabase();
                        ContentValues values = new ContentValues();

                        values.clear();
                        values.put(TaskContract.Columns.TASK, taskData);
                        values.put(TaskContract.Columns.DATE, date.getText().toString());
                        values.put(TaskContract.Columns.LOCATION, locationData);

                        db.insertWithOnConflict(TaskContract.TABLE, null, values,
                                SQLiteDatabase.CONFLICT_IGNORE);

                        updateUI();
                    }
                });

                builder.setNegativeButton("Cancel",null);

                final Activity currentActivity = this;
                date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                            @Override
                                            public void onFocusChange(View v, boolean focus) {
                                                final Calendar c = Calendar.getInstance();
                                                int mYear = c.get(Calendar.YEAR);
                                                int mMonth = c.get(Calendar.MONTH);
                                                int mDay = c.get(Calendar.DAY_OF_MONTH);

                                                if (focus) {
                                                    DatePickerDialog dpd = new DatePickerDialog(currentActivity,
                                                            new DatePickerDialog.OnDateSetListener() {

                                                                @Override
                                                                public void onDateSet(DatePicker view, int year,
                                                                                      int monthOfYear, int dayOfMonth) {
                                                                    date.setText(dayOfMonth + "-"
                                                                            + (monthOfYear + 1) + "-" + year);

                                                                }
                                                            }, mYear, mMonth, mDay);
                                                    dpd.show();
                                                }
                                            }
                                        }
                );


                AlertDialog dialog = builder.create();
                dialog.show();
                return true;


            default:
                return false;
        }
    }

    public void onEditButtonClick(View view) {
        View v = (View) view.getParent();
        TextView taskTextView = (TextView) v.findViewById(R.id.taskTextView);
        helper = new TaskDBHelper(MainActivity.this);

        final String taskText = taskTextView.getText().toString();

        SQLiteDatabase sqlDB = helper.getWritableDatabase();

        String whereClause = TaskContract.Columns.DATE + " = ?";
        String[] whereArgs = new String[] {
                taskText
        };

        Cursor cursor = sqlDB.query(TaskContract.TABLE,
                new String[]{TaskContract.Columns._ID, TaskContract.Columns.TASK, TaskContract.Columns.DATE, TaskContract.Columns.LOCATION},
                "",null,null,null,null);
        cursor.moveToFirst();
        String dateText = cursor.getString(cursor.getColumnIndex(TaskContract.Columns.DATE));
        String locationText = cursor.getString(cursor.getColumnIndex(TaskContract.Columns.LOCATION));
        cursor.close();
        //TextView dateTextView = (TextView) v.findViewById(R.id.date);
        //final String dateText = "";//dateTextView.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog, null);
        builder.setTitle("Edit a task");
        final EditText inputField = (EditText) dialogView.findViewById(R.id.task);
        final EditText dateField = (EditText) dialogView.findViewById(R.id.date);
        final Spinner locationField = (Spinner) dialogView.findViewById(R.id.location);
        inputField.setText(taskText);
        dateField.setText(dateText);
        for (int i=0;i < locationField.getCount();i++) {
            if (locationField.getItemAtPosition(i).toString().contentEquals(locationText)) {
                locationField.setSelection(i);
            }
        }

        builder.setView(dialogView);

        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String sql = String.format("UPDATE %s SET %s = '%s', %s = '%s' , %s = '%s' WHERE %s = '%s'",
                        TaskContract.TABLE,
                        TaskContract.Columns.TASK,
                        inputField.getText().toString(),
                        TaskContract.Columns.DATE,
                        dateField.getText().toString(),
                        TaskContract.Columns.LOCATION,
                        locationField.getSelectedItem().toString(),
                        TaskContract.Columns.TASK,
                        taskText);


                helper = new TaskDBHelper(MainActivity.this);
                SQLiteDatabase sqlDB = helper.getWritableDatabase();
                sqlDB.execSQL(sql);
                updateUI();
            }
        });

        builder.setNegativeButton("Cancel",null);
        builder.create().show();
    }



    @Override
    public void onStart()
    {
        super.onStart();
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }


}

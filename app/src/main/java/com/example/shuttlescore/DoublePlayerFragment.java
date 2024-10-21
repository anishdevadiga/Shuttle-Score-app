package com.example.shuttlescore;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DoublePlayerFragment extends Fragment {

    private EditText dateEditText, timeEditText;
    private EditText team1player1NameEditText,team1player2NameEditText,team2player1NameEditText,team2player2NameEditText; // New fields for player names
    private CheckBox readSystemTimeCheckBox;
    private Button addButton,startButton;
    private DatabaseReference databaseReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.double_player_fragment, container, false);

        dateEditText = view.findViewById(R.id.dateEditText2); // Date field
        timeEditText = view.findViewById(R.id.timeEditText2); // Time field
        team1player1NameEditText = view.findViewById(R.id.team1_player1); // Player 1 name field
        team1player2NameEditText = view.findViewById(R.id.team1_player2); // Player 2 name field
        team2player1NameEditText = view.findViewById(R.id.team2_player1); // Player 1 name field
        team2player2NameEditText = view.findViewById(R.id.team2_player2); // Player 2 name field
        readSystemTimeCheckBox = view.findViewById(R.id.read_system_time); // Checkbox for system date/time
        addButton = view.findViewById(R.id.add_button); // Add button
        startButton = view.findViewById(R.id.start_button);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("matches");

        // Date Picker
        dateEditText.setOnClickListener(v -> showDatePicker());

        // Time Picker
        timeEditText.setOnClickListener(v -> showTimePicker());

        // Listen for checkbox changes
        readSystemTimeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Get current date and time from system
                setCurrentDateTime();
            } else {
                // Clear the date and time fields if checkbox is unchecked
                dateEditText.setText("");
                timeEditText.setText("");
            }
        });

        // Save data when Add button is clicked
        addButton.setOnClickListener(v -> {
            String date = dateEditText.getText().toString().trim();
            String time = timeEditText.getText().toString().trim();
            String team1player1 = team1player1NameEditText.getText().toString().trim();
            String team1player2 = team1player2NameEditText.getText().toString().trim();
            String team2player1 = team2player1NameEditText.getText().toString().trim();
            String team2player2 = team2player2NameEditText.getText().toString().trim();
            saveDataToFirebase();
            clearFields();
            scheduleNotification(date,time,team1player1,team1player2,team2player1,team2player2); });

        // Save data when Start button is clicked
        startButton.setOnClickListener(v -> {
            // Assuming you have EditText fields to get player names
            String player1Name = team1player1NameEditText.getText().toString().trim();
            String player2Name = team2player1NameEditText.getText().toString().trim();

            // Create an intent to move to the layout_java activity (where the scoring interface is)
            Intent intent = new Intent(getActivity(), layout_java.class);

            // Pass player names as extras in the intent
            intent.putExtra("player1Name", player1Name);
            intent.putExtra("player2Name", player2Name);

            // Start the new activity
            startActivity(intent);

            saveDataToFirebase();
            clearFields();
        });

        return view;
    }

    // Method to show Date Picker Dialog
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    dateEditText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    // Method to show Time Picker Dialog
    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute1) -> {
                    String selectedTime = hourOfDay + ":" + (minute1 < 10 ? "0" + minute1 : minute1);
                    timeEditText.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    // Method to set current date and time from the system
    private void setCurrentDateTime() {
        final Calendar calendar = Calendar.getInstance();

        // Get the current date
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Months are indexed from 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String currentDate = day + "/" + month + "/" + year;
        dateEditText.setText(currentDate);

        // Get the current time
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String currentTime = hour + ":" + (minute < 10 ? "0" + minute : minute);
        timeEditText.setText(currentTime);
    }

    // Method to save data to Firebase Realtime Database
    private void saveDataToFirebase() {
        String date = dateEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();
        String team1player1Name = team1player1NameEditText.getText().toString().trim();
        String team1player2Name = team1player2NameEditText.getText().toString().trim();
        String team2player1Name = team2player1NameEditText.getText().toString().trim();
        String team2player2Name = team2player2NameEditText.getText().toString().trim();
        boolean useSystemTime = readSystemTimeCheckBox.isChecked();

        if (date.isEmpty() || time.isEmpty() || team1player1Name.isEmpty() || team1player2Name.isEmpty() || team2player1Name.isEmpty() || team2player2Name.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new match object to save
        Map<String, Object> matchData = new HashMap<>();
        matchData.put("date", date);
        matchData.put("time", time);
        matchData.put("team1player1Name", team1player1Name);
        matchData.put("team1player2Name", team1player2Name);
        matchData.put("team2player1Name", team2player1Name);
        matchData.put("team2player2Name", team2player2Name);
        matchData.put("useSystemTime", useSystemTime);

        // Save data to Firebase under a unique key
        databaseReference.push().setValue(matchData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to schedule notification using AlarmManager
    private void scheduleNotification(String date, String time, String team1player1, String team1player2, String team2player1, String team2player2) {
        // Create a calendar instance to set the scheduled time for the match
        Calendar calendar = Calendar.getInstance();
        // Assuming the date format is "dd/MM/yyyy" and time format is "HH:mm"
        String[] dateParts = date.split("/");
        String[] timeParts = time.split(":");
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1); // Month is zero-based
        calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        calendar.set(Calendar.SECOND, 0);

        // Intent for the notification
        Intent intent = new Intent(getContext(), MatchNotificationReceiver.class);
        String message = "Match between " + team1player1 + " and " + team1player2 + " with " + team2player1 + " and " + team2player2  + " is coming up!";
        intent.putExtra("message", message);

        // Create PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule the notification using AlarmManager
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(getContext(), "Notification scheduled", Toast.LENGTH_SHORT).show();
    }

    // Method to clear all input fields
    private void clearFields() {
        dateEditText.setText("");
        timeEditText.setText("");
        team1player1NameEditText.setText("");
        team1player2NameEditText.setText("");
        team2player1NameEditText.setText("");
        team2player2NameEditText.setText("");
        readSystemTimeCheckBox.setChecked(false);  // Uncheck the checkbox
    }
}

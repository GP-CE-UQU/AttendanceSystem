package com.example.attendancewithfirebase;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AttendanceAdapter extends ArrayAdapter<Attendance> {

    public AttendanceAdapter(Activity context, List<Attendance> attend) {

        super(context, 0,attend);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;

        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_attendance, parent, false);
        }

        // Get the attend object located at this position in the list of objects
        // Position change every time to display all objects, this is how it works
        Attendance currentAttendance = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID attend_name
        TextView AttendanceDateIn = listItemView.findViewById(R.id.attendance_date);
        TextView AttendanceDateOut =  listItemView.findViewById(R.id.attendance_dateOut);
        TextView idTest = listItemView.findViewById(R.id.date_only);

        // Get the attend name from the current attend object and
        // set this text on the TextView
        AttendanceDateIn.setText(currentAttendance.getDateIn());
        AttendanceDateOut.setText(currentAttendance.getDateOut());
        idTest.setText(currentAttendance.getId());

        // Return the whole list item layout (containing the TextViews)
        // so that it can be shown in the ListView
        return listItemView;
    }
}
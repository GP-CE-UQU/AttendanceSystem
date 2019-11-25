package com.example.attendancewithfirebase;

public class Attendance {
    private String id;
    private String name;
    private String dateIn;
    private String dateOut;

    public Attendance() {

    }

    public Attendance(String id, String dateIn, String dateOut) {
        //this.name = name;
        this.dateIn = dateIn;
        this.dateOut = dateOut;
        this.id = id;
    }



    public String getDateIn() {
        return dateIn;
    }

    public String getDateOut() {
        return dateOut;
    }

    public String getId() {
        return id;
    }
}

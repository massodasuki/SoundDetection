package com.unifreelancer.masso.sounddetection;

/**
 * Created by Masso on 8/27/2017.
 */
 class PatientResult {
    private static PatientResult _instance = null;

    private PatientResult(){
    }
    public static PatientResult instance(){

        if (_instance == null){
            _instance = new PatientResult();
        }
        return _instance;
    }

    public double getMinDb() {
        return minDb;
    }

    public void setMinDb(double minDb) {
        this.minDb = minDb;
    }

    public double getMaxDb() {
        return maxDb;
    }

    public void setMaxDb(double maxDb) {
        this.maxDb = maxDb;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        this.Date = date;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        this.Score = score;
    }

    public int getAvrSpeech() {
        return AvrSpeech;
    }

    public void setAvrSpeech(int avrSpeech) {
        this.AvrSpeech = avrSpeech;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        this.Duration = duration;
    }

    private String Date;
    private int Score;
    private int AvrSpeech;
    private int Duration;

    private double minDb;
    private double maxDb;


}

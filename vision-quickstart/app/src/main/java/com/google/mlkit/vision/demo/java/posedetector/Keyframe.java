package com.google.mlkit.vision.demo.java.posedetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import java.time.Duration;
import java.time.Instant;

interface Point {
    boolean isValidPoint(List<List<Double>> landmarks);
    List<String> getInfo();
}

class TriPointAngle implements Point {
    private List<Integer> toTrack;
    private int target;
    private double actual;
    private double leniency;

    public TriPointAngle(List<Integer> _toTrack, int _angle, double _leniency) {
        toTrack = _toTrack;
        target = _angle;
        leniency = _leniency;
    }

    public TriPointAngle(JSONObject json) {
        try {
            toTrack = new ArrayList<>();
            target = (int) json.get("angle");
            leniency = Double.parseDouble(json.get("leniency").toString());
            JSONArray points = (JSONArray) json.get("toTrack");

            for (int i = 0; i < points.length(); i++) {
                toTrack.add((int) points.get(i));
            }

        } catch (JSONException e) {
            System.out.println(e);
            return;
        }
    }

    @Override
    public boolean isValidPoint(List<List<Double>> landmarks) {
        // get tracking points from landmarks
        // calculate the angle of the tracked points
        List<Double> p1 = landmarks.get(toTrack.get(0));
        List<Double> p2 = landmarks.get(toTrack.get(1));
        List<Double> p3 = landmarks.get(toTrack.get(2));

        actual = Util.getAngle(p1.get(0), p1.get(1), p2.get(0), p2.get(1), p3.get(0), p3.get(1));

//        System.out.println("Target angle: " + angle);
//        System.out.println("Actual angle: " + actualAngle);

        return Math.abs(actual - target) < leniency;
    }

    @Override
    public List<String>  getInfo() {

        List<String> poseFeedback = new ArrayList<>();

        poseFeedback.add("Target angle: " + target);
        poseFeedback.add("Actual angle: " + String.format("%.1f", actual));

        return poseFeedback;
    }
}

class UniPointPosition implements Point {
    private int toTrack;
    private List<Double> target;
    private List<Double> actual;
    private double leniency;

    public UniPointPosition(List<Double> _target, int _toTrack, double _leniency) {
        target = _target;
        toTrack = _toTrack;
        leniency = _leniency;
    }

    public UniPointPosition(JSONObject json) {
        try {

            //to track parsing
            JSONArray toTrackJson = (JSONArray) json.get("toTrack");
            toTrack = (int) toTrackJson.get(0);

            //target to reach parsing
            JSONArray targetJson = (JSONArray) json.get("target"); //gets the json array of the target to track (in screen ratio)

            List<Double> toTrackRatio = new ArrayList<>(); //array to store java target (still screen ratio)
            for (int i = 0; i < targetJson.length(); i++)
                toTrackRatio.add((Double) targetJson.get(i)); //converts the json array into the java array (still screen ratio target)

            target = Util.ScreenRatioVectorToPixelVector(toTrackRatio); //gets the pixel values for the target to track

            //leniency parsing
            leniency = Double.parseDouble(json.get("leniency").toString());

        } catch (JSONException e) {
            System.out.println(e);
            return;
        }
    }

    @Override
    public boolean isValidPoint(List<List<Double>> landmarks) {
        List<Double> point = landmarks.get(toTrack);
        actual = Arrays.asList(point.get(0), point.get(1));
        double distance = Util.getDistance(actual.get(0), actual.get(1), target.get(0), target.get(1));

        return distance <= leniency;
    }

    @Override
    public List<String>  getInfo() {

        List<String> poseFeedback = new ArrayList<>();
        poseFeedback.add("Target point: " + String.format("%.1f", target.get(0)) + ", " + String.format("%.1f", target.get(1)));
        poseFeedback.add("Actual point: " + String.format("%.1f", actual.get(0)) + ", " + String.format("%.1f", actual.get(1)));

        return poseFeedback;
    }
}

public class Keyframe implements Point {
    private List<Point> points;
    private Instant startTime;
    private Duration timeLimit;

    public Keyframe(List<Point> _points, Duration _timeLimit) {
        points = _points;
        timeLimit = _timeLimit;
        startTime = null;
    }

    public Keyframe(JSONObject json) {
        this.points = new ArrayList<>();
        try {
            double tl = Double.parseDouble(json.get("timeLimit").toString());
            if (tl == -1) {
                timeLimit = null;
            } else {
                timeLimit = Duration.ofMillis((long) (tl * 1000));
            }
            JSONArray points = (JSONArray) json.get("points");

            for (int i = 0; i < points.length(); i++) {
                JSONObject point = (JSONObject) points.get(i);
                switch ((String) point.get("pointType")) {
                    case "triPointAngle":
                        this.points.add(new TriPointAngle(point));
                        break;
                    case "pointPosition":
                        this.points.add(new UniPointPosition(point));
                        break;
                }
            }

        } catch (JSONException e) {
            System.out.println(e);
            return;
        }
    }

    @Override
    public boolean isValidPoint(List<List<Double>> landmarks) {
        // Iterate through every point
        for (int i = 0; i < points.size(); i++)
            if (!points.get(i).isValidPoint(landmarks))
                return false;
        return true;
    }

    @Override
    public List<String>  getInfo() {

        List<String> poseFeedback = new ArrayList<>();

        if (timeLimit != null && startTime != null) {
            Duration timeDiff = Duration.between(startTime, Instant.now());
            Duration timeLeft = timeLimit.minus(timeDiff);
            poseFeedback.add("Time left for this keyframe: " + Util.getDecimalSeconds(timeLeft));
        } else {
            poseFeedback.add("No timer");
        }

        //loops through each point in this frame
        for (int i = 0; i < points.size(); i++) {
            //gets the list of output strings
            List<String> thisFramesInfo = points.get(i).getInfo();
            //combines the two lists
            poseFeedback.addAll(thisFramesInfo);
        }
        return poseFeedback;
    }

    public boolean isWithinTime() {
        // TODO: Implement logic to determine if the timer has expired
        if (timeLimit == null) {
            // do nothing
        } else if (startTime == null) {
            startTime = Instant.now();
        } else {
            Instant currentTime = Instant.now();
            Duration timeDiff = Duration.between(startTime, currentTime);
            return timeDiff.compareTo(timeLimit) < 0;
        }
        return true;
    }

    public void clearTimer() {
        startTime = null;
    }
}

package com.google.mlkit.vision.demo.java.posedetector.pointTypes;

import com.google.mlkit.vision.demo.java.posedetector.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

import java.time.Duration;
import java.time.Instant;


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

            //gets the timing for this keyframe
            double tl = Double.parseDouble(json.get("timeLimit").toString());
            if (tl == -1)
                timeLimit = null;
            else
                timeLimit = Duration.ofMillis((long) (tl * 1000));

            //populates the list of points for this keyframe
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
                    case "abovePosition":
                        this.points.add(new PointAbovePoint(point));
                        break;
                    case "parallelLines":
                        this.points.add(new ParallelLines(point));
                        break;
                    default:
                        this.points.add(new UnknownPoint());
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

        boolean toReturn = true;

        // Iterate through every point
        for (int i = 0; i < points.size(); i++)

            //cumulativeley saves if the points were met
            toReturn &= points.get(i).isValidPoint(landmarks);

        //returns if all the points were met
        return toReturn;
    }

    @Override
    public List<String> getInfo() {

        List<String> poseFeedback = new ArrayList<>();

        if (timeLimit != null && startTime != null) {
            Duration timeDiff = Duration.between(startTime, Instant.now());
            Duration timeLeft = timeLimit.minus(timeDiff);
            poseFeedback.add("Time left for this keyframe: " + Util.getDecimalSeconds(timeLeft));
        } else {
            poseFeedback.add("No timer");
        }

        //loops through each point in this frame
        for (int i = 0; i < points.size(); i++)

            //combines the two lists
            poseFeedback.addAll(points.get(i).getInfo());

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

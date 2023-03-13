package com.google.mlkit.vision.demo.java.posedetector.pointTypes;

import com.google.mlkit.vision.demo.java.posedetector.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TriPointAngle implements Point {

    private List<Integer> toTrack;
    private double target;
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
            target =  Double.parseDouble(json.get("angle").toString());
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
        List<Double> p1 = landmarks.get(toTrack.get(0));
        List<Double> p2 = landmarks.get(toTrack.get(1));
        List<Double> p3 = landmarks.get(toTrack.get(2));

        // calculate the angle of the tracked points
        actual = Util.getAngle(p1.get(0), p1.get(1), p2.get(0), p2.get(1), p3.get(0), p3.get(1));

        return Math.abs(actual - target) < leniency;
    }

    @Override
    public List<String> getInfo() {

        List<String> poseFeedback = new ArrayList<>();

        poseFeedback.add("Target angle: " + target);
        poseFeedback.add("Actual angle: " + String.format("%.1f", actual));

        return poseFeedback;
    }
}

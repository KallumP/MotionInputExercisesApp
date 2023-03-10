package com.google.mlkit.vision.demo.java.posedetector.pointTypes;

import com.google.mlkit.vision.demo.java.posedetector.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UniPointPosition implements Point {
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

            target = Util.screenRatioVectorToPixelVector(toTrackRatio); //gets the pixel values for the target to track

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

        actual = Arrays.asList((double) point.get(0), (double) point.get(1));
        double distance = Util.getDistance(actual.get(0), actual.get(1), target.get(0), target.get(1));

        return distance < leniency;
    }

    @Override
    public List<String> getInfo() {

        List<String> poseFeedback = new ArrayList<>();

        if (target != null)
            poseFeedback.add("Target point: " + String.format("%.1f", target.get(0)) + ", " + String.format("%.1f", target.get(1)));
        if (actual != null)
            poseFeedback.add("Actual point: " + String.format("%.1f", actual.get(0)) + ", " + String.format("%.1f", actual.get(1)));

        return poseFeedback;
    }
}

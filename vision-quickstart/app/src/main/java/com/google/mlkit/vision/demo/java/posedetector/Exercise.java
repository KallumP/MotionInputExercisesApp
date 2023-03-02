package com.google.mlkit.vision.demo.java.posedetector;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import com.google.mlkit.vision.demo.java.posedetector.pointTypes.Keyframe;
import com.google.mlkit.vision.pose.PoseLandmark;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Exercise {

    String name;
    private List<Keyframe> keyframes;
    int currentKeyframe;
    int gestureCount;

    public Exercise(List<Keyframe> _kfs) {
        keyframes = _kfs;
        currentKeyframe = 0;
        gestureCount = 0;
    }

    public Exercise(JSONObject json, String _name) {
        name = _name;
        this.keyframes = new ArrayList<>();
        try {
            JSONArray kfs = (JSONArray) json.get("keyframes");
            for (int i = 0; i < kfs.length(); i++) {
                JSONObject kf = (JSONObject) kfs.get(i);
                this.keyframes.add(new Keyframe(kf));
            }
        } catch (JSONException e) {
            System.out.println(e);
            return;
        }
    }

    public void validatePose(List<PoseLandmark> landmarks) {
        Keyframe kf = keyframes.get(currentKeyframe);

        List<List<Double>> landmarks_double = new ArrayList<>(landmarks.size());

        for (int i = 0; i < landmarks.size(); i++) {
            PoseLandmark lm = landmarks.get(i);
            landmarks_double.add(Arrays.asList((double) lm.getPosition().x, (double) lm.getPosition().y));
        }

        boolean validPose = kf.isValidPoint(landmarks_double);
        boolean withinTime = kf.isWithinTime();
        if (!withinTime) {
            // reset the state
            for (int i = 0; i < currentKeyframe + 1; i++) {
                keyframes.get(i).clearTimer();
            }
            currentKeyframe = 0;
        } else if (!validPose) {
            // pose is not valid but is still within time
            // continue
            System.out.println("Invalid pose");
        } else {
            // move to the next keyframe
            // or terminate if traversed all keyframes
            System.out.println("Pass");
            if (currentKeyframe >= keyframes.size() - 1) {
                resetPoseTracker();
            } else {
                keyframes.get(currentKeyframe).clearTimer();
                currentKeyframe++;
            }
        }
    }

    public void resetPoseTracker() {

        gestureCount++;

        currentKeyframe = 0;

        for (int i = 0; i < keyframes.size(); i++)
            keyframes.get(i).clearTimer();
    }

    public int getPoseStatus() {
        return currentKeyframe + 1;
    }

    public List<String> getPoseInfo() {
        List<String> info = keyframes.get(currentKeyframe).getInfo();
        info.add("Current Keyframe: " + currentKeyframe);
        info.add("Gestures detected: " + gestureCount);
        return info;
    }
}

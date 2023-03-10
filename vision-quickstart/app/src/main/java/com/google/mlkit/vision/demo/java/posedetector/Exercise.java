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
        keyframes = new ArrayList<>();
        try {
            JSONArray keyFramesJson = (JSONArray) json.get("keyframes");
            for (int i = 0; i < keyFramesJson.length(); i++) {
                JSONObject keyFrameJson = (JSONObject) keyFramesJson.get(i);
                keyframes.add(new Keyframe(keyFrameJson));
            }
        } catch (JSONException e) {
            System.out.println(e);
            return;
        }
    }

    public boolean validatePose(List<PoseLandmark> landmarks) {

        if (landmarks.size() > 0) { //doesn't do anything if there are no landmarks

            //gets the current keyframe
            Keyframe keyframe = keyframes.get(currentKeyframe);

            //gets all the landmarks of the user
            List<List<Double>> landmarks_double = new ArrayList<>(landmarks.size());
            for (int i = 0; i < landmarks.size(); i++) {
                PoseLandmark lm = landmarks.get(i);
                landmarks_double.add(Arrays.asList((double) lm.getPosition().x, (double) lm.getPosition().y));
            }

            //checks if the user's pose matched what is required of this keyframe
            boolean validPose = keyframe.isValidPoint(landmarks_double);

            //checks if the pose was matched within the required time
            boolean withinTime = keyframe.isWithinTime();


            if (!withinTime) { //not within time

                // reset the state
                for (int i = 0; i < currentKeyframe + 1; i++) {
                    keyframes.get(i).clearTimer();
                }
                currentKeyframe = 0;

            } else if (!validPose) { //not a valid pose

                //do nothing

            } else { //within time and good pose

                if (currentKeyframe >= keyframes.size() - 1) { //current keyframe was the lase

                    return true; //lets the timeline know that this exercise is over

                } else { //more keyframes left

                    keyframes.get(currentKeyframe).clearTimer(); //resets this keyframe's timer

                    currentKeyframe++; // move to the next keyframe
                }
            }
        }

        return false; //returns that this exercise is not over
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
        List<String> info = new ArrayList<>();

        //states the current keyframe
        info.add("Current Keyframe: " + currentKeyframe);

        //gets the keyframes info
        info.addAll(keyframes.get(currentKeyframe).getInfo());

        return info;
    }
}

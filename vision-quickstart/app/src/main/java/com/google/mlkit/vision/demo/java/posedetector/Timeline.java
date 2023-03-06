package com.google.mlkit.vision.demo.java.posedetector;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// An object that encapsulates a series of exercises
public class Timeline {

    static JSONObject gestureJson;
    public List<Exercise> exercises = null;
    public int currentIndex = 0;
    public int timelineRepeat = 0;

    public int exerciseUnavailable = 1;

    public Timeline(Context context, JSONObject timelineJson) {
        // Read each exercise stored in the json
        try {
            JSONArray allExerciseJson = (JSONArray) timelineJson.get("timeline");
            exerciseUnavailable = allExerciseJson.length();
            exercises = new ArrayList<>(Collections.nCopies(exerciseUnavailable, null));
            RequestQueue rq = Volley.newRequestQueue(context.getApplicationContext());

            for (int i = 0; i < allExerciseJson.length(); i++) {
                // Download every exercise's json file
                JSONObject exerciseJson = (JSONObject) allExerciseJson.get(i);
                String name = (String) exerciseJson.get("exercise");
                String url = (String) exerciseJson.get("url");
                final int index = i;
                StringRequest sr = Util.fetchJson(url, context, new Util.jsonHandler() {
                    @Override
                    void parse(JSONObject response) {
                        exercises.set(index, new Exercise(response, name));
                        exerciseUnavailable -= 1;
                    }
                });
                rq.add(sr);
            }
            currentIndex = 0;
        } catch (JSONException e) {
            System.out.println(e);
        }
    }

    public void validatePose(List<PoseLandmark> landmarks) {

        //passes the validation down to the current exercise (and checks if the exercise finished
        if (exercises.get(currentIndex).validatePose(landmarks)) {

            //moves to the next exercise
            currentIndex++;

            //if timeline has finished
            if (currentIndex >= exercises.size()){
                // resets all exercises
                for (int i = 0; i < currentIndex; i++) {
                    exercises.get(i).resetPoseTracker();
                }
                //resets the timeline
                currentIndex = 0;

                //increments the repeat counter
                timelineRepeat++;
            }
        }
    }

    // TODO: Modify this function
    public List<String> getLandMarkInfo() {
        List<String> info = new ArrayList<>();

        //states how many times the timeline has been completed
        info.add("Timeline finished " + timelineRepeat + " times");

        //states the current exercise name
        info.add("Current exercise: " + exercises.get(currentIndex).name);

        //gets the exercise's info
        info.addAll(exercises.get(currentIndex).getPoseInfo());

        return info;
    }
}

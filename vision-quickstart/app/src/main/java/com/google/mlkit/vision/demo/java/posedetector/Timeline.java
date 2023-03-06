package com.google.mlkit.vision.demo.java.posedetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;

import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.GraphicOverlay.Graphic;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A timeline encapsulates a series of related exercises
// Seems like extending the Graphic interface is a bit unnecessary
public class Timeline extends Graphic {

    static JSONObject gestureJson;
    public List<Exercise> exercises = null;
    public int currentIndex = 0;
    public int timelineRepeat = 0;

    public int exerciseUnavailable = 1;

    // Why do we need this overlay object
    public Timeline(GraphicOverlay overlay, JSONObject json) {
        super(overlay);

        // TODO: Read the timeline url from sharedPreferences
        SharedPreferences sp = getApplicationContext().getSharedPreferences("timelineData", Context.MODE_PRIVATE);
        String url = sp.getString("timelineUrl", "null"); // default url is one of the sample timelines

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject timelineJSON = new JSONObject(response);
                    // Read the individual exercise stored in the json
                    JSONArray allExerciseJson = (JSONArray) timelineJSON.get("timeline");
                    exerciseUnavailable = allExerciseJson.length();
                    exercises = new ArrayList<>(Collections.nCopies(exerciseUnavailable, null));
                    RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

                    for (int i = 0; i < allExerciseJson.length(); i++) {
                        // Download every exercise's json file
                        JSONObject exerciseJson = (JSONObject) allExerciseJson.get(i);
                        String name = (String) exerciseJson.get("exercise");
                        String url = (String) exerciseJson.get("url");
                        final int index = i;
                        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject exerciseJson = new JSONObject(response);
                                    exercises.set(index, new Exercise(exerciseJson, name));
                                    exerciseUnavailable -= 1;
                                } catch (JSONException e) {System.out.println(e);}
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError e) {System.out.println(e);}
                        });
                        rq.add(sr);
                    }
                } catch (JSONException e) {System.out.println(e);}
            }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {System.out.println(e);}
            }
        );

        rq.add(sr);
//
//        //instantiates the list of exercises
//        exercises = new ArrayList<>();
//
//        try {
//            //gets the list of exercises in a json array format
//            JSONArray exercisesJson = (JSONArray) json.get("timeline");
//
//            //loops through the list of exercises
//            for (int i = 0; i < exercisesJson.length(); i++) {
//
//                //gets the json for this particular exercise
//                JSONObject exerciseJson = (JSONObject) exercisesJson.get(i);
//
//                //gets the name of this exercise
//                String exerciseName = (String) exerciseJson.get("exercise");
//
//                //gets the url for this exercise's api call
//                String exerciseURL = (String) exerciseJson.get("url");
//
//                //pulls the relevant data and adds this exercise to the list
//                PullExerciseJson(exerciseURL, exerciseName);
//            }
//
//        } catch (JSONException e) {
//            System.out.println(e);
//            return;
//        }
//
        currentIndex = 0;
    }

    @Override
    public void draw(Canvas canvas) {
        //no need to draw anything
    }

    public void validatePose(List<PoseLandmark> landmarks) {

        //passes the validation down to the current exercise (and checks if the exercise finished
        if (exercises.get(currentIndex).validatePose(landmarks)) {

            //moves to the next exercise
            currentIndex++;

            //if timeline has finished
            if (currentIndex >= exercises.size()){
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


    // TODO: Rewrite this function to fetch all exercises
    void PullExerciseJson(String url, String name) {
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    // order the response here
                    gestureJson = new JSONObject(response);
                    exercises.add(new Exercise(gestureJson, name));

                } catch (JSONException e) {
                    System.out.println(e);
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

    }
//
//    private interface jsonParser {
//        void handleResponse(String response);
//    }
//
//    private void fetchJson(String url, jsonParser parser) {
//
//    }
}

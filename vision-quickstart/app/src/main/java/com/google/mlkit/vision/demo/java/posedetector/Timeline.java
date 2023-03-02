package com.google.mlkit.vision.demo.java.posedetector;

import android.graphics.Canvas;

import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.GraphicOverlay.Graphic;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.mlkit.vision.pose.PoseLandmark;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Timeline extends Graphic {

    RequestQueue rq;
    static JSONObject gestureJson;
    List<Exercise> exercises = null;
    int currentIndex = 0;

    List<String> debugList = new ArrayList<>();


    public Timeline(GraphicOverlay overlay, JSONObject json, RequestQueue _rq) {
        super(overlay);

        rq = _rq;

        //instantiates the list of exercises
        exercises = new ArrayList<>();

        try {

            //gets the list of exercises in a json array format
            JSONArray exercisesJson = (JSONArray) json.get("timeline");

            //loops through the list of exercises
            for (int i = 0; i < exercisesJson.length(); i++) {

                //gets the json for this particular exercise
                JSONObject exerciseJson = (JSONObject) exercisesJson.get(i);

                //gets the name of this exercise
                String exerciseName = (String) exerciseJson.get("exercise");

                //gets the url for this exercise's api call
                String exerciseURL = (String) exerciseJson.get("url");

                //pulls the relevant data and adds this exercise to the list
                PullExerciseJson(exerciseURL, exerciseName);
            }

        } catch (
                JSONException e) {
            System.out.println(e);
            return;
        }

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
            if (currentIndex >= exercises.size())

                currentIndex = 0;
        }
    }

    public List<String> getLandMarkInfo() {
        List<String> info = new ArrayList<>();

        //states the current exercise name
        info.add("Current exercise: " + exercises.get(currentIndex).name);

        //gets the exercise's info
        info.addAll(exercises.get(currentIndex).getPoseInfo());

        return info;
    }


    void PullExerciseJson(String url, String name) {

        RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {

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

        rq.add(sr);
    }
}

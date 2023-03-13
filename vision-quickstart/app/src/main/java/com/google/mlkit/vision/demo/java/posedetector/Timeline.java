package com.google.mlkit.vision.demo.java.posedetector;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.pose.PoseLandmark;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// An object that encapsulates a series of exercises
public class Timeline {

    public List<Exercise> exercises = null;
    public int currentIndex = 0;
    public int timelineRepeat = 0;
    private int exerciseUnavailable;
    // Indicates whether we can validate landmarks against this timeline
    public boolean ready = false;
    String name;

    public Timeline(Context context, JSONObject timelineJson, String _name) {
        name = _name;

        // Read each exercise stored in the json
        try {
            //gets all the exercise jsons from the timeline
            JSONArray allExerciseJson = (JSONArray) timelineJson.get("timeline");
            exerciseUnavailable = allExerciseJson.length();
            exercises = new ArrayList<>(Collections.nCopies(exerciseUnavailable, null));

            //loops through all the exercise jsons
            for (int i = 0; i < allExerciseJson.length(); i++) {
                //adds this exercise to the list
                String name = ((JSONObject) allExerciseJson.get(i)).get("exercise").toString();
                fetchExercise(name, i);
            }
            currentIndex = 0;
        } catch (JSONException e) {
            System.out.println(e);
        }
    }

    public void validatePose(List<PoseLandmark> landmarks) {

        if (exercises.size() == 0)
            return;

        //passes the validation down to the current exercise (and checks if the exercise finished
        if (exercises.get(currentIndex).validatePose(landmarks)) {

            //moves to the next exercise
            currentIndex++;

            //if timeline has finished
            if (currentIndex >= exercises.size()) {

                // resets all exercises
                for (int i = 0; i < currentIndex; i++)
                    exercises.get(i).resetExercise();

                //resets the timeline
                currentIndex = 0;

                //increments the repeat counter
                timelineRepeat++;
            }
        }
    }

    public List<String> getLandMarkInfo() {
        List<String> info = new ArrayList<>();

        //states how many times the timeline has been completed
        info.add("Timeline: " + name);

        if (exercises.size() == 0){
            info.add("No exercises. Try to reload...");
            return info;
        }

        info.add("Repetitions: " + timelineRepeat);

        //states the current exercise name
        info.add("Current exercise: " + exercises.get(currentIndex).name);

        //gets the exercise's info
        info.addAll(exercises.get(currentIndex).getExerciseInfo());

        return info;
    }

    void fetchExercise(String exerciseName, final int index) {
        if (exerciseName.contains(".json"))
            exerciseName = exerciseName.substring(0, exerciseName.length() - 5);
        String path = "Exercises/" + exerciseName;
        Util.fetchJson(path, new Util.jsonHandler() {
            @Override
            void parse(String name, JSONObject response) {
                exercises.set(index, new Exercise(response, name));
                exerciseUnavailable -= 1;
                if (exerciseUnavailable == 0)
                    ready = true;
            }
        });
    }
}

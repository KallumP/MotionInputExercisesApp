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
import java.util.List;

// An object that encapsulates a series of exercises
public class Timeline {

    public List<Exercise> exercises = null;
    public int currentIndex = 0;
    public int timelineRepeat = 0;
    String name = "";

    public Timeline(Context context, JSONObject timelineJson, String _name) {

        name = _name;
        exercises = new ArrayList<>();

        // Read each exercise stored in the json
        try {

            //gets all the exercise jsons from the timeline
            JSONArray allExerciseJson = (JSONArray) timelineJson.get("timeline");

            //loops through all the exercise jsons
            for (int i = 0; i < allExerciseJson.length(); i++)

                //adds this exercise to the list
                FetchExercise(((JSONObject) allExerciseJson.get(i)).get("exercise").toString());

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
                    exercises.get(i).resetPoseTracker();

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
        info.add("Timeline: " + name + " finished " + timelineRepeat + " times");

        if (exercises.size() == 0){
            info.add("No exercises");
            return info;
        }

        //states the current exercise name
        info.add("Current exercise: " + exercises.get(currentIndex).name);

        //gets the exercise's info
        info.addAll(exercises.get(currentIndex).getPoseInfo());

        return info;
    }

    void FetchExercise(String exerciseName) {

        //removes the ".json" from the exercise name if there was one
        String nameWithoutExtension = exerciseName.replace(".json", "");
        String exercisePath = "Exercises/" + nameWithoutExtension;

        //instance of the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //tries to pull this exercise's json
        database.getReference(exercisePath).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                //couldn't get values
                if (!task.isSuccessful())
                    Log.e("firebase", "Error getting data", task.getException());

                else {

                    //gets the json of this exercise
                    String value = task.getResult().getValue().toString();

                    try {

                        //adds this exercise to the list
                        JSONObject fetchedJson = new JSONObject(value);
                        exercises.add(new Exercise(fetchedJson, nameWithoutExtension));

                    } catch (JSONException e) {
                        System.out.println(e);
                    }
                }
            }
        });
    }
}

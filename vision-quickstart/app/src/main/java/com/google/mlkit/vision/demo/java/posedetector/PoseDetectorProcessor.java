/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.java.posedetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.odml.image.MlImage;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;

import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.java.VisionProcessorBase;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


//A processor to run pose detector.
public class PoseDetectorProcessor extends VisionProcessorBase<PoseDetectorProcessor.PoseResult> {
    private static final String TAG = "PoseDetectorProcessor";

    private final PoseDetector detector;

    private final boolean showInFrameLikelihood;
    private final boolean visualizeZ;
    private final boolean rescaleZForVisualization;
    private final boolean isStreamMode;
    private final Context context;

    public List<Timeline> timelines = null;

    //Internal class to hold Pose and classification results.
    protected static class PoseResult {
        private final Pose pose;

        public PoseResult(Pose pose) {
            this.pose = pose;
        }

        public Pose getPose() {
            return pose;
        }

    }

    public PoseDetectorProcessor(Context context, PoseDetectorOptionsBase options, boolean showInFrameLikelihood, boolean visualizeZ, boolean rescaleZForVisualization, boolean isStreamMode) {
        super(context);
        this.showInFrameLikelihood = showInFrameLikelihood;
        this.visualizeZ = visualizeZ;
        this.rescaleZForVisualization = rescaleZForVisualization;
        this.detector = PoseDetection.getClient(options);
        this.isStreamMode = isStreamMode;
        // we could pass the context object here into the Timeline class
        this.context = context;
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences("timelineData", Context.MODE_PRIVATE);
        sp.edit().putString("timelineUrl", "https://api.npoint.io/f9d2459d13562c7a5542").apply();

        setupTimeline(context);
    }

    void setupTimeline(Context context) {

        timelines = new ArrayList<>();
        FetchTimelines();
    }

    @Override
    public void stop() {
        super.stop();
        detector.close();
    }

    @Override
    protected Task<PoseResult> detectInImage(InputImage image) {
        return detector.process(image).continueWith(task -> {
            Pose pose = task.getResult();
            return new PoseResult(pose);
        });
    }

    @Override
    protected Task<PoseResult> detectInImage(MlImage image) {
        return detector.process(image).continueWith(task -> {
            Pose pose = task.getResult();
            return new PoseResult(pose);
        });
    }

    @Override
    protected void onSuccess(@NonNull PoseResult poseResult, @NonNull GraphicOverlay graphicOverlay) {

        //json was not initialised
        if (timelines == null)
            return;

        //no timelines found
        if (timelines.size() == 0)
            return;


        List<String> info = new ArrayList<>();

        info.add("Timelines found: " + timelines.size());

        int timelineToCheck = 1;
        //if the timeline exists, and there were exercises
        if (timelines.get(timelineToCheck) != null) {
            info.add("condition passed");
            timelines.get(timelineToCheck).validatePose(poseResult.pose.getAllPoseLandmarks());
            info = timelines.get(timelineToCheck).getLandMarkInfo();
        }

        graphicOverlay.add(new PoseGraphic(graphicOverlay, poseResult.pose, showInFrameLikelihood, visualizeZ, rescaleZForVisualization, info));
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Pose detection failed!", e);
    }

    @Override
    protected boolean isMlImageEnabled(Context context) {
        // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
        return true;
    }

    void FetchTimelines() {

        //instance of the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //tries to pull the timelines section of the database
        database.getReference("Timelines").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                //couldn't get values
                if (!task.isSuccessful())
                    Log.e("firebase", "Error getting data", task.getException());

                else {

                    // Get the value of each child object
                    for (DataSnapshot childSnapshot : task.getResult().getChildren()) {

                        if (childSnapshot.getKey() == "1")
                            return;

                        //gets the json of current timeline
                        String value = childSnapshot.getValue().toString();
                        String name = childSnapshot.getKey().toString();

                        try {

                            //adds this timeline to the list
                            JSONObject fetchedJson = new JSONObject(value);
                            timelines.add(new Timeline(context, fetchedJson, name));

                        } catch (JSONException e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        });
    }
}

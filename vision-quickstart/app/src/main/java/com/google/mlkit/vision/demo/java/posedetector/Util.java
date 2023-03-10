package com.google.mlkit.vision.demo.java.posedetector;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.time.Duration;

public class Util {
    public static Integer screenHeight = null;
    public static Integer screenWidth = null;

    public static abstract class jsonHandler {
        abstract void parse(JSONObject response);
    }

    public static StringRequest fetchJson(String url, Context context, jsonHandler handler) {
        return new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    handler.parse(json);
                } catch (JSONException e) {System.out.println(e);}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {System.out.println(e);}
        }
        );
    }

    //returns exact pixel values of the screen from a ratio vector array
    public static List<Double> screenRatioVectorToPixelVector(List<Double> ratio) {

        List<Double> toReturn = new ArrayList<>();

        toReturn.add(ratio.get(0) * Util.screenWidth);
        toReturn.add(ratio.get(1) * Util.screenHeight);

        return toReturn;
    }

    public static double getDistance(double x1, double y1, double x2, double y2) {
        double xDistance = Math.pow(x1 - x2, 2);
        double yDistance = Math.pow(y1 - y2, 2);

        return Math.pow(xDistance + yDistance, 0.5);
    }
    public static double getAngle(double x1, double y1, double x2, double y2, double x3, double y3) {
        double len1_2 = getDistance(x1, y1, x2, y2);
        double len2_3 = getDistance(x2, y2, x3, y3);
        double len1_3 = getDistance(x1, y1, x3, y3);
        double radian = Math.acos((Math.pow(len1_2, 2) + Math.pow(len2_3, 2) - Math.pow(len1_3, 2)) / (2 * len1_2 * len2_3));
        double degrees = Math.toDegrees(radian);
        return degrees;
    }

    public static double getAngle(List<Double> start, List<Double> end) {
        if (start.get(0) == end.get(0))
            if (start.get(1) > end.get(1))
                return -90.0;
            else
                return 90.0;

        Double deltaX = start.get(0) - end.get(0);
        Double deltaY = start.get(1) - end.get(1);

        Double thetaRad = Math.atan(deltaY/deltaX);
        Double thetaDeg = Math.toDegrees(thetaRad);

        // we convert all angles to the range [0, 180], for the ease of comparison later
        if (thetaDeg < 0)
            thetaDeg += 180;

//        System.out.println("Degrees: " + thetaDeg + ", Radians: " + thetaRad);

        return thetaDeg;
    }

    public static float getDecimalSeconds(Duration time) {
        return (float) time.toMillis() / 1000;
    }
}

package com.google.mlkit.vision.demo.java.posedetector;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class TriPointAngleTest {

    private TriPointAngle tpa;
    private List<List<Double>> landmarks;

    @Before
    public void setUp() {
        List<Integer> toTrack = Arrays.asList(16, 14, 12);
        double leniency = 10.0;
        int angle = 45;
        tpa = new TriPointAngle(toTrack, angle, leniency);
        landmarks = new ArrayList<>(Collections.nCopies(33, null));
    }

    @After
    public void tearDown() {
        tpa = null;
    }

    @Test
    public void isValidPoint_SameAngle_True() {
        landmarks.set(16, Arrays.asList(10.0, 10.0));
        landmarks.set(14, Arrays.asList(0.0, 0.0));
        landmarks.set(12, Arrays.asList(10.0, 0.0));

        assertTrue("Validate same angle", tpa.isValidPoint(landmarks));
    }

    @Test
    public void isValidPoint_SlightDeviationWithinRange_True() {
        landmarks.set(16, Arrays.asList(10.0, 11.0));
        landmarks.set(14, Arrays.asList(0.0, 0.0));
        landmarks.set(12, Arrays.asList(10.0, 0.0));

        assertTrue("Validate angle with slight deviation", tpa.isValidPoint(landmarks));
    }

    @Test
    public void isValidPoint_OutOfRange_False() {
        landmarks.set(16, Arrays.asList(0.0, 10.0));
        landmarks.set(14, Arrays.asList(0.0, 0.0));
        landmarks.set(12, Arrays.asList(10.0, 0.0));

        assertFalse("Validate angle out of range", tpa.isValidPoint(landmarks));
    }
}
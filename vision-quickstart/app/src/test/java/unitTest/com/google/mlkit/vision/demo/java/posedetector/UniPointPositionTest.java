package com.google.mlkit.vision.demo.java.posedetector;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class UniPointPositionTest {
    private UniPointPosition upp;
    private List<List<Double>> landmarks;

    @Before
    public void setUp() throws Exception {
        // Using pixels as the units
        upp = new UniPointPosition(Arrays.asList(300.0, 100.0), 15, 20);
        landmarks = new ArrayList<>(Collections.nCopies(33, null));
    }

    @After
    public void tearDown() throws Exception {
        upp = null;
        landmarks = null;
    }

    @Test
    public void isValidPoint_SamePoint_True() {
        landmarks.set(15, Arrays.asList(300.0, 100.0));
        assertTrue(upp.isValidPoint(landmarks));
    }

    @Test
    public void isValidPoint_WithinLeniency_True() {
        landmarks.set(15, Arrays.asList(300.0, 110.0));
        assertTrue(upp.isValidPoint(landmarks));
    }

    @Test
    public void isValidPoint_OnBoundary_True() {
        landmarks.set(15, Arrays.asList(300.0, 120.0));
        assertTrue(upp.isValidPoint(landmarks));
    }

    @Test
    public void isValidPoint_OutOfBoundary_False() {
        landmarks.set(15, Arrays.asList(320.0, 110.0));
        assertFalse(upp.isValidPoint(landmarks));
    }
}
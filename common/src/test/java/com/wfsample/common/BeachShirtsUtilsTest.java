package com.wfsample.common;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for BeachShirtsUtils class.
 */
public class BeachShirtsUtilsTest {

    @Test
    public void testIsErrorRequest() {
        AtomicInteger index = new AtomicInteger(0);
        
        assertTrue(BeachShirtsUtils.isErrorRequest(index, 1, 10));
        
        index = new AtomicInteger(0);
        
        assertFalse(BeachShirtsUtils.isErrorRequest(index, 0, 2)); // First request (index=1)
        assertTrue(BeachShirtsUtils.isErrorRequest(index, 0, 2));  // Second request (index=2)
        assertFalse(BeachShirtsUtils.isErrorRequest(index, 0, 2)); // Third request (index=3)
    }

    @Test
    public void testGetRequestLatency() {
        Random rand = new Random(123); // Use fixed seed for reproducibility
        
        long mean = 100;
        long delta = 0;
        assertEquals(mean, BeachShirtsUtils.getRequestLatency(mean, delta, rand));
        
        mean = -100;
        delta = 0;
        assertEquals(mean, BeachShirtsUtils.getRequestLatency(mean, delta, rand));
        
        mean = 0;
        delta = 0;
        assertEquals(0, BeachShirtsUtils.getRequestLatency(mean, delta, rand));
        
        mean = 100;
        delta = 50;
        long result = BeachShirtsUtils.getRequestLatency(mean, delta, rand);
        assertTrue("Result should be within reasonable range of mean", 
                  result >= mean - 2*delta && result <= mean + 2*delta);
    }
}

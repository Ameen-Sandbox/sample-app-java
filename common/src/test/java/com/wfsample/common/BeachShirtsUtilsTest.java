package com.wfsample.common;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for BeachShirtsUtils class.
 */
public class BeachShirtsUtilsTest {

    @Test
    public void testIsErrorRequest() {
        assertFalse(BeachShirtsUtils.isErrorRequest(0.0));
        
        assertTrue(BeachShirtsUtils.isErrorRequest(1.0));
        
        assertFalse(BeachShirtsUtils.isErrorRequest(-0.1));
        
        assertTrue(BeachShirtsUtils.isErrorRequest(1.1));
    }

    @Test
    public void testGetLatency() {
        assertEquals(0, BeachShirtsUtils.getLatency(0, 0));
        
        int meanLatency = 100;
        assertEquals(meanLatency, BeachShirtsUtils.getLatency(meanLatency, 0));
        
        assertEquals(0, BeachShirtsUtils.getLatency(-100, 0));
    }
}

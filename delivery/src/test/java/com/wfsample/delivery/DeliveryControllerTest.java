package com.wfsample.delivery;

import com.wavefront.sdk.jersey.WavefrontJerseyFactory;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtDTO;

import io.opentracing.Tracer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DeliveryController.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeliveryControllerTest {

    @Mock
    private Environment env;
    
    @Mock
    private WavefrontJerseyFactory wavefrontJerseyFactory;
    
    private DeliveryController deliveryController;

    @Before
    public void setUp() {
        when(env.getProperty("request.slow.percentage", Double.class)).thenReturn(0.0);
        when(env.getProperty("request.slow.latency", Long.class)).thenReturn(0L);
        when(env.getProperty("request.error.interval", Integer.class)).thenReturn(0);
        
        deliveryController = new DeliveryController(env, wavefrontJerseyFactory);
    }

    @Test
    public void testTrackOrder() {
        Response response = deliveryController.trackOrder("12345");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
    
    @Test
    public void testCancelOrder() {
        Response response = deliveryController.cancelOrder("12345");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
}

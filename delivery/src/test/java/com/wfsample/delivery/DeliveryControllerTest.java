package com.wfsample.delivery;

import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DeliveryController.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeliveryControllerTest {

    @Mock
    private AtomicInteger tracking;

    @InjectMocks
    private DeliveryController deliveryController;

    @Before
    public void setUp() {
        when(tracking.incrementAndGet()).thenReturn(1);
    }

    @Test
    public void testDispatch() {
        PackedShirtsDTO packedShirtsDTO = new PackedShirtsDTO();
        packedShirtsDTO.setPackedShirts(5);
        
        ResponseEntity<DeliveryStatusDTO> response = deliveryController.dispatch("12345", packedShirtsDTO);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("scheduled", response.getBody().getStatus());
        assertEquals(1, response.getBody().getTrackingNum());
    }
    
    @Test
    public void testTrack() {
        when(tracking.get()).thenReturn(1);
        
        ResponseEntity<DeliveryStatusDTO> response = deliveryController.track("12345");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("delivered", response.getBody().getStatus());
    }
    
    @Test
    public void testCancel() {
        ResponseEntity<?> response = deliveryController.cancel("12345");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

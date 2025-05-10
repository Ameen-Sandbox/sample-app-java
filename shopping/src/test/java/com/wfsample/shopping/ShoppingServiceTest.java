package com.wfsample.shopping;

import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.service.DeliveryApi;
import com.wfsample.service.StylingApi;

import io.dropwizard.testing.junit.ResourceTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ShoppingService.
 */
public class ShoppingServiceTest {

    private static final StylingApi stylingApi = mock(StylingApi.class);
    private static final DeliveryApi deliveryApi = mock(DeliveryApi.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new ShoppingWebResource(stylingApi, deliveryApi))
            .build();

    @Before
    public void setup() {
        List<ShirtStyleDTO> mockStyles = new ArrayList<>();
        mockStyles.add(new ShirtStyleDTO("style1", "url1"));
        mockStyles.add(new ShirtStyleDTO("style2", "url2"));
        when(stylingApi.getAllStyles()).thenReturn(mockStyles);
        
        PackedShirtsDTO packedShirtsDTO = new PackedShirtsDTO();
        packedShirtsDTO.setPackedShirts(5);
        when(stylingApi.makeShirts(anyString(), anyInt())).thenReturn(Response.ok(packedShirtsDTO).build());
        
        DeliveryStatusDTO deliveryStatusDTO = new DeliveryStatusDTO();
        deliveryStatusDTO.setStatus("scheduled");
        deliveryStatusDTO.setTrackingNum(1);
        when(deliveryApi.dispatch(anyString(), any(PackedShirtsDTO.class))).thenReturn(Response.ok(deliveryStatusDTO).build());
        
        DeliveryStatusDTO trackingStatusDTO = new DeliveryStatusDTO();
        trackingStatusDTO.setStatus("delivered");
        trackingStatusDTO.setTrackingNum(1);
        when(deliveryApi.track(anyString())).thenReturn(Response.ok(trackingStatusDTO).build());
        
        when(deliveryApi.cancel(anyString())).thenReturn(Response.ok().build());
    }

    @Test
    public void testGetShoppingMenu() {
        List<ShirtStyleDTO> styles = resources.target("/shop/menu")
                .request()
                .get(new GenericType<List<ShirtStyleDTO>>() {});
        
        assertNotNull(styles);
        assertEquals(2, styles.size());
        assertEquals("style1", styles.get(0).getName());
        assertEquals("url1", styles.get(0).getImageUrl());
    }
    
    @Test
    public void testOrderShirts() {
        OrderDTO orderDTO = new OrderDTO("style1", 5);
        
        Response response = resources.target("/shop/order")
                .request()
                .post(Entity.json(orderDTO));
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        verify(stylingApi).makeShirts(eq("style1"), eq(5));
        
        ArgumentCaptor<PackedShirtsDTO> packedShirtsCaptor = ArgumentCaptor.forClass(PackedShirtsDTO.class);
        verify(deliveryApi).dispatch(anyString(), packedShirtsCaptor.capture());
        assertEquals(5, packedShirtsCaptor.getValue().getPackedShirts());
    }
    
    @Test
    public void testGetOrderStatus() {
        Response response = resources.target("/shop/status/12345")
                .request()
                .get();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        verify(deliveryApi).track(eq("12345"));
    }
    
    @Test
    public void testCancelOrder() {
        String orderNum = "12345";
        
        Response response = resources.target("/shop/cancel")
                .request()
                .post(Entity.json(orderNum));
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        verify(deliveryApi).cancel(eq("12345"));
    }
}

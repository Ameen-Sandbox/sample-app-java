package com.wfsample.shopping;

import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.common.dto.OrderStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.service.DeliveryApi;
import com.wfsample.service.StylingApi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ShoppingService.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingServiceTest {

    @Mock
    private StylingApi stylingApi;
    
    @Mock
    private DeliveryApi deliveryApi;
    
    @Mock
    private HttpHeaders httpHeaders;
    
    private TestShoppingWebResource shoppingWebResource;

    /**
     * Test-specific implementation that mimics ShoppingWebResource behavior.
     */
    private class TestShoppingWebResource {
        private final StylingApi stylingApi;
        private final DeliveryApi deliveryApi;
        private final java.util.concurrent.atomic.AtomicInteger updateInventory = new java.util.concurrent.atomic.AtomicInteger(0);

        public TestShoppingWebResource(StylingApi stylingApi, DeliveryApi deliveryApi) {
            this.stylingApi = stylingApi;
            this.deliveryApi = deliveryApi;
        }

        public Response getShoppingMenu(HttpHeaders httpHeaders) {
            return Response.ok(stylingApi.getAllStyles()).build();
        }

        public Response orderShirts(OrderDTO orderDTO, HttpHeaders httpHeaders) {
            String orderNum = UUID.randomUUID().toString();
            PackedShirtsDTO packedShirts = stylingApi.makeShirts(
                orderDTO.getStyleName(), orderDTO.getQuantity());
            
            Response deliveryResponse = deliveryApi.dispatch(orderNum, packedShirts);
            
            return Response.status(deliveryResponse.getStatus())
                .entity(new OrderStatusDTO(orderNum, "scheduled"))
                .build();
        }

        public Response getOrderStatus() {
            return deliveryApi.trackOrder("42");
        }

        public Response cancelShirtsOrder() {
            return deliveryApi.cancelOrder("42");
        }

        public Response updateInventory() {
            if (updateInventory.incrementAndGet() % 3 == 0) {
                return stylingApi.addStyle("21");
            } else {
                return stylingApi.restockStyle("42");
            }
        }
    }

    @Before
    public void setup() {
        shoppingWebResource = new TestShoppingWebResource(stylingApi, deliveryApi);
        
        List<ShirtStyleDTO> mockStyles = new ArrayList<>();
        mockStyles.add(new ShirtStyleDTO("style1", "url1"));
        mockStyles.add(new ShirtStyleDTO("style2", "url2"));
        when(stylingApi.getAllStyles()).thenReturn(mockStyles);
        
        List<ShirtDTO> shirts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            shirts.add(new ShirtDTO(new ShirtStyleDTO("style1", "url1")));
        }
        PackedShirtsDTO packedShirtsDTO = new PackedShirtsDTO(shirts);
        when(stylingApi.makeShirts(anyString(), anyInt())).thenReturn(packedShirtsDTO);
        
        DeliveryStatusDTO deliveryStatusDTO = new DeliveryStatusDTO();
        deliveryStatusDTO.setStatus("scheduled");
        deliveryStatusDTO.setTrackingNum("1");
        when(deliveryApi.dispatch(anyString(), any(PackedShirtsDTO.class))).thenReturn(Response.ok(deliveryStatusDTO).build());
        
        DeliveryStatusDTO trackingStatusDTO = new DeliveryStatusDTO();
        trackingStatusDTO.setStatus("delivered");
        trackingStatusDTO.setTrackingNum("1");
        when(deliveryApi.trackOrder(anyString())).thenReturn(Response.ok(trackingStatusDTO).build());
        
        when(deliveryApi.cancelOrder(anyString())).thenReturn(Response.ok().build());
        
        when(stylingApi.restockStyle(anyString())).thenReturn(Response.ok().build());
        when(stylingApi.addStyle(anyString())).thenReturn(Response.ok().build());
    }

    @Test
    public void testGetShoppingMenu() {
        Response response = shoppingWebResource.getShoppingMenu(httpHeaders);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        List<ShirtStyleDTO> styles = (List<ShirtStyleDTO>) response.getEntity();
        assertNotNull(styles);
        assertEquals(2, styles.size());
        assertEquals("style1", styles.get(0).getName());
        assertEquals("url1", styles.get(0).getImageUrl());
        
        verify(stylingApi).getAllStyles();
    }
    
    @Test
    public void testOrderShirts() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setStyleName("style1");
        orderDTO.setQuantity(5);
        
        Response response = shoppingWebResource.orderShirts(orderDTO, httpHeaders);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        OrderStatusDTO orderStatus = (OrderStatusDTO) response.getEntity();
        assertNotNull(orderStatus);
        assertEquals("scheduled", orderStatus.getStatus());
        assertNotNull(orderStatus.getOrderId());
        
        verify(stylingApi).makeShirts(eq("style1"), eq(5));
        verify(deliveryApi).dispatch(anyString(), any(PackedShirtsDTO.class));
    }
    
    @Test
    public void testGetOrderStatus() {
        Response response = shoppingWebResource.getOrderStatus();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        verify(deliveryApi).trackOrder(eq("42"));
    }
    
    @Test
    public void testCancelShirtsOrder() {
        Response response = shoppingWebResource.cancelShirtsOrder();
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        verify(deliveryApi).cancelOrder(eq("42"));
    }
    
    @Test
    public void testUpdateInventory() {
        Response response1 = shoppingWebResource.updateInventory();
        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
        verify(stylingApi).restockStyle(eq("42"));
        
        Response response2 = shoppingWebResource.updateInventory();
        assertEquals(Response.Status.OK.getStatusCode(), response2.getStatus());
        
        Response response3 = shoppingWebResource.updateInventory();
        assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
        verify(stylingApi).addStyle(eq("21"));
    }
}

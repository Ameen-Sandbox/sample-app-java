package com.wfsample.common.dto;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for OrderDTO class.
 */
public class OrderDTOTest {

    @Test
    public void testOrderDTOSettersAndGetters() {
        OrderDTO orderDTO = new OrderDTO();
        
        String styleName = "testStyle";
        int quantity = 5;
        orderDTO.setStyleName(styleName);
        orderDTO.setQuantity(quantity);
        
        assertEquals(styleName, orderDTO.getStyleName());
        assertEquals(quantity, orderDTO.getQuantity());
    }
    
    @Test
    public void testOrderDTOUpdateValues() {
        OrderDTO orderDTO = new OrderDTO();
        
        orderDTO.setStyleName("initialStyle");
        orderDTO.setQuantity(1);
        
        String newStyleName = "updatedStyle";
        int newQuantity = 10;
        orderDTO.setStyleName(newStyleName);
        orderDTO.setQuantity(newQuantity);
        
        assertEquals(newStyleName, orderDTO.getStyleName());
        assertEquals(newQuantity, orderDTO.getQuantity());
    }
    
    @Test
    public void testDefaultValues() {
        OrderDTO orderDTO = new OrderDTO();
        
        assertNull(orderDTO.getStyleName());
        assertEquals(0, orderDTO.getQuantity());
    }
}

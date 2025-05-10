package com.wfsample.common.dto;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for OrderDTO class.
 */
public class OrderDTOTest {

    @Test
    public void testOrderDTOConstructorAndGetters() {
        String styleName = "testStyle";
        int quantity = 5;
        OrderDTO orderDTO = new OrderDTO(styleName, quantity);
        
        assertEquals(styleName, orderDTO.getStyleName());
        assertEquals(quantity, orderDTO.getQuantity());
    }
    
    @Test
    public void testOrderDTOSetters() {
        OrderDTO orderDTO = new OrderDTO("initialStyle", 1);
        
        String newStyleName = "updatedStyle";
        int newQuantity = 10;
        orderDTO.setStyleName(newStyleName);
        orderDTO.setQuantity(newQuantity);
        
        assertEquals(newStyleName, orderDTO.getStyleName());
        assertEquals(newQuantity, orderDTO.getQuantity());
    }
    
    @Test
    public void testDefaultConstructor() {
        OrderDTO orderDTO = new OrderDTO();
        
        assertNull(orderDTO.getStyleName());
        assertEquals(0, orderDTO.getQuantity());
    }
}

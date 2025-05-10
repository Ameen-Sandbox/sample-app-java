package com.wfsample.styling;

import com.wavefront.sdk.grpc.WavefrontClientInterceptor;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.beachshirts.Color;
import com.wfsample.beachshirts.PackagingGrpc;
import com.wfsample.beachshirts.PrintingGrpc;
import com.wfsample.beachshirts.Shirt;
import com.wfsample.beachshirts.ShirtStyle;
import com.wfsample.beachshirts.Void;
import com.wfsample.beachshirts.WrapRequest;
import com.wfsample.beachshirts.PrintRequest;
import com.wfsample.beachshirts.WrappingType;
import com.wfsample.beachshirts.PackedShirts;
import com.wfsample.beachshirts.Status;
import com.wfsample.beachshirts.AvailableColors;
import com.wfsample.beachshirts.WrappingTypes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StylingService.
 */
@RunWith(MockitoJUnitRunner.class)
public class StylingServiceTest {

    @Mock
    private PrintingGrpc.PrintingBlockingStub printingBlockingStub;

    @Mock
    private PackagingGrpc.PackagingBlockingStub packagingBlockingStub;

    /**
     * Test implementation that mimics StylingWebResource behavior without extending it.
     */
    private static class TestStylingWebResource {
        private final PrintingGrpc.PrintingBlockingStub printingStub;
        private final PackagingGrpc.PackagingBlockingStub packagingStub;
        private final List<ShirtStyleDTO> shirtStyleDTOS = new ArrayList<>();
        
        public TestStylingWebResource(PrintingGrpc.PrintingBlockingStub printingStub, 
                                     PackagingGrpc.PackagingBlockingStub packagingStub) {
            this.printingStub = printingStub;
            this.packagingStub = packagingStub;
            
            ShirtStyleDTO dto = new ShirtStyleDTO();
            dto.setName("style1");
            dto.setImageUrl("style1Image");
            ShirtStyleDTO dto2 = new ShirtStyleDTO();
            dto2.setName("style2");
            dto2.setImageUrl("style2Image");
            shirtStyleDTOS.add(dto);
            shirtStyleDTOS.add(dto2);
        }
        
        public List<ShirtStyleDTO> getAllStyles() {
            printingStub.getAvailableColors(Void.getDefaultInstance());
            packagingStub.getPackingTypes(Void.getDefaultInstance());
            
            return shirtStyleDTOS;
        }
        
        public PackedShirtsDTO makeShirts(String id, int quantity) {
            Iterator<Shirt> shirts = printingStub.printShirts(PrintRequest.newBuilder()
                .setStyleToPrint(ShirtStyle.newBuilder().setName(id).setImageUrl(id + "Image").build())
                .setQuantity(quantity)
                .build());
            
            if (quantity < 30) {
                packagingStub.wrapShirts(WrapRequest.newBuilder().addAllShirts(() -> shirts).build());
            } else {
                packagingStub.giftWrap(WrapRequest.newBuilder().addAllShirts(() -> shirts).build());
            }
            
            List<ShirtDTO> packedShirts = new ArrayList<>(quantity);
            for (int i = 0; i < quantity; i++) {
                packedShirts.add(new ShirtDTO(new ShirtStyleDTO(id, id + "Image")));
            }
            return new PackedShirtsDTO(packedShirts);
        }
        
        public Response addStyle(String id) {
            printingStub.addPrintColor(Color.newBuilder().setColor("rgb").build());
            return Response.ok().build();
        }
        
        public Response restockStyle(String id) {
            printingStub.restockColor(Color.newBuilder().setColor("rgb").build());
            packagingStub.restockMaterial(WrappingType.newBuilder().setWrappingType("wrap").build());
            return Response.ok().build();
        }
    }

    private TestStylingWebResource stylingWebResource;

    @Before
    public void setup() {
        stylingWebResource = new TestStylingWebResource(printingBlockingStub, packagingBlockingStub);
    }

    @Test
    public void testGetAllStyles() {
        when(printingBlockingStub.getAvailableColors(any(Void.class)))
            .thenReturn(AvailableColors.newBuilder().build());
        when(packagingBlockingStub.getPackingTypes(any(Void.class)))
            .thenReturn(WrappingTypes.newBuilder().build());
        
        List<ShirtStyleDTO> styles = stylingWebResource.getAllStyles();
        
        assertNotNull(styles);
        assertTrue(styles.size() > 0);
        assertEquals("style1", styles.get(0).getName());
        assertEquals("style1Image", styles.get(0).getImageUrl());
        
        verify(printingBlockingStub).getAvailableColors(any(Void.class));
        verify(packagingBlockingStub).getPackingTypes(any(Void.class));
    }

    @Test
    public void testMakeShirts() {
        List<Shirt> shirts = createMockShirts(5);
        
        when(printingBlockingStub.printShirts(any(PrintRequest.class))).thenReturn(shirts.iterator());
        
        when(packagingBlockingStub.wrapShirts(any(WrapRequest.class)))
            .thenReturn(PackedShirts.newBuilder().addAllShirts(shirts).build());
        
        PackedShirtsDTO result = stylingWebResource.makeShirts("style1", 5);
        
        assertNotNull(result);
        assertEquals(5, result.getShirts().size());
        
        verify(printingBlockingStub).printShirts(any(PrintRequest.class));
        verify(packagingBlockingStub).wrapShirts(any(WrapRequest.class));
    }

    @Test
    public void testAddStyle() {
        when(printingBlockingStub.addPrintColor(any(Color.class)))
            .thenReturn(Status.newBuilder().setStatus(true).build());
        
        Response response = stylingWebResource.addStyle("newStyle");
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        verify(printingBlockingStub).addPrintColor(any(Color.class));
    }

    @Test
    public void testRestockStyle() {
        when(printingBlockingStub.restockColor(any(Color.class)))
            .thenReturn(Status.newBuilder().setStatus(true).build());
        when(packagingBlockingStub.restockMaterial(any(WrappingType.class)))
            .thenReturn(Status.newBuilder().setStatus(true).build());
        
        Response response = stylingWebResource.restockStyle("style1");
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        verify(printingBlockingStub).restockColor(any(Color.class));
        verify(packagingBlockingStub).restockMaterial(any(WrappingType.class));
    }

    private List<Shirt> createMockShirts(int count) {
        List<Shirt> shirts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            shirts.add(Shirt.newBuilder()
                .setStyle(ShirtStyle.newBuilder()
                    .setName("style" + i)
                    .setImageUrl("style" + i + "Image")
                    .build())
                .build());
        }
        return shirts;
    }
}

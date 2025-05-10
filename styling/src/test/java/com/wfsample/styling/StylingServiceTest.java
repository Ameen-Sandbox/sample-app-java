package com.wfsample.styling;

import com.wfsample.common.dto.OrderDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.beachshirts.Color;
import com.wfsample.beachshirts.PackagingGrpc;
import com.wfsample.beachshirts.PrintingGrpc;
import com.wfsample.beachshirts.Shirt;
import com.wfsample.beachshirts.ShirtStyle;
import com.wfsample.beachshirts.Void;
import com.wfsample.beachshirts.WrapRequest;
import com.wfsample.beachshirts.PrintRequest;
import com.wfsample.beachshirts.PrintResponse;
import com.wfsample.beachshirts.WrappingType;
import com.wfsample.beachshirts.PackedShirts;

import io.dropwizard.testing.junit.ResourceTestRule;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StylingService.
 */
public class StylingServiceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Mock
    private PrintingGrpc.PrintingBlockingStub printingBlockingStub;

    @Mock
    private PackagingGrpc.PackagingBlockingStub packagingBlockingStub;

    private StylingWebResource stylingWebResource;

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new StylingWebResource(null, null))
            .build();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        stylingWebResource = new StylingWebResource(printingBlockingStub, packagingBlockingStub);
    }

    @Test
    public void testGetStyles() {
        List<ShirtStyleDTO> styles = stylingWebResource.getAllStyles();
        
        assertNotNull(styles);
        
        assert(styles.size() > 0);
        
        ShirtStyleDTO firstStyle = styles.get(0);
        assertNotNull(firstStyle.getName());
        assertNotNull(firstStyle.getImageUrl());
    }

    @Test
    public void testMakeShirts() {
        PrintResponse printResponse = PrintResponse.newBuilder()
                .addAllShirts(createMockShirts(5))
                .build();
        when(printingBlockingStub.printShirts(any(PrintRequest.class))).thenReturn(printResponse);
        
        PackedShirts packedShirts = PackedShirts.newBuilder()
                .setPackedShirts(5)
                .build();
        when(packagingBlockingStub.wrapShirts(any(WrapRequest.class))).thenReturn(packedShirts);
        
        Response response = stylingWebResource.makeShirts("style1", 5);
        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        ArgumentCaptor<PrintRequest> printRequestCaptor = ArgumentCaptor.forClass(PrintRequest.class);
        verify(printingBlockingStub).printShirts(printRequestCaptor.capture());
        PrintRequest capturedPrintRequest = printRequestCaptor.getValue();
        assertEquals("style1", capturedPrintRequest.getStyleName());
        assertEquals(5, capturedPrintRequest.getQuantity());
        
        ArgumentCaptor<WrapRequest> wrapRequestCaptor = ArgumentCaptor.forClass(WrapRequest.class);
        verify(packagingBlockingStub).wrapShirts(wrapRequestCaptor.capture());
        WrapRequest capturedWrapRequest = wrapRequestCaptor.getValue();
        assertEquals(5, capturedWrapRequest.getShirtsCount());
    }

    private List<Shirt> createMockShirts(int count) {
        List<Shirt> shirts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            shirts.add(Shirt.newBuilder()
                    .setStyle(ShirtStyle.newBuilder().setName("style" + i).setColor(Color.newBuilder().setName("blue").build()).build())
                    .build());
        }
        return shirts;
    }
}

package com.wfsample.printing;

import com.wfsample.beachshirts.Color;
import com.wfsample.beachshirts.PrintRequest;
import com.wfsample.beachshirts.PrintResponse;
import com.wfsample.beachshirts.PrintingGrpc;
import com.wfsample.beachshirts.Shirt;
import com.wfsample.beachshirts.ShirtStyle;
import com.wfsample.beachshirts.Void;
import com.wfsample.common.GrpcServiceConfig;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for PrintingService.
 */
public class PrintingServiceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private PrintingGrpc.PrintingBlockingStub blockingStub;
    
    @Mock
    private StreamObserver<PrintResponse> responseObserver;
    
    private PrintingImpl printingImpl;
    private String serverName;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        serverName = InProcessServerBuilder.generateName();
        
        GrpcServiceConfig config = new GrpcServiceConfig();
        config.setPort(0); // Not used in tests
        printingImpl = new PrintingImpl(config);
        
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(printingImpl)
                .build()
                .start());

        blockingStub = PrintingGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder
                        .forName(serverName)
                        .directExecutor()
                        .build()));
    }

    @Test
    public void testPrintShirts() {
        String styleName = "testStyle";
        int quantity = 5;
        PrintRequest request = PrintRequest.newBuilder()
                .setStyleName(styleName)
                .setQuantity(quantity)
                .build();
        
        PrintResponse response = blockingStub.printShirts(request);
        
        assertNotNull(response);
        assertEquals(quantity, response.getShirtsCount());
        
        for (Shirt shirt : response.getShirtsList()) {
            assertEquals(styleName, shirt.getStyle().getName());
        }
    }
    
    @Test
    public void testAddPrintColor() {
        Color color = Color.newBuilder().setName("newColor").build();
        
        Void response = blockingStub.addPrintColor(color);
        
        assertNotNull(response);
    }
    
    @Test
    public void testRestockColor() {
        Color color = Color.newBuilder().setName("existingColor").build();
        
        Void response = blockingStub.restockColor(color);
        
        assertNotNull(response);
    }
    
    @Test
    public void testGetAvailableColors() {
        Void request = Void.newBuilder().build();
        PrintingGrpc.PrintingStub asyncStub = PrintingGrpc.newStub(
                grpcCleanup.register(InProcessChannelBuilder
                        .forName(serverName)
                        .directExecutor()
                        .build()));
        
        ArgumentCaptor<Color> colorCaptor = ArgumentCaptor.forClass(Color.class);
        doAnswer(invocation -> null).when(responseObserver).onCompleted();
        
        asyncStub.getAvailableColors(request, responseObserver);
        
        verify(responseObserver).onCompleted();
    }
}

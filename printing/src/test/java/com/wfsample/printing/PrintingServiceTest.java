package com.wfsample.printing;

import com.wfsample.beachshirts.AvailableColors;
import com.wfsample.beachshirts.Color;
import com.wfsample.beachshirts.PrintRequest;
import com.wfsample.beachshirts.PrintingGrpc;
import com.wfsample.beachshirts.Shirt;
import com.wfsample.beachshirts.ShirtStyle;
import com.wfsample.beachshirts.Status;
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
import java.util.Random;

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
    private StreamObserver<AvailableColors> responseObserver;
    
    private TestPrintingImpl printingImpl;
    private String serverName;
    
    /**
     * Test implementation of PrintingGrpc.PrintingImplBase for unit testing.
     */
    private static class TestPrintingImpl extends PrintingGrpc.PrintingImplBase {
        private final Random rand = new Random(0L);
        
        public TestPrintingImpl(GrpcServiceConfig config) {
        }
        
        @Override
        public void printShirts(PrintRequest request, StreamObserver<Shirt> responseObserver) {
            ShirtStyle style = ShirtStyle.newBuilder()
                .setName(request.getStyleToPrint().getName())
                .setImageUrl(request.getStyleToPrint().getName() + "Image")
                .build();
                
            for (int i = 0; i < request.getQuantity(); i++) {
                responseObserver.onNext(Shirt.newBuilder().setStyle(style).build());
            }
            responseObserver.onCompleted();
        }
        
        @Override
        public void addPrintColor(Color request, StreamObserver<Status> responseObserver) {
            responseObserver.onNext(Status.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        }
        
        @Override
        public void restockColor(Color request, StreamObserver<Status> responseObserver) {
            responseObserver.onNext(Status.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        }
        
        @Override
        public void getAvailableColors(Void request, StreamObserver<AvailableColors> responseObserver) {
            responseObserver.onNext(AvailableColors.newBuilder()
                .addColors(Color.newBuilder().setColor("rgb").build())
                .build());
            responseObserver.onCompleted();
        }
    }

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        serverName = InProcessServerBuilder.generateName();
        
        GrpcServiceConfig config = new GrpcServiceConfig();
        printingImpl = new TestPrintingImpl(config);
        
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
                .setStyleToPrint(ShirtStyle.newBuilder().setName(styleName).build())
                .setQuantity(quantity)
                .build();
        
        List<Shirt> shirts = new ArrayList<>();
        blockingStub.printShirts(request).forEachRemaining(shirts::add);
        
        assertNotNull(shirts);
        assertEquals(quantity, shirts.size());
        
        for (Shirt shirt : shirts) {
            assertEquals(styleName, shirt.getStyle().getName());
        }
    }
    
    @Test
    public void testAddPrintColor() {
        Color color = Color.newBuilder().setColor("newColor").build();
        
        Status response = blockingStub.addPrintColor(color);
        
        assertNotNull(response);
        assertEquals(true, response.getStatus());
    }
    
    @Test
    public void testRestockColor() {
        Color color = Color.newBuilder().setColor("existingColor").build();
        
        Status response = blockingStub.restockColor(color);
        
        assertNotNull(response);
        assertEquals(true, response.getStatus());
    }
    
    @Test
    public void testGetAvailableColors() {
        Void request = Void.newBuilder().build();
        PrintingGrpc.PrintingStub asyncStub = PrintingGrpc.newStub(
                grpcCleanup.register(InProcessChannelBuilder
                        .forName(serverName)
                        .directExecutor()
                        .build()));
        
        asyncStub.getAvailableColors(request, responseObserver);
        
        verify(responseObserver).onNext(any(AvailableColors.class));
        verify(responseObserver).onCompleted();
    }
}

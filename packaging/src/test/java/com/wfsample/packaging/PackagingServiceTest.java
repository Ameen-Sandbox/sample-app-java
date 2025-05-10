package com.wfsample.packaging;

import com.wfsample.beachshirts.PackagingGrpc;
import com.wfsample.beachshirts.PackedShirts;
import com.wfsample.beachshirts.Shirt;
import com.wfsample.beachshirts.ShirtStyle;
import com.wfsample.beachshirts.Color;
import com.wfsample.beachshirts.WrapRequest;
import com.wfsample.beachshirts.WrappingType;
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
 * Unit tests for PackagingService.
 */
public class PackagingServiceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private PackagingGrpc.PackagingBlockingStub blockingStub;
    
    @Mock
    private StreamObserver<WrappingType> responseObserver;
    
    private PackagingImpl packagingImpl;
    private String serverName;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        serverName = InProcessServerBuilder.generateName();
        
        GrpcServiceConfig config = new GrpcServiceConfig();
        config.setPort(0); // Not used in tests
        packagingImpl = new PackagingImpl(config);
        
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(packagingImpl)
                .build()
                .start());

        blockingStub = PackagingGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder
                        .forName(serverName)
                        .directExecutor()
                        .build()));
    }

    @Test
    public void testWrapShirts() {
        WrapRequest request = WrapRequest.newBuilder()
                .addAllShirts(createMockShirts(5))
                .build();
        
        PackedShirts response = blockingStub.wrapShirts(request);
        
        assertNotNull(response);
        assertEquals(5, response.getPackedShirts());
    }
    
    @Test
    public void testGiftWrap() {
        WrapRequest request = WrapRequest.newBuilder()
                .addAllShirts(createMockShirts(3))
                .build();
        
        PackedShirts response = blockingStub.giftWrap(request);
        
        assertNotNull(response);
        assertEquals(3, response.getPackedShirts());
    }
    
    @Test
    public void testRestockMaterial() {
        WrappingType wrappingType = WrappingType.newBuilder()
                .setWrappingType("standardPackaging")
                .build();
        
        Void response = blockingStub.restockMaterial(wrappingType);
        
        assertNotNull(response);
    }
    
    @Test
    public void testGetPackingTypes() {
        Void request = Void.newBuilder().build();
        PackagingGrpc.PackagingStub asyncStub = PackagingGrpc.newStub(
                grpcCleanup.register(InProcessChannelBuilder
                        .forName(serverName)
                        .directExecutor()
                        .build()));
        
        ArgumentCaptor<WrappingType> wrappingTypeCaptor = ArgumentCaptor.forClass(WrappingType.class);
        doAnswer(invocation -> null).when(responseObserver).onCompleted();
        
        asyncStub.getPackingTypes(request, responseObserver);
        
        verify(responseObserver).onCompleted();
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

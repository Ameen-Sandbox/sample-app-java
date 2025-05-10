package com.wfsample.packaging;

import com.wfsample.beachshirts.GiftPack;
import com.wfsample.beachshirts.PackagingGrpc;
import com.wfsample.beachshirts.PackedShirts;
import com.wfsample.beachshirts.Shirt;
import com.wfsample.beachshirts.ShirtStyle;
import com.wfsample.beachshirts.Status;
import com.wfsample.beachshirts.WrapRequest;
import com.wfsample.beachshirts.WrappingType;
import com.wfsample.beachshirts.WrappingTypes;
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
 * Unit tests for PackagingService.
 */
public class PackagingServiceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private PackagingGrpc.PackagingBlockingStub blockingStub;
    
    @Mock
    private StreamObserver<WrappingTypes> responseObserver;
    
    private TestPackagingImpl packagingImpl;
    private String serverName;
    
    /**
     * Test implementation of PackagingGrpc.PackagingImplBase for unit testing.
     */
    private static class TestPackagingImpl extends PackagingGrpc.PackagingImplBase {
        private final Random rand = new Random(0L);
        
        public TestPackagingImpl(GrpcServiceConfig config) {
        }
        
        @Override
        public void wrapShirts(WrapRequest request, StreamObserver<PackedShirts> responseObserver) {
            int count = request.getShirtsCount();
            PackedShirts packedShirts = PackedShirts.newBuilder()
                .addAllShirts(request.getShirtsList())
                .build();
            responseObserver.onNext(packedShirts);
            responseObserver.onCompleted();
        }
        
        @Override
        public void giftWrap(WrapRequest request, StreamObserver<GiftPack> responseObserver) {
            GiftPack giftPack = GiftPack.newBuilder()
                .setGiftMaterial(com.google.protobuf.ByteString.copyFromUtf8("gift material"))
                .build();
            responseObserver.onNext(giftPack);
            responseObserver.onCompleted();
        }
        
        @Override
        public void restockMaterial(WrappingType request, StreamObserver<Status> responseObserver) {
            responseObserver.onNext(Status.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        }
        
        @Override
        public void getPackingTypes(Void request, StreamObserver<WrappingTypes> responseObserver) {
            responseObserver.onNext(WrappingTypes.newBuilder()
                .addWrappingType(WrappingType.newBuilder().setWrappingType("standardPackaging").build())
                .addWrappingType(WrappingType.newBuilder().setWrappingType("giftWrap").build())
                .build());
            responseObserver.onCompleted();
        }
    }

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        serverName = InProcessServerBuilder.generateName();
        
        GrpcServiceConfig config = new GrpcServiceConfig();
        packagingImpl = new TestPackagingImpl(config);
        
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
        assertEquals(5, response.getShirtsCount());
    }
    
    @Test
    public void testGiftWrap() {
        WrapRequest request = WrapRequest.newBuilder()
                .addAllShirts(createMockShirts(3))
                .build();
        
        GiftPack response = blockingStub.giftWrap(request);
        
        assertNotNull(response);
        assertNotNull(response.getGiftMaterial());
    }
    
    @Test
    public void testRestockMaterial() {
        WrappingType wrappingType = WrappingType.newBuilder()
                .setWrappingType("standardPackaging")
                .build();
        
        Status response = blockingStub.restockMaterial(wrappingType);
        
        assertNotNull(response);
        assertEquals(true, response.getStatus());
    }
    
    @Test
    public void testGetPackingTypes() {
        Void request = Void.newBuilder().build();
        PackagingGrpc.PackagingStub asyncStub = PackagingGrpc.newStub(
                grpcCleanup.register(InProcessChannelBuilder
                        .forName(serverName)
                        .directExecutor()
                        .build()));
        
        asyncStub.getPackingTypes(request, responseObserver);
        
        verify(responseObserver).onNext(any(WrappingTypes.class));
        verify(responseObserver).onCompleted();
    }
    
    private List<Shirt> createMockShirts(int count) {
        List<Shirt> shirts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            shirts.add(Shirt.newBuilder()
                    .setStyle(ShirtStyle.newBuilder().setName("style" + i).build())
                    .build());
        }
        return shirts;
    }
}

package com.solarwinds.msp.relay;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.21.0)",
    comments = "Source: relay.proto")
public final class PublisherGrpc {

  private PublisherGrpc() {}

  public static final String SERVICE_NAME = "msp.relay.Publisher";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.solarwinds.msp.relay.Relay.PublishRequest,
      com.solarwinds.msp.relay.Relay.PublishResponse> getPublishMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Publish",
      requestType = com.solarwinds.msp.relay.Relay.PublishRequest.class,
      responseType = com.solarwinds.msp.relay.Relay.PublishResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.solarwinds.msp.relay.Relay.PublishRequest,
      com.solarwinds.msp.relay.Relay.PublishResponse> getPublishMethod() {
    io.grpc.MethodDescriptor<com.solarwinds.msp.relay.Relay.PublishRequest, com.solarwinds.msp.relay.Relay.PublishResponse> getPublishMethod;
    if ((getPublishMethod = PublisherGrpc.getPublishMethod) == null) {
      synchronized (PublisherGrpc.class) {
        if ((getPublishMethod = PublisherGrpc.getPublishMethod) == null) {
          PublisherGrpc.getPublishMethod = getPublishMethod = 
              io.grpc.MethodDescriptor.<com.solarwinds.msp.relay.Relay.PublishRequest, com.solarwinds.msp.relay.Relay.PublishResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "msp.relay.Publisher", "Publish"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.solarwinds.msp.relay.Relay.PublishRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.solarwinds.msp.relay.Relay.PublishResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new PublisherMethodDescriptorSupplier("Publish"))
                  .build();
          }
        }
     }
     return getPublishMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PublisherStub newStub(io.grpc.Channel channel) {
    return new PublisherStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PublisherBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new PublisherBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PublisherFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new PublisherFutureStub(channel);
  }

  /**
   */
  public static abstract class PublisherImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Publishes an event.
     * Common errors generated by server conditions:
     * INTERNAL - An internal error occurred, preventing the events from being relayed.
     *    
     * Likely a problem publishing the events to the event bus.
     * UNAUTHENTICATED - The token provided with this customer does not have authority to publish
     * UNAVAILABLE - The service is not available, possibly because it is temporarily overloaded
     * </pre>
     */
    public void publish(com.solarwinds.msp.relay.Relay.PublishRequest request,
        io.grpc.stub.StreamObserver<com.solarwinds.msp.relay.Relay.PublishResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getPublishMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPublishMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.solarwinds.msp.relay.Relay.PublishRequest,
                com.solarwinds.msp.relay.Relay.PublishResponse>(
                  this, METHODID_PUBLISH)))
          .build();
    }
  }

  /**
   */
  public static final class PublisherStub extends io.grpc.stub.AbstractStub<PublisherStub> {
    private PublisherStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PublisherStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PublisherStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PublisherStub(channel, callOptions);
    }

    /**
     * <pre>
     * Publishes an event.
     * Common errors generated by server conditions:
     * INTERNAL - An internal error occurred, preventing the events from being relayed.
     *    
     * Likely a problem publishing the events to the event bus.
     * UNAUTHENTICATED - The token provided with this customer does not have authority to publish
     * UNAVAILABLE - The service is not available, possibly because it is temporarily overloaded
     * </pre>
     */
    public void publish(com.solarwinds.msp.relay.Relay.PublishRequest request,
        io.grpc.stub.StreamObserver<com.solarwinds.msp.relay.Relay.PublishResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPublishMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class PublisherBlockingStub extends io.grpc.stub.AbstractStub<PublisherBlockingStub> {
    private PublisherBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PublisherBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PublisherBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PublisherBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Publishes an event.
     * Common errors generated by server conditions:
     * INTERNAL - An internal error occurred, preventing the events from being relayed.
     *    
     * Likely a problem publishing the events to the event bus.
     * UNAUTHENTICATED - The token provided with this customer does not have authority to publish
     * UNAVAILABLE - The service is not available, possibly because it is temporarily overloaded
     * </pre>
     */
    public com.solarwinds.msp.relay.Relay.PublishResponse publish(com.solarwinds.msp.relay.Relay.PublishRequest request) {
      return blockingUnaryCall(
          getChannel(), getPublishMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class PublisherFutureStub extends io.grpc.stub.AbstractStub<PublisherFutureStub> {
    private PublisherFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PublisherFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PublisherFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PublisherFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Publishes an event.
     * Common errors generated by server conditions:
     * INTERNAL - An internal error occurred, preventing the events from being relayed.
     *    
     * Likely a problem publishing the events to the event bus.
     * UNAUTHENTICATED - The token provided with this customer does not have authority to publish
     * UNAVAILABLE - The service is not available, possibly because it is temporarily overloaded
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.solarwinds.msp.relay.Relay.PublishResponse> publish(
        com.solarwinds.msp.relay.Relay.PublishRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getPublishMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PUBLISH = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final PublisherImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(PublisherImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PUBLISH:
          serviceImpl.publish((com.solarwinds.msp.relay.Relay.PublishRequest) request,
              (io.grpc.stub.StreamObserver<com.solarwinds.msp.relay.Relay.PublishResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class PublisherBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PublisherBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.solarwinds.msp.relay.Relay.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Publisher");
    }
  }

  private static final class PublisherFileDescriptorSupplier
      extends PublisherBaseDescriptorSupplier {
    PublisherFileDescriptorSupplier() {}
  }

  private static final class PublisherMethodDescriptorSupplier
      extends PublisherBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    PublisherMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PublisherGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PublisherFileDescriptorSupplier())
              .addMethod(getPublishMethod())
              .build();
        }
      }
    }
    return result;
  }
}

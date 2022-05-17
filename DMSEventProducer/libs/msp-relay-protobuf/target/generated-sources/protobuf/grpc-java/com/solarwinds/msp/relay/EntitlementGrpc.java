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
public final class EntitlementGrpc {

  private EntitlementGrpc() {}

  public static final String SERVICE_NAME = "msp.relay.Entitlement";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.solarwinds.msp.relay.Relay.EntitlementRequest,
      com.solarwinds.msp.relay.Relay.EntitlementResponse> getVerifyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Verify",
      requestType = com.solarwinds.msp.relay.Relay.EntitlementRequest.class,
      responseType = com.solarwinds.msp.relay.Relay.EntitlementResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.solarwinds.msp.relay.Relay.EntitlementRequest,
      com.solarwinds.msp.relay.Relay.EntitlementResponse> getVerifyMethod() {
    io.grpc.MethodDescriptor<com.solarwinds.msp.relay.Relay.EntitlementRequest, com.solarwinds.msp.relay.Relay.EntitlementResponse> getVerifyMethod;
    if ((getVerifyMethod = EntitlementGrpc.getVerifyMethod) == null) {
      synchronized (EntitlementGrpc.class) {
        if ((getVerifyMethod = EntitlementGrpc.getVerifyMethod) == null) {
          EntitlementGrpc.getVerifyMethod = getVerifyMethod = 
              io.grpc.MethodDescriptor.<com.solarwinds.msp.relay.Relay.EntitlementRequest, com.solarwinds.msp.relay.Relay.EntitlementResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "msp.relay.Entitlement", "Verify"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.solarwinds.msp.relay.Relay.EntitlementRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.solarwinds.msp.relay.Relay.EntitlementResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new EntitlementMethodDescriptorSupplier("Verify"))
                  .build();
          }
        }
     }
     return getVerifyMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EntitlementStub newStub(io.grpc.Channel channel) {
    return new EntitlementStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EntitlementBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new EntitlementBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EntitlementFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new EntitlementFutureStub(channel);
  }

  /**
   */
  public static abstract class EntitlementImplBase implements io.grpc.BindableService {

    /**
     */
    public void verify(com.solarwinds.msp.relay.Relay.EntitlementRequest request,
        io.grpc.stub.StreamObserver<com.solarwinds.msp.relay.Relay.EntitlementResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getVerifyMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getVerifyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.solarwinds.msp.relay.Relay.EntitlementRequest,
                com.solarwinds.msp.relay.Relay.EntitlementResponse>(
                  this, METHODID_VERIFY)))
          .build();
    }
  }

  /**
   */
  public static final class EntitlementStub extends io.grpc.stub.AbstractStub<EntitlementStub> {
    private EntitlementStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EntitlementStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EntitlementStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EntitlementStub(channel, callOptions);
    }

    /**
     */
    public void verify(com.solarwinds.msp.relay.Relay.EntitlementRequest request,
        io.grpc.stub.StreamObserver<com.solarwinds.msp.relay.Relay.EntitlementResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getVerifyMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class EntitlementBlockingStub extends io.grpc.stub.AbstractStub<EntitlementBlockingStub> {
    private EntitlementBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EntitlementBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EntitlementBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EntitlementBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.solarwinds.msp.relay.Relay.EntitlementResponse verify(com.solarwinds.msp.relay.Relay.EntitlementRequest request) {
      return blockingUnaryCall(
          getChannel(), getVerifyMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class EntitlementFutureStub extends io.grpc.stub.AbstractStub<EntitlementFutureStub> {
    private EntitlementFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EntitlementFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EntitlementFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EntitlementFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.solarwinds.msp.relay.Relay.EntitlementResponse> verify(
        com.solarwinds.msp.relay.Relay.EntitlementRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getVerifyMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_VERIFY = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EntitlementImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(EntitlementImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_VERIFY:
          serviceImpl.verify((com.solarwinds.msp.relay.Relay.EntitlementRequest) request,
              (io.grpc.stub.StreamObserver<com.solarwinds.msp.relay.Relay.EntitlementResponse>) responseObserver);
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

  private static abstract class EntitlementBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EntitlementBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.solarwinds.msp.relay.Relay.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Entitlement");
    }
  }

  private static final class EntitlementFileDescriptorSupplier
      extends EntitlementBaseDescriptorSupplier {
    EntitlementFileDescriptorSupplier() {}
  }

  private static final class EntitlementMethodDescriptorSupplier
      extends EntitlementBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    EntitlementMethodDescriptorSupplier(String methodName) {
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
      synchronized (EntitlementGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EntitlementFileDescriptorSupplier())
              .addMethod(getVerifyMethod())
              .build();
        }
      }
    }
    return result;
  }
}

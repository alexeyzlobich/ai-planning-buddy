package com.taskmanager.api.grpc.util;

import com.taskmanager.api.grpc.generated.TaskManagerControllerGrpc;
import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.annotation.GrpcChannel;
import io.micronaut.grpc.server.GrpcServerChannel;

/**
 * This class is used to create gRPC clients for API testing purposes.
 */
@Factory
public class TestGrpcClients {

    @Bean
    TaskManagerControllerGrpc.TaskManagerControllerBlockingStub blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) ManagedChannel channel) {
        return TaskManagerControllerGrpc.newBlockingStub(channel);
    }

}

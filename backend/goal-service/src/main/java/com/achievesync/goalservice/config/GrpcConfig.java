package com.achievesync.goalservice.config;

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.grpc.ServerBuilder;

@Configuration
public class GrpcConfig {

    @Bean
    public GrpcServerConfigurer grpcServerConfigurer() {
        return serverBuilder -> {
            if (serverBuilder instanceof ServerBuilder) {
                ((ServerBuilder<?>) serverBuilder)
                    .maxInboundMessageSize(4 * 1024 * 1024) // 4MB
                    .maxInboundMetadataSize(8 * 1024); // 8KB
            }
        };
    }
}
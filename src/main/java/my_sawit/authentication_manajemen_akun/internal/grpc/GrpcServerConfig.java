package my_sawit.authentication_manajemen_akun.internal.grpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfig {

    @Bean
    @ConditionalOnProperty(prefix = "grpc.server", name = "enabled", havingValue = "true", matchIfMissing = true)
    SmartLifecycle authGrpcServer(
            AuthInternalGrpcService authInternalGrpcService,
            @Value("${grpc.server.port:9091}") int grpcPort
    ) {
        return new AuthGrpcServerLifecycle(authInternalGrpcService, grpcPort);
    }

    private static class AuthGrpcServerLifecycle implements SmartLifecycle {
        private final AuthInternalGrpcService authInternalGrpcService;
        private final int grpcPort;
        private Server server;
        private boolean running;

        private AuthGrpcServerLifecycle(AuthInternalGrpcService authInternalGrpcService, int grpcPort) {
            this.authInternalGrpcService = authInternalGrpcService;
            this.grpcPort = grpcPort;
        }

        @Override
        public void start() {
            try {
                server = NettyServerBuilder.forPort(grpcPort)
                        .addService(authInternalGrpcService)
                        .build()
                        .start();
                running = true;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to start Auth gRPC server on port " + grpcPort, e);
            }
        }

        @Override
        public void stop() {
            if (server != null) {
                server.shutdown();
            }
            running = false;
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }
}

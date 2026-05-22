package my_sawit.authentication_manajemen_akun.internal.grpc;

import my_sawit.authentication_manajemen_akun.common.mapper.UserResponseMapper;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class GrpcServerConfigTest {

    @Test
    void authGrpcServer_StartAndStop_ShouldManageLifecycleState() {
        GrpcServerConfig config = new GrpcServerConfig();
        AuthInternalGrpcService grpcService = createGrpcService();
        SmartLifecycle lifecycle = config.authGrpcServer(grpcService, 0);

        assertFalse(lifecycle.isRunning());

        lifecycle.start();
        assertTrue(lifecycle.isRunning());

        lifecycle.stop();
        assertFalse(lifecycle.isRunning());
    }

    @Test
    void authGrpcServer_StopBeforeStart_ShouldRemainNotRunning() {
        GrpcServerConfig config = new GrpcServerConfig();
        AuthInternalGrpcService grpcService = createGrpcService();
        SmartLifecycle lifecycle = config.authGrpcServer(grpcService, 0);

        lifecycle.stop();

        assertFalse(lifecycle.isRunning());
    }

    @Test
    void authGrpcServer_WhenPortAlreadyInUse_ShouldThrowIllegalStateException() throws IOException {
        int port = findAvailablePort();
        GrpcServerConfig config = new GrpcServerConfig();
        SmartLifecycle firstLifecycle = config.authGrpcServer(createGrpcService(), port);
        SmartLifecycle secondLifecycle = config.authGrpcServer(createGrpcService(), port);

        firstLifecycle.start();
        try {
            assertThrows(IllegalStateException.class, secondLifecycle::start);
            assertFalse(secondLifecycle.isRunning());
        } finally {
            firstLifecycle.stop();
        }
    }

    private AuthInternalGrpcService createGrpcService() {
        MandorProfileRepository mandorProfileRepository = mock(MandorProfileRepository.class);
        return new AuthInternalGrpcService(
                mock(UserRepository.class),
                new UserResponseMapper(mandorProfileRepository)
        );
    }

    private int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}

package my_sawit.authentication_manajemen_akun.internal.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUserByIdRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUsersByIdsRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUsersByIdsResponse;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.UserResponse;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.ValidateUserRoleRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.ValidateUserRoleResponse;
import my_sawit.authentication_manajemen_akun.common.mapper.UserResponseMapper;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInternalGrpcServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MandorProfileRepository mandorProfileRepository;

    @Mock
    private StreamObserver<UserResponse> userResponseObserver;

    @Mock
    private StreamObserver<GetUsersByIdsResponse> usersResponseObserver;

    @Mock
    private StreamObserver<ValidateUserRoleResponse> validateRoleObserver;

    private AuthInternalGrpcService grpcService;
    private User mandor;
    private User supir;

    @BeforeEach
    void setUp() {
        UserResponseMapper userResponseMapper = new UserResponseMapper(mandorProfileRepository);
        grpcService = new AuthInternalGrpcService(userRepository, userResponseMapper);

        mandor = User.builder()
                .id(UUID.randomUUID())
                .username("mandor_andi")
                .fullname("Andi Mandor")
                .email("mandor@sawit.com")
                .role(Role.builder().name("MANDOR").build())
                .build();

        supir = User.builder()
                .id(UUID.randomUUID())
                .username("supir_budi")
                .fullname("Budi Supir")
                .email("supir@sawit.com")
                .role(Role.builder().name("SUPIR").build())
                .mandor(mandor)
                .build();
    }

    @Test
    void getUserById_WhenUserExists_ReturnsFoundUser() {
        when(userRepository.findById(supir.getId())).thenReturn(Optional.of(supir));

        grpcService.getUserById(
                GetUserByIdRequest.newBuilder()
                        .setUserId(supir.getId().toString())
                        .build(),
                userResponseObserver
        );

        ArgumentCaptor<UserResponse> captor = ArgumentCaptor.forClass(UserResponse.class);
        verify(userResponseObserver).onNext(captor.capture());
        verify(userResponseObserver).onCompleted();

        UserResponse response = captor.getValue();
        assertTrue(response.getFound());
        assertEquals(supir.getId().toString(), response.getId());
        assertEquals("supir_budi", response.getUsername());
        assertEquals("Budi Supir", response.getFullname());
        assertEquals("supir@sawit.com", response.getEmail());
        assertEquals("SUPIR", response.getRole());
        assertEquals("Andi Mandor", response.getNamaMandor());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ReturnsNotFoundResponse() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        grpcService.getUserById(
                GetUserByIdRequest.newBuilder()
                        .setUserId(missingId.toString())
                        .build(),
                userResponseObserver
        );

        ArgumentCaptor<UserResponse> captor = ArgumentCaptor.forClass(UserResponse.class);
        verify(userResponseObserver).onNext(captor.capture());
        verify(userResponseObserver).onCompleted();

        UserResponse response = captor.getValue();
        assertFalse(response.getFound());
        assertEquals(missingId.toString(), response.getId());
    }

    @Test
    void getUserById_WhenUserIdInvalid_ReturnsInvalidArgumentError() {
        grpcService.getUserById(
                GetUserByIdRequest.newBuilder()
                        .setUserId("not-a-uuid")
                        .build(),
                userResponseObserver
        );

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(userResponseObserver).onError(captor.capture());
        verify(userResponseObserver, never()).onNext(org.mockito.ArgumentMatchers.any());
        verify(userResponseObserver, never()).onCompleted();

        StatusRuntimeException error = assertInstanceOf(StatusRuntimeException.class, captor.getValue());
        assertEquals(Status.INVALID_ARGUMENT.getCode(), error.getStatus().getCode());
    }

    @Test
    void getUsersByIds_ReturnsOnlyFoundUsers() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findAllById(List.of(supir.getId(), missingId, mandor.getId())))
                .thenReturn(List.of(supir, mandor));

        grpcService.getUsersByIds(
                GetUsersByIdsRequest.newBuilder()
                        .addUserIds(supir.getId().toString())
                        .addUserIds(missingId.toString())
                        .addUserIds(mandor.getId().toString())
                        .build(),
                usersResponseObserver
        );

        ArgumentCaptor<GetUsersByIdsResponse> captor = ArgumentCaptor.forClass(GetUsersByIdsResponse.class);
        verify(usersResponseObserver).onNext(captor.capture());
        verify(usersResponseObserver).onCompleted();

        GetUsersByIdsResponse response = captor.getValue();
        assertEquals(2, response.getUsersCount());
        assertEquals(supir.getId().toString(), response.getUsers(0).getId());
        assertEquals(mandor.getId().toString(), response.getUsers(1).getId());
    }

    @Test
    void getUsersByIds_WhenAnyUserIdInvalid_ReturnsInvalidArgumentError() {
        grpcService.getUsersByIds(
                GetUsersByIdsRequest.newBuilder()
                        .addUserIds(supir.getId().toString())
                        .addUserIds("not-a-uuid")
                        .build(),
                usersResponseObserver
        );

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(usersResponseObserver).onError(captor.capture());
        verify(usersResponseObserver, never()).onNext(org.mockito.ArgumentMatchers.any());
        verify(usersResponseObserver, never()).onCompleted();

        StatusRuntimeException error = assertInstanceOf(StatusRuntimeException.class, captor.getValue());
        assertEquals(Status.INVALID_ARGUMENT.getCode(), error.getStatus().getCode());
    }

    @Test
    void validateUserRole_WhenRoleMatches_ReturnsValid() {
        when(userRepository.findById(mandor.getId())).thenReturn(Optional.of(mandor));

        grpcService.validateUserRole(
                ValidateUserRoleRequest.newBuilder()
                        .setUserId(mandor.getId().toString())
                        .setExpectedRole("MANDOR")
                        .build(),
                validateRoleObserver
        );

        ArgumentCaptor<ValidateUserRoleResponse> captor = ArgumentCaptor.forClass(ValidateUserRoleResponse.class);
        verify(validateRoleObserver).onNext(captor.capture());
        verify(validateRoleObserver).onCompleted();

        ValidateUserRoleResponse response = captor.getValue();
        assertTrue(response.getValid());
        assertEquals("MANDOR", response.getActualRole());
        assertEquals("MANDOR", response.getExpectedRole());
        assertEquals("User role is valid", response.getMessage());
    }

    @Test
    void validateUserRole_WhenRoleDoesNotMatch_ReturnsInvalid() {
        when(userRepository.findById(supir.getId())).thenReturn(Optional.of(supir));

        grpcService.validateUserRole(
                ValidateUserRoleRequest.newBuilder()
                        .setUserId(supir.getId().toString())
                        .setExpectedRole("MANDOR")
                        .build(),
                validateRoleObserver
        );

        ArgumentCaptor<ValidateUserRoleResponse> captor = ArgumentCaptor.forClass(ValidateUserRoleResponse.class);
        verify(validateRoleObserver).onNext(captor.capture());
        verify(validateRoleObserver).onCompleted();

        ValidateUserRoleResponse response = captor.getValue();
        assertFalse(response.getValid());
        assertEquals("SUPIR", response.getActualRole());
        assertEquals("MANDOR", response.getExpectedRole());
        assertEquals("Expected role MANDOR but was SUPIR", response.getMessage());
    }

    @Test
    void validateUserRole_WhenUserHasNoRole_ReturnsInvalidWithEmptyActualRole() {
        User userWithoutRole = User.builder()
                .id(UUID.randomUUID())
                .username("no_role")
                .fullname("No Role")
                .email("norole@sawit.com")
                .role(null)
                .build();
        when(userRepository.findById(userWithoutRole.getId())).thenReturn(Optional.of(userWithoutRole));

        grpcService.validateUserRole(
                ValidateUserRoleRequest.newBuilder()
                        .setUserId(userWithoutRole.getId().toString())
                        .setExpectedRole("MANDOR")
                        .build(),
                validateRoleObserver
        );

        ArgumentCaptor<ValidateUserRoleResponse> captor = ArgumentCaptor.forClass(ValidateUserRoleResponse.class);
        verify(validateRoleObserver).onNext(captor.capture());
        verify(validateRoleObserver).onCompleted();

        ValidateUserRoleResponse response = captor.getValue();
        assertFalse(response.getValid());
        assertEquals("", response.getActualRole());
        assertEquals("Expected role MANDOR but was ", response.getMessage());
    }

    @Test
    void validateUserRole_WhenUserRoleNameIsNull_ReturnsInvalidWithEmptyActualRole() {
        User userWithUnnamedRole = User.builder()
                .id(UUID.randomUUID())
                .username("unnamed_role")
                .fullname("Unnamed Role")
                .email("unnamedrole@sawit.com")
                .role(Role.builder().name(null).build())
                .build();
        when(userRepository.findById(userWithUnnamedRole.getId())).thenReturn(Optional.of(userWithUnnamedRole));

        grpcService.validateUserRole(
                ValidateUserRoleRequest.newBuilder()
                        .setUserId(userWithUnnamedRole.getId().toString())
                        .setExpectedRole("MANDOR")
                        .build(),
                validateRoleObserver
        );

        ArgumentCaptor<ValidateUserRoleResponse> captor = ArgumentCaptor.forClass(ValidateUserRoleResponse.class);
        verify(validateRoleObserver).onNext(captor.capture());
        verify(validateRoleObserver).onCompleted();

        ValidateUserRoleResponse response = captor.getValue();
        assertFalse(response.getValid());
        assertEquals("", response.getActualRole());
        assertEquals("Expected role MANDOR but was ", response.getMessage());
    }

    @Test
    void validateUserRole_WhenUserDoesNotExist_ReturnsInvalid() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        grpcService.validateUserRole(
                ValidateUserRoleRequest.newBuilder()
                        .setUserId(missingId.toString())
                        .setExpectedRole("MANDOR")
                        .build(),
                validateRoleObserver
        );

        ArgumentCaptor<ValidateUserRoleResponse> captor = ArgumentCaptor.forClass(ValidateUserRoleResponse.class);
        verify(validateRoleObserver).onNext(captor.capture());
        verify(validateRoleObserver).onCompleted();

        ValidateUserRoleResponse response = captor.getValue();
        assertFalse(response.getValid());
        assertEquals(missingId.toString(), response.getUserId());
        assertEquals("MANDOR", response.getExpectedRole());
        assertEquals("User not found", response.getMessage());
    }

    @Test
    void validateUserRole_WhenUserIdInvalid_ReturnsInvalidArgumentError() {
        grpcService.validateUserRole(
                ValidateUserRoleRequest.newBuilder()
                        .setUserId("not-a-uuid")
                        .setExpectedRole("MANDOR")
                        .build(),
                validateRoleObserver
        );

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(validateRoleObserver).onError(captor.capture());
        verify(validateRoleObserver, never()).onNext(org.mockito.ArgumentMatchers.any());
        verify(validateRoleObserver, never()).onCompleted();

        StatusRuntimeException error = assertInstanceOf(StatusRuntimeException.class, captor.getValue());
        assertEquals(Status.INVALID_ARGUMENT.getCode(), error.getStatus().getCode());
    }
}

package my_sawit.authentication_manajemen_akun.internal.grpc;

import id.ac.ui.cs.advprog.mysawit.grpc.auth.AuthInternalServiceGrpc;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUserByIdRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUsersByIdsRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUsersByIdsResponse;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.UserResponse;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.ValidateUserRoleRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.ValidateUserRoleResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import my_sawit.authentication_manajemen_akun.common.mapper.UserResponseMapper;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthInternalGrpcService extends AuthInternalServiceGrpc.AuthInternalServiceImplBase {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    public AuthInternalGrpcService(UserRepository userRepository, UserResponseMapper userResponseMapper) {
        this.userRepository = userRepository;
        this.userResponseMapper = userResponseMapper;
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        UUID userId;
        try {
            userId = parseUuid(request.getUserId(), "user_id");
        } catch (IllegalArgumentException e) {
            responseObserver.onError(invalidArgument(e.getMessage()));
            return;
        }

        Optional<User> user = userRepository.findById(userId);

        UserResponse response = user
                .map(this::toGrpcUserResponse)
                .orElseGet(() -> notFoundUserResponse(request.getUserId()));

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUsersByIds(
            GetUsersByIdsRequest request,
            StreamObserver<GetUsersByIdsResponse> responseObserver
    ) {
        List<UUID> userIds;
        try {
            userIds = request.getUserIdsList().stream()
                    .map(userId -> parseUuid(userId, "user_ids"))
                    .toList();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(invalidArgument(e.getMessage()));
            return;
        }

        GetUsersByIdsResponse response = GetUsersByIdsResponse.newBuilder()
                .addAllUsers(userRepository.findAllById(userIds).stream()
                        .map(this::toGrpcUserResponse)
                        .toList())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void validateUserRole(
            ValidateUserRoleRequest request,
            StreamObserver<ValidateUserRoleResponse> responseObserver
    ) {
        UUID userId;
        try {
            userId = parseUuid(request.getUserId(), "user_id");
        } catch (IllegalArgumentException e) {
            responseObserver.onError(invalidArgument(e.getMessage()));
            return;
        }

        Optional<User> user = userRepository.findById(userId);
        String actualRole = user
                .map(this::getRoleName)
                .orElse("");
        boolean valid = user.isPresent() && actualRole.equalsIgnoreCase(request.getExpectedRole());

        ValidateUserRoleResponse response = ValidateUserRoleResponse.newBuilder()
                .setValid(valid)
                .setUserId(request.getUserId())
                .setActualRole(actualRole)
                .setExpectedRole(request.getExpectedRole())
                .setMessage(buildRoleValidationMessage(user.isPresent(), valid, request.getExpectedRole(), actualRole))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private UserResponse toGrpcUserResponse(User user) {
        UserResponseDTO dto = userResponseMapper.toDto(user);

        return UserResponse.newBuilder()
                .setId(toStringOrEmpty(dto.getId()))
                .setUsername(toStringOrEmpty(dto.getUsername()))
                .setFullname(toStringOrEmpty(dto.getFullname()))
                .setEmail(toStringOrEmpty(dto.getEmail()))
                .setRole(toStringOrEmpty(dto.getRole()))
                .setNomorSertifikasi(toStringOrEmpty(dto.getNomorSertifikasi()))
                .setNamaMandor(toStringOrEmpty(dto.getNamaMandor()))
                .setFound(true)
                .build();
    }

    private UserResponse notFoundUserResponse(String userId) {
        return UserResponse.newBuilder()
                .setId(toStringOrEmpty(userId))
                .setFound(false)
                .build();
    }

    private UUID parseUuid(String rawValue, String fieldName) {
        try {
            return UUID.fromString(rawValue);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid UUID", e);
        }
    }

    private RuntimeException invalidArgument(String message) {
        return Status.INVALID_ARGUMENT
                .withDescription(message)
                .asRuntimeException();
    }

    private String buildRoleValidationMessage(
            boolean userFound,
            boolean valid,
            String expectedRole,
            String actualRole
    ) {
        if (!userFound) {
            return "User not found";
        }
        if (valid) {
            return "User role is valid";
        }
        return "Expected role " + expectedRole + " but was " + actualRole;
    }

    private String getRoleName(User user) {
        if (user.getRole() == null || user.getRole().getName() == null) {
            return "";
        }
        return user.getRole().getName();
    }

    private String toStringOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}

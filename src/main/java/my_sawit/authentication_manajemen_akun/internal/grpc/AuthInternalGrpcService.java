package my_sawit.authentication_manajemen_akun.internal.grpc;

import id.ac.ui.cs.advprog.mysawit.grpc.auth.AuthInternalServiceGrpc;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUserByIdRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUsersByIdsRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.GetUsersByIdsResponse;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.UserResponse;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.ValidateUserRoleRequest;
import id.ac.ui.cs.advprog.mysawit.grpc.auth.ValidateUserRoleResponse;
import io.grpc.stub.StreamObserver;
import my_sawit.authentication_manajemen_akun.common.mapper.UserResponseMapper;
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
        UUID userId = UUID.fromString(request.getUserId());
        Optional<User> user = userRepository.findById(userId);

        UserResponse response = user
                .map(this::toGrpcUserResponse)
                .orElseGet(() -> UserResponse.newBuilder()
                        .setId(request.getUserId())
                        .setFound(false)
                        .build());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUsersByIds(
            GetUsersByIdsRequest request,
            StreamObserver<GetUsersByIdsResponse> responseObserver
    ) {
        List<UUID> userIds = request.getUserIdsList().stream()
                .map(UUID::fromString)
                .toList();

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
        Optional<User> user = userRepository.findById(UUID.fromString(request.getUserId()));
        String actualRole = user
                .map(this::getRoleName)
                .orElse("");
        boolean valid = user.isPresent() && actualRole.equalsIgnoreCase(request.getExpectedRole());

        ValidateUserRoleResponse response = ValidateUserRoleResponse.newBuilder()
                .setValid(valid)
                .setUserId(request.getUserId())
                .setActualRole(actualRole)
                .setExpectedRole(request.getExpectedRole())
                .setMessage(valid ? "User role is valid" : "User role is invalid")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private UserResponse toGrpcUserResponse(User user) {
        my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO dto =
                userResponseMapper.toDto(user);

        UserResponse.Builder builder = UserResponse.newBuilder()
                .setId(toStringOrEmpty(dto.getId()))
                .setUsername(toStringOrEmpty(dto.getUsername()))
                .setFullname(toStringOrEmpty(dto.getFullname()))
                .setEmail(toStringOrEmpty(dto.getEmail()))
                .setRole(toStringOrEmpty(dto.getRole()))
                .setNomorSertifikasi(toStringOrEmpty(dto.getNomorSertifikasi()))
                .setNamaMandor(toStringOrEmpty(dto.getNamaMandor()))
                .setFound(true);

        return builder.build();
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

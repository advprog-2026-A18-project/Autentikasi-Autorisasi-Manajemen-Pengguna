package my_sawit.authentication_manajemen_akun.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private int statusCode;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return of(ApiResponseStatus.SUCCESS, message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return of(ApiResponseStatus.CREATED, message, data);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return of(ApiResponseStatus.BAD_REQUEST, message, null);
    }

    public static <T> ApiResponse<T> badRequest(String message, T data) {
        return of(ApiResponseStatus.BAD_REQUEST, message, data);
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return of(ApiResponseStatus.UNAUTHORIZED, message, null);
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return of(ApiResponseStatus.FORBIDDEN, message, null);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return of(ApiResponseStatus.NOT_FOUND, message, null);
    }

    public static <T> ApiResponse<T> internalServerError(String message) {
        return of(ApiResponseStatus.INTERNAL_SERVER_ERROR, message, null);
    }

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return new ApiResponse<>(
                status.value(),
                message,
                data
        );
    }

    public static <T> ApiResponse<T> of(ApiResponseStatus status, String message, T data) {
        return new ApiResponse<>(
                status.getCode(),
                message != null ? message : status.getDefaultMessage(),
                data
        );
    }
}

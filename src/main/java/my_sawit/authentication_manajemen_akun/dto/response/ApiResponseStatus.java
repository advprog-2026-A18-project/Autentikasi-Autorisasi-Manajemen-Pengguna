package my_sawit.authentication_manajemen_akun.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiResponseStatus {
    SUCCESS(HttpStatus.OK, "Success"),
    CREATED(HttpStatus.CREATED, "Created"),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not found"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ApiResponseStatus(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return httpStatus.value();
    }

}

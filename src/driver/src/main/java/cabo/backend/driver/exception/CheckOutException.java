package cabo.backend.driver.exception;

import org.springframework.http.HttpStatus;

public class CheckOutException extends RuntimeException{
    private HttpStatus status;

    private String message;

    public CheckOutException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

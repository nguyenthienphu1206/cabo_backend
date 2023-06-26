package cabo.backend.customer.exception;

import cabo.backend.customer.dto.ErrorDetails;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<ErrorDetails> handleFirebaseAuthException(FirebaseAuthException exception,
                                                              WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(new Date(),
                                                    exception.getMessage(),
                                                    webRequest.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ErrorDetails> handleInterruptedException(InterruptedException exception,
                                                                   WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(new Date(),
                                                    exception.getMessage(),
                                                    webRequest.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Global Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception,
                                                              WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(), webRequest.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

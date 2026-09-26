package application.notification;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Обрабатывает один сценарий через {@code NotificationExceptionHandler}.
 */
@RestControllerAdvice
public class NotificationExceptionHandler {
    @ExceptionHandler(InvalidNotificationException.class)
    public ProblemDetail invalidNotification(InvalidNotificationException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid notification");
        problem.setDetail(exception.getMessage());
        return problem;
    }
}

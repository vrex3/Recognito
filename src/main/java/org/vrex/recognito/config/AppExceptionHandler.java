package org.vrex.recognito.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.Message;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@SuppressWarnings("unused")
public class AppExceptionHandler {

    private static final String GENERIC_ERROR_MESSAGE = "Recognito - Exception - ";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Message> handleGenericException(Exception exception) {
        if (exception.getMessage() != null)
            log.error(GENERIC_ERROR_MESSAGE, exception);
        else
            log.error(GENERIC_ERROR_MESSAGE +
                    Arrays.stream(
                                    exception.getStackTrace()).
                            map(stackTrace -> stackTrace.toString()).
                            collect(Collectors.joining("\n")), exception);

        return new ResponseEntity<>(Message.builder().text(GENERIC_ERROR_MESSAGE).build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Message> handleCustomException(ApplicationException exception) {
        log.error("{} {} : {}", GENERIC_ERROR_MESSAGE, exception.getErrorMessage(), exception.getStatus() != null ? exception.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(
                Message.builder().text(exception.getErrorMessage() != null ? exception.getErrorMessage() : GENERIC_ERROR_MESSAGE).build(),
                exception.getStatus() != null ? exception.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //BindException
}

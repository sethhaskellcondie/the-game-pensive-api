package com.sethhaskellcondie.thegamepensiveapi.exceptions;

import com.sethhaskellcondie.thegamepensiveapi.api.FormattedResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(value = {ExceptionFailedDbValidation.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleExceptionFailedDbValidation(ExceptionFailedDbValidation e) {
        FormattedResponseBody<String> body = new FormattedResponseBody<>(e.getMessage());
        return body.formatError();
    }

    @ExceptionHandler(value = {ExceptionInputValidation.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleExceptionInputValidation(ExceptionInputValidation e) {
        FormattedResponseBody<String> body = new FormattedResponseBody<>(e.getMessage());
        return body.formatError();
    }

    @ExceptionHandler(value = {ExceptionResourceNotFound.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, String> handleExceptionResourceNotFound(ExceptionResourceNotFound e) {
        FormattedResponseBody<String> body = new FormattedResponseBody<>(e.getMessage());
        return body.formatError();
    }

    @ExceptionHandler(value = {ExceptionMalformedEntity.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, List<String>> handleExceptionMalformedEntity(ExceptionMalformedEntity e) {
        FormattedResponseBody<List<String>> body = new FormattedResponseBody<>(e.getMessages());
        return body.formatError();
    }
}

/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.exception.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.workflow.exception.IntegrationException;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  final JaxRsDpsLog log;

  @ExceptionHandler({JsonMappingException.class, JsonParseException.class, IllegalStateException.class,
      MismatchedInputException.class, IntegrationException.class })
  protected ResponseEntity<Object> handleInvalidBody(RuntimeException ex,
      WebRequest request) {
    log.error("Exception during REST request: " + request.getDescription(false), ex);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ApiError apiError = ApiError.builder()
        .status(HttpStatus.BAD_REQUEST)
        .message(ExceptionUtils.getRootCauseMessage(ex))
        .build();
    return handleExceptionInternal(ex, apiError, headers,
        HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({ ConstraintViolationException.class })
  protected ResponseEntity<Object> handle(ConstraintViolationException ex, WebRequest request) {
    List<String> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
    }
    log.error("Constraint exception: {}" + errors);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ApiError apiError = ApiError.builder()
        .status(HttpStatus.BAD_REQUEST)
        .message(ExceptionUtils.getRootCauseMessage(ex))
        .errors(errors)
        .build();
    return handleExceptionInternal(ex, apiError, headers, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(AppException.class)
  protected ResponseEntity<Object> handleAppException(AppException e) {
    return this.getErrorResponse(e);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    ApiError apiError = ApiError.builder()
        .status((HttpStatus) status)
        .message(ex.getLocalizedMessage())
        .build();
    return handleExceptionInternal(ex, apiError, headers, status, request);
  }

  @ExceptionHandler({ResourceConflictException.class})
  protected ResponseEntity<Object> handle(ResourceConflictException r, WebRequest request) {
    log.error("Exception during REST request: " + request.getDescription(false), r);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ConflictApiError conflictApiError = ConflictApiError.ConflictErrorBuilder()
        .conflictId(r.getConflictingResourceId())
        .message(ExceptionUtils.getRootCauseMessage(r))
        .build();
    return handleExceptionInternal(r, conflictApiError, headers, HttpStatus.CONFLICT, request);
  }

  private ResponseEntity<Object> getErrorResponse(AppException e) {

    String exceptionMsg = e.getOriginalException() != null
        ? e.getOriginalException().getMessage()
        : e.getError().getMessage();

    if (e.getError().getCode() > 499) {
      this.log.error(exceptionMsg, e);
    } else {
      this.log.warning(exceptionMsg, e);
    }

    // Support for non standard HttpStatus Codes
    HttpStatus httpStatus = HttpStatus.resolve(e.getError().getCode());
    if (httpStatus == null) {
      return ResponseEntity.status(e.getError().getCode()).body(e);
    } else {
      return new ResponseEntity<>(e.getError(), httpStatus);
    }
  }

}

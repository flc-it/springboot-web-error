/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flcit.springboot.web.error;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.flcit.commons.core.exception.BasicRuntimeException;
import org.flcit.commons.core.util.ArrayUtils;
import org.flcit.commons.core.util.ClassUtils;
import org.flcit.commons.core.util.StringUtils;
import org.flcit.springboot.web.error.domain.ApiErrorBase;
import org.flcit.springboot.web.error.domain.ApiErrorTrace;
import org.flcit.springboot.web.error.domain.ApiErrors;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@ControllerAdvice
public class ErrorControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorControllerAdvice.class);
    private static final String CODE_VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String CODE_MESSAGE_READ_FAILED = "MESSAGE_READ_FAILED";
    private static final String CODE_EXTERNAL_REST_CALL_FAILED = "EXTERNAL_REST_CALL_FAILED";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        log(ex);
        return handleExceptionInternal(ex, buildApiError(webRequest, status.value(), CODE_VALIDATION_FAILED, null, ex.getBindingResult().getAllErrors()), HttpHeaders.EMPTY, status, webRequest);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        log(ex);
        return handleGlobalException(ex, webRequest, CODE_MESSAGE_READ_FAILED, status);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex,
            HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        logAsyncTaskError(ex, webRequest);
        if (webRequest instanceof ServletWebRequest) {
            final HttpServletResponse response = ((ServletWebRequest) webRequest).getResponse();
            if (response != null && response.isCommitted()) {
                return null;
            }
        }
        return handleGlobalException(ex, webRequest, status);
    }

    /**
     * @param ex
     * @param webRequest
     * @return
     */
    @ExceptionHandler(BasicRuntimeException.class)
    public ResponseEntity<Object> handleBasicRuntimeException(BasicRuntimeException ex, WebRequest webRequest) {
        log(ex);
        return handleGlobalException(ex, webRequest, ex.getCode(), getStatus(ex));
    }

    /**
     * @param ex
     * @param webRequest
     * @return
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Object> handleRestClientResponseException(RestClientResponseException ex, WebRequest webRequest){
        log(ex);
        return handleGlobalException(ex, webRequest,
                CODE_EXTERNAL_REST_CALL_FAILED,
                ex.getMessage() + " | " + StringUtils.limitLength(ex.getResponseBodyAsString(), 10000),
                HttpStatus.INTERNAL_SERVER_ERROR, true, 15);
    }

    /**
     * @param ex
     * @param webRequest
     * @return
     */
    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<Object> handleTaskRejectedException(TaskRejectedException ex, WebRequest webRequest) {
        logAsyncTaskError(ex, webRequest);
        return handleGlobalException(ex, webRequest, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * @param ex
     * @param webRequest
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest webRequest) {
        log(ex);
        return handleGlobalException(ex, webRequest, getDefaultStatus(ex), isDefaultStackTraces(ex));
    }

    private ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest webRequest, HttpStatus status) {
        return handleGlobalException(ex, webRequest, status, false);
    }

    private ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest webRequest, HttpStatus status, boolean stackTraces) {
        return handleGlobalException(ex, webRequest, ex.getClass().getName(), status, stackTraces);
    }

    private ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest webRequest, String code, HttpStatus status) {
        return handleGlobalException(ex, webRequest, code, status, false);
    }

    private ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest webRequest, String code, HttpStatus status, boolean stackTraces) {
        return handleGlobalException(ex, webRequest, code, getMessage(ex, status), status, stackTraces, null);
    }

    private ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest webRequest, String code, String message, HttpStatus status, boolean stackTraces, Integer maxStackTraceElement) {
        return handleExceptionInternal(ex, status == HttpStatus.NO_CONTENT ? null : buildApiError(webRequest, status.value(), code, message, getStackTraceElement(stackTraces, ex), maxStackTraceElement), HttpHeaders.EMPTY, status, webRequest);
    }

    private static final StackTraceElement[] getStackTraceElement(boolean stackTraces, Exception ex) {
        return stackTraces ? ex.getStackTrace() : null;
    }

    private static final void logAsyncTaskError(Exception ex, WebRequest webRequest) {
        log(ex, webRequest, true, false);
    }

    private static final void log(Exception ex) {
        log(ex, null, false);
    }

    private static final void log(Exception ex, WebRequest webRequest, boolean withPath) {
        log(ex, webRequest, withPath, true);
    }

    private static final void log(Exception ex, WebRequest webRequest, boolean withPath, boolean withException) {
        if (LOG.isWarnEnabled()) {
            if (withException) {
                LOG.warn(getLogMessage(ex, webRequest, withPath), ex);
            } else {
                LOG.warn(getLogMessage(ex, webRequest, withPath));
            }
        }
    }

    private static final String getLogMessage(Exception ex, WebRequest webRequest, boolean withPath) {
        return withPath ? ex.getClass().getName() + " - " + getPath(webRequest) : ex.getClass().getName();
    }

    private static final String getMessage(Exception ex, HttpStatus status) {
        final String message = ex.getMessage();
        return org.springframework.util.StringUtils.hasLength(message) ? message : status.getReasonPhrase();
    }

    private static final HttpStatus getStatus(BasicRuntimeException ex) {
        final ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        return responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static final ApiErrorBase buildApiError(WebRequest request, int status, String code, String message) {
        return new ApiErrorBase(getPath(request), status, code, message);
    }

    private static final ApiErrorBase buildApiError(WebRequest request, int status, String code, String message, List<ObjectError> errors) {
        if (!CollectionUtils.isEmpty(errors)) {
            return new ApiErrors(getPath(request), status, code, message, errors);
        }
        return buildApiError(request, status, code, message);
    }

    private static final ApiErrorBase buildApiError(WebRequest request, int status, String code, String message, StackTraceElement[] stackTraces, Integer maxStackTraceElement) {
        if (!ObjectUtils.isEmpty(stackTraces)) {
            return new ApiErrorTrace(getPath(request), status, code, message, ArrayUtils.limit(stackTraces, maxStackTraceElement));
        }
        return buildApiError(request, status, code, message);
    }

    private static final String getPath(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(request.getRequestURI());
        final String query = request.getQueryString();
        if (org.springframework.util.StringUtils.hasLength(query)) {
            sb.append('?');
            sb.append(query);
        }
        return sb.toString();
    }

    private static final String getPath(WebRequest request) {
        if (!(request instanceof ServletWebRequest)) {
            return null;
        }
        return getPath(((ServletWebRequest) request).getRequest());
    }

    private static final boolean isDefaultStackTraces(Exception e) {
        return !isAccessDeniedException(e);
    }

    private static final HttpStatus getDefaultStatus(Exception e) {
        if (isAccessDeniedException(e)) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static final boolean isAccessDeniedException(Exception e) {
        return ClassUtils.isClass(e, "org.springframework.security.access.AccessDeniedException");
    }

}

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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import org.flcit.springboot.commons.core.exception.NotFoundException;
import org.flcit.springboot.commons.test.validation.SimpleBindingResult;

@RestController
@RequestMapping
class TestResource {

    static final String TEST_PATH = "/test";
    static final String FUNCTIONAL_EXCEPTION_PATH = "/functional-exception";
    static final String TECHNICAL_EXCEPTION_PATH = "/technical-exception";
    static final String ACCESS_DENIED_EXCEPTION_PATH = "/access-denied-exception";
    static final String METHOD_ARGUMENT_NOT_VALID_EXCEPTION_PATH = "/method-argument-not-valid-exception";
    static final String HTTP_MESSAGE_NOT_READABLE_EXCEPTION_PATH = "/http-message-not-readable-exception";
    static final String ASYNC_REQUEST_TIMEOUT_EXCEPTION_PATH = "/async-request-timeout-exception";
    static final String TASK_REJECTED_EXCEPTION_PATH = "/task-rejected-exception";
    static final String REST_CLIENT_RESPONSE_EXCEPTION_PATH = "/rest-client-response-exception";

    static final Map<String, String> TEST_RESPONSE_VALUE = Collections.singletonMap("key", "value");

    static final String FUNCTIONAL_EXCEPTION_CODE = "FUNCTIONAL_CODE";
    static final String FUNCTIONAL_EXCEPTION_MESSAGE = "FUNCTIONAL_MESSAGE";

    static final String TECHNICAL_EXCEPTION_CODE = getCode(RuntimeException.class);
    static final String TECHNICAL_EXCEPTION_MESSAGE = "TECHNICAL_MESSAGE";

    static final String ACCESS_DENIED_EXCEPTION_CODE = getCode(AccessDeniedException.class);
    static final String ACCESS_DENIED_EXCEPTION_MESSAGE = "ACCESS_DENIED_MESSAGE";

    static final String HTTP_MESSAGE_NOT_READABLE_EXCEPTION_MESSAGE = "HTTP_MESSAGE_NOT_READABLE_MESSAGE";

    static final String ASYNC_REQUEST_TIMEOUT_EXCEPTION_CODE = getCode(AsyncRequestTimeoutException.class);
    static final String ASYNC_REQUEST_TIMEOUT_EXCEPTION_MESSAGE = HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase();

    static final String TASK_REJECTED_EXCEPTION_CODE = getCode(TaskRejectedException.class);
    static final String TASK_REJECTED_EXCEPTION_MESSAGE = "TASK_REJECTED_MESSAGE";

    static final String REST_CLIENT_RESPONSE_EXCEPTION_MESSAGE = "REST_CLIENT_RESPONSE_MESSAGE";
    static final String REST_CLIENT_RESPONSE_EXCEPTION_RESPONSE_BODY = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();

    @GetMapping(TEST_PATH)
    public Map<String, String> test() {
        return TEST_RESPONSE_VALUE;
    }

    @GetMapping(FUNCTIONAL_EXCEPTION_PATH)
    public void functionalException() {
        throw new NotFoundException(FUNCTIONAL_EXCEPTION_CODE, FUNCTIONAL_EXCEPTION_MESSAGE);
    }

    @GetMapping(TECHNICAL_EXCEPTION_PATH)
    public void technicalException() {
        throw new RuntimeException(TECHNICAL_EXCEPTION_MESSAGE);
    }

    @GetMapping(ACCESS_DENIED_EXCEPTION_PATH)
    public void accessDeniedException() {
        throw new AccessDeniedException(ACCESS_DENIED_EXCEPTION_MESSAGE);
    }

    @GetMapping(METHOD_ARGUMENT_NOT_VALID_EXCEPTION_PATH)
    public void methodArgumentNotValidException() throws MethodArgumentNotValidException, SecurityException {
        throw new MethodArgumentNotValidException(new MethodParameter(Object.class.getMethods()[1], 0), new SimpleBindingResult(Constants.ERRORS));
    }

    @GetMapping(HTTP_MESSAGE_NOT_READABLE_EXCEPTION_PATH)
    public void httpMessageNotReadableException() {
        throw new HttpMessageNotReadableException(HTTP_MESSAGE_NOT_READABLE_EXCEPTION_MESSAGE, new MockHttpInputMessage(new byte[] { }));
    }

    @GetMapping(ASYNC_REQUEST_TIMEOUT_EXCEPTION_PATH)
    public void asyncRequestTimeoutException() {
        throw new AsyncRequestTimeoutException();
    }

    @GetMapping(TASK_REJECTED_EXCEPTION_PATH)
    public void taskRejectedException() {
        throw new TaskRejectedException(TASK_REJECTED_EXCEPTION_MESSAGE);
    }

    @GetMapping(REST_CLIENT_RESPONSE_EXCEPTION_PATH)
    public void restClientResponseException() {
        throw new RestClientResponseException(REST_CLIENT_RESPONSE_EXCEPTION_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null, REST_CLIENT_RESPONSE_EXCEPTION_RESPONSE_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private static final <T extends Exception> String getCode(Class<T> exceptionType) {
        return exceptionType.getName();
    }

}

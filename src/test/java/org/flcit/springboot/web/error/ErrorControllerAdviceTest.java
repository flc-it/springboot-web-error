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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import org.flcit.commons.core.exception.BasicRuntimeException;
import org.flcit.springboot.commons.core.exception.NoContentException;
import org.flcit.springboot.commons.test.MockitoBaseTest;
import org.flcit.springboot.commons.test.util.LogTestUtils;
import org.flcit.springboot.web.error.domain.ApiErrorBase;

class ErrorControllerAdviceTest implements MockitoBaseTest {

    private final ErrorControllerAdvice tested = new ErrorControllerAdvice();

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ServletWebRequest servletWebRequest;

    @Mock
    private Logger logger;

    @Test
    void handleMethodArgumentNotValidExceptionTest() throws Exception {
        final MethodArgumentNotValidException exception = new MethodArgumentNotValidException(new MethodParameter(Object.class.getMethods()[1], 0), bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());
        assertInstanceOf(ApiErrorBase.class, tested.handleException(exception, servletWebRequest).getBody());
    }

    @Test
    void handleAsyncRequestTimeoutExceptionTest() throws Exception {
        final AsyncRequestTimeoutException exception = new AsyncRequestTimeoutException();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCommitted(true);
        assertNull(tested.handleException(exception, new ServletWebRequest(new MockHttpServletRequest(), response)));
        assertInstanceOf(ApiErrorBase.class, tested.handleException(exception, new ServletWebRequest(new MockHttpServletRequest(), null)).getBody());
        assertInstanceOf(ApiErrorBase.class, tested.handleException(exception, mock(WebRequest.class)).getBody());
    }

    @Test
    void getPathTest() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");
        request.setQueryString("param=v1");
        assertEquals("/test?param=v1", ((ApiErrorBase) tested.handleException(new AsyncRequestTimeoutException(), new ServletWebRequest(request, new MockHttpServletResponse())).getBody()).getPath());
    }

    @Test
    void handleBasicRuntimeExceptionNoContentTest() {
        assertNull(tested.handleBasicRuntimeException(new NoContentException(HttpStatus.NO_CONTENT.getReasonPhrase()), new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse())).getBody());
    }

    @Test
    void handleBasicRuntimeExceptionNoStatusTest() {
        assertEquals(500, ((ApiErrorBase) tested.handleBasicRuntimeException(new NoStatusBasicRuntimeException(), new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse())).getBody()).getStatus());
    }

    @Test
    void logTest() throws Exception {
        when(logger.isWarnEnabled()).thenReturn(false);
        LogTestUtils.setLogger(tested, logger);
        tested.handleException(new AsyncRequestTimeoutException(), new ServletWebRequest(new MockHttpServletRequest(), null));
        verify(logger, never()).warn(anyString());
        verify(logger, never()).warn(anyString(), any(Throwable.class));
    }

    static final class NoStatusBasicRuntimeException extends BasicRuntimeException {

        private static final long serialVersionUID = 1L;

        protected NoStatusBasicRuntimeException() {
            super(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }

    }

}

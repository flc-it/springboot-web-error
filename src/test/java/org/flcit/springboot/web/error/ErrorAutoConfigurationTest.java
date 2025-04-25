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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.servlet.ResultActions;

import org.flcit.springboot.commons.test.MockitoBaseTest;
import org.flcit.springboot.commons.test.util.ContextRunnerUtils;
import org.flcit.springboot.commons.test.util.EnvironmentPostProcessorTestUtils;
import org.flcit.springboot.commons.test.util.MvcUtils;
import org.flcit.springboot.commons.test.util.ResultActionsUtils;
import org.flcit.springboot.web.error.domain.ApiErrorBase;
import org.flcit.springboot.web.error.domain.ApiErrors;

class ErrorAutoConfigurationTest implements MockitoBaseTest {

    @Mock
    private MockEnvironment mockEnvironment;

    @Mock
    private MutablePropertySources mutablePropertySources;

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    WebMvcAutoConfiguration.class,
                    ErrorAutoConfiguration.class));

    private static final ApiErrorBase API_ERROR_FUNCTIONAL_EXCEPTION = new ApiErrorBase(TestResource.FUNCTIONAL_EXCEPTION_PATH, 404, TestResource.FUNCTIONAL_EXCEPTION_CODE, TestResource.FUNCTIONAL_EXCEPTION_MESSAGE);
    private static final ApiErrorBase API_ERROR_TECHNICAL_EXCEPTION = new ApiErrorBase(TestResource.TECHNICAL_EXCEPTION_PATH, 500, TestResource.TECHNICAL_EXCEPTION_CODE, TestResource.TECHNICAL_EXCEPTION_MESSAGE);
    private static final ApiErrorBase API_ERROR_ACCESS_DENIED = new ApiErrorBase(TestResource.ACCESS_DENIED_EXCEPTION_PATH, 403, TestResource.ACCESS_DENIED_EXCEPTION_CODE, TestResource.ACCESS_DENIED_EXCEPTION_MESSAGE);
    private static final ApiErrors API_ERRORS_METHOD_ARGUMENT_NOT_VALID = new ApiErrors(TestResource.METHOD_ARGUMENT_NOT_VALID_EXCEPTION_PATH, 400, "VALIDATION_FAILED", null, Constants.ERRORS);
    private static final ApiErrorBase API_ERROR_HTTP_MESSAGE_NOT_READABLE_EXCEPTION = new ApiErrorBase(TestResource.HTTP_MESSAGE_NOT_READABLE_EXCEPTION_PATH, 400, "MESSAGE_READ_FAILED", TestResource.HTTP_MESSAGE_NOT_READABLE_EXCEPTION_MESSAGE);
    private static final ApiErrorBase API_ERROR_ASYNC_REQUEST_TIMEOUT_EXCEPTION = new ApiErrorBase(TestResource.ASYNC_REQUEST_TIMEOUT_EXCEPTION_PATH, 503, TestResource.ASYNC_REQUEST_TIMEOUT_EXCEPTION_CODE, TestResource.ASYNC_REQUEST_TIMEOUT_EXCEPTION_MESSAGE);
    private static final ApiErrorBase API_ERROR_TASK_REJECTED_EXCEPTION = new ApiErrorBase(TestResource.TASK_REJECTED_EXCEPTION_PATH, 503, TestResource.TASK_REJECTED_EXCEPTION_CODE, TestResource.TASK_REJECTED_EXCEPTION_MESSAGE);
    private static final ApiErrorBase API_ERROR_REST_CLIENT_RESPONSE_EXCEPTION = new ApiErrorBase(TestResource.REST_CLIENT_RESPONSE_EXCEPTION_PATH, 500, "EXTERNAL_REST_CALL_FAILED", TestResource.REST_CLIENT_RESPONSE_EXCEPTION_MESSAGE + " | " + TestResource.REST_CLIENT_RESPONSE_EXCEPTION_RESPONSE_BODY);

    @Test
    void environmentPostProcessorLoaded() throws IOException {
        EnvironmentPostProcessorTestUtils.checkPropertySource(new ErrorAutoConfiguration(), new ResourcePropertySource("classpath:web-error-lib.properties"));
        when(mockEnvironment.getPropertySources()).thenReturn(mutablePropertySources);
        doAnswer(invocation -> {
            throw new IOException();
          }).when(mutablePropertySources).addLast(any(ResourcePropertySource.class));
        final ErrorAutoConfiguration condifuration = new ErrorAutoConfiguration();
        final SpringApplication application = new SpringApplication();
        assertThrows(RuntimeException.class, () -> condifuration.postProcessEnvironment(mockEnvironment, application));
    }

    @Test
    void errorControllerAdviceBeanOk() {
        ContextRunnerUtils.assertHasSingleBean(this.contextRunner, ErrorControllerAdvice.class);
    }

    @Test
    void noErrorMvcAutoConfigurationBean() {
        ContextRunnerUtils.assertDoesNotHaveBean(this.contextRunner, ErrorMvcAutoConfiguration.class);
    }

    @Test
    void testEndpointsException() {
        this.contextRunner
        .withUserConfiguration(TestResource.class)
        .run(context -> {
            MvcUtils.assertGetJsonResponse(context, TestResource.TEST_PATH, TestResource.TEST_RESPONSE_VALUE);
            assertGetJsonResponse(context, TestResource.FUNCTIONAL_EXCEPTION_PATH, API_ERROR_FUNCTIONAL_EXCEPTION);
            ResultActionsUtils.assertArrayNotEmpty(
                    assertGetJsonResponse(context, TestResource.TECHNICAL_EXCEPTION_PATH, API_ERROR_TECHNICAL_EXCEPTION, false),
                    "trace");
            assertGetJsonResponse(context, TestResource.ACCESS_DENIED_EXCEPTION_PATH, API_ERROR_ACCESS_DENIED);
            assertGetJsonResponse(context, TestResource.METHOD_ARGUMENT_NOT_VALID_EXCEPTION_PATH, API_ERRORS_METHOD_ARGUMENT_NOT_VALID);
            assertGetJsonResponse(context, TestResource.HTTP_MESSAGE_NOT_READABLE_EXCEPTION_PATH, API_ERROR_HTTP_MESSAGE_NOT_READABLE_EXCEPTION);
            assertGetJsonResponse(context, TestResource.ASYNC_REQUEST_TIMEOUT_EXCEPTION_PATH, API_ERROR_ASYNC_REQUEST_TIMEOUT_EXCEPTION);
            assertGetJsonResponse(context, TestResource.TASK_REJECTED_EXCEPTION_PATH, API_ERROR_TASK_REJECTED_EXCEPTION);
            ResultActionsUtils.assertArrayHasSizeLessOrEqual(
                    assertGetJsonResponse(context, TestResource.REST_CLIENT_RESPONSE_EXCEPTION_PATH, API_ERROR_REST_CLIENT_RESPONSE_EXCEPTION, false),
                    "trace", 15);
        });
    }

    private static final ResultActions assertGetJsonResponse(AssertableWebApplicationContext context, String path, ApiErrorBase error) throws Exception {
        return assertGetJsonResponse(context, path, error, true);
    }

    private static final ResultActions assertGetJsonResponse(AssertableWebApplicationContext context, String path, ApiErrorBase error, boolean strict) throws Exception {
        return MvcUtils.assertGetJsonResponse(context, path, HttpStatus.valueOf(error.getStatus()), error, strict);
    }

}

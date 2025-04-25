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

package org.flcit.springboot.web.error.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ApiErrorTraceTest {

    private static final String PATH = "/api/pp";
    private static final int STATUS = 400;
    private static final String CODE = "CODE";
    private static final String MESSAGE = "MESSAGE";
    private static final StackTraceElement[] TRACE = new StackTraceElement[] {
            new StackTraceElement("declaringClass1", "methodName1", "fileName1", 501),
            new StackTraceElement("declaringClass2", "methodName2", "fileName2", 502),
            new StackTraceElement("declaringClass3", "methodName3", "fileName3", 503)
    };

    @Test
    void test() {
        final ApiErrorTrace apiErrorTrace = new ApiErrorTrace(PATH, STATUS, CODE, MESSAGE, TRACE);
        assertEquals(PATH, apiErrorTrace.getPath());
        assertEquals(STATUS, apiErrorTrace.getStatus());
        assertEquals(CODE, apiErrorTrace.getCode());
        assertEquals(MESSAGE, apiErrorTrace.getMessage());
        assertArrayEquals(TRACE, apiErrorTrace.getTrace());
    }

}

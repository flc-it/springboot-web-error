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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;

import org.flcit.springboot.web.error.Constants;

class ApiErrorsTest {

    private static final String PATH = "/api/pp";
    private static final int STATUS = 400;
    private static final String CODE = "CODE";
    private static final String MESSAGE = "MESSAGE";

    @Test
    void test() {
        ApiErrors apiErrors = new ApiErrors(PATH, STATUS, CODE, MESSAGE, Constants.ERRORS);
        assertEquals(PATH, apiErrors.getPath());
        assertEquals(STATUS, apiErrors.getStatus());
        assertEquals(CODE, apiErrors.getCode());
        assertEquals(MESSAGE, apiErrors.getMessage());
        assertNotNull(apiErrors.getErrors());
        assertEquals(Constants.ERRORS.size(), apiErrors.getErrors().size());
        for (int i = 0; i < apiErrors.getErrors().size(); i++) {
            assertEquals(Constants.ERRORS.get(i).getObjectName(), apiErrors.getErrors().get(i).getObjectName());
            assertEquals(Constants.ERRORS.get(i).getCode(), apiErrors.getErrors().get(i).getCode());
            assertEquals(Constants.ERRORS.get(i).getDefaultMessage(), apiErrors.getErrors().get(i).getDefaultMessage());
            if (Constants.ERRORS.get(i) instanceof FieldError) {
                assertEquals(((FieldError) Constants.ERRORS.get(i)).getField(), apiErrors.getErrors().get(i).getField());
            } else {
                assertNull(apiErrors.getErrors().get(i).getField());
            }
        }
        apiErrors = new ApiErrors(PATH, STATUS, CODE, MESSAGE, null);
        assertNull(apiErrors.getErrors());
    }

}

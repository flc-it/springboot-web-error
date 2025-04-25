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

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public class ApiErrors extends ApiErrorBase {

    private final List<Error> errors;

    /**
     * @param path
     * @param status
     * @param code
     * @param message
     * @param errors
     */
    public ApiErrors(String path, int status, String code, String message, List<ObjectError> errors) {
        super(path, status, code, message);
        if (!CollectionUtils.isEmpty(errors)) {
            this.errors = new ArrayList<>(errors.size());
            for (ObjectError error: errors) {
                Error e = null;
                if (error instanceof FieldError) {
                    e = new Error(error.getCode(), error.getObjectName(), ((FieldError)error).getField(), error.getDefaultMessage());
                } else {
                    e = new Error(error.getCode(), error.getObjectName(), error.getDefaultMessage());
                }
                this.errors.add(e);
            }
        } else {
            this.errors = null;
        }
    }

    /**
     * @return
     */
    public List<Error> getErrors() {
        return errors;
    }

    static class Error {

        private final String code;
        private final String objectName;
        private final String field;
        private final String defaultMessage;

        Error(String code, String objectName, String defaultMessage) {
            this.code = code;
            this.objectName = objectName;
            this.field = null;
            this.defaultMessage = defaultMessage;
        }

        Error(String code, String objectName, String field, String defaultMessage) {
            this.code = code;
            this.objectName = objectName;
            this.field = field;
            this.defaultMessage = defaultMessage;
        }

        public String getCode() {
            return code;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getField() {
            return field;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

}

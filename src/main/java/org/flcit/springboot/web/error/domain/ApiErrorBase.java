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

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public class ApiErrorBase {

    private final String path;
    private final int status;
    private final String code;
    private final String message;

    /**
     * @param path
     * @param status
     * @param code
     * @param message
     */
    public ApiErrorBase(String path, int status, String code, String message) {
        this.path = path;
        this.status = status;
        this.code = code;
        this.message = message;
    }

    /**
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * @return
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

}

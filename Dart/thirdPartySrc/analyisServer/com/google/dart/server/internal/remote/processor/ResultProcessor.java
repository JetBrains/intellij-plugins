/*
 * Copyright (c) 2014, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.ExtendedRequestErrorCode;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dartlang.analysis.server.protocol.RequestError;

/**
 * Abstract processor class for processing responses sent to consumers.
 * 
 * @coverage dart.server.remote
 */
public class ResultProcessor extends JsonProcessor {

  RequestError generateRequestError(Exception exception) {
    String message = exception.getMessage();
    String stackTrace = null;
    if (exception.getStackTrace() != null) {
      stackTrace = ExceptionUtils.getStackTrace(exception);
    }
    return new RequestError(ExtendedRequestErrorCode.INVALID_SERVER_RESPONSE, message != null
        ? message : "", stackTrace);
  }

}

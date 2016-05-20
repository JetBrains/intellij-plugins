/*
 * Copyright (c) 2015, the Dart project authors.
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
package com.google.dart.server.internal.remote;

import com.google.dart.server.internal.remote.utilities.RequestUtilities;
import com.google.dart.server.internal.remote.utilities.ResponseUtilities;
import com.google.dart.server.utilities.logging.Logging;
import com.google.gson.JsonObject;

/**
 * A {@link RequestSink} that reports with an error to each request.
 * 
 * @coverage dart.server.remote
 */
public class ErrorRequestSink implements RequestSink {
  /**
   * The {@link ResponseSink} to send error responses to.
   */
  private final ResponseSink responseSink;

  private final String code;
  private final String message;

  public ErrorRequestSink(ResponseSink responseSink, String code, String message) {
    if (responseSink == null || code == null || message == null) {
      throw new IllegalArgumentException("Unexpected null argument: " + responseSink + " " + code
          + " " + message);
    }
    this.responseSink = responseSink;
    this.code = code;
    this.message = message;
  }

  @Override
  public void add(JsonObject request) {
    String id = RequestUtilities.getId(request);
    try {
      JsonObject response = ResponseUtilities.createErrorResponse(id, code, message);
      responseSink.add(response);
    } catch (Throwable e) {
      Logging.getLogger().logError(e.getMessage(), e);
    }
  }

  @Override
  public void close() {
  }
}

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
package com.google.dart.server.internal.remote.utilities;

import com.google.gson.JsonObject;

/**
 * A utilities class for generating the analysis server json responses.
 * 
 * @coverage dart.server.remote
 */
public class ResponseUtilities {
  private static final String CODE = "code";
  private static final String ERROR = "error";
  private static final String ID = "id";
  private static final String MESSAGE = "message";

  public static final String INCOMPATIBLE_SERVER_VERSION = "INCOMPATIBLE_SERVER_VERSION";

  /**
   * Return a new error response with the given id, code and message.
   */
  public static JsonObject createErrorResponse(String id, String code, String message) {
    JsonObject error = new JsonObject();
    error.addProperty(CODE, code);
    error.addProperty(MESSAGE, message);
    JsonObject response = new JsonObject();
    response.addProperty(ID, id);
    response.add(ERROR, error);
    return response;
  }

  /**
   * Set "id" property for the given response.
   */
  public static void setId(JsonObject response, String id) {
    response.addProperty(ID, id);
  }

  private ResponseUtilities() {
  }
}

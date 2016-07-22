/*
 * Copyright (c) 2012, the Dart project authors.
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

package com.jetbrains.lang.dart.ide.runner.server.google;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A VM command result.
 */
public class VmResult<T> {

  static <T> VmResult<T> createErrorResult(String errorMessage) {
    VmResult<T> result = new VmResult<>();
    result.setError(errorMessage);
    return result;
  }

  static <T> VmResult<T> createFrom(JSONObject params) throws JSONException {
    VmResult<T> result = new VmResult<>();

    if (params.has("error")) {
      result.setError(params.getString("error"));
    }

    return result;
  }

  static <T> VmResult<T> createFrom(T object) {
    VmResult<T> result = new VmResult<>();

    result.setResult(object);

    return result;
  }

  static JSONObject createJsonErrorResult(String message) throws JSONException {
    JSONObject obj = new JSONObject();

    obj.put("error", message);

    return obj;
  }

  private String error;

  private T result;

  VmResult() {

  }

  public String getError() {
    return error;
  }

  public T getResult() {
    return result;
  }

  public boolean isError() {
    return error != null;
  }

  @Override
  public String toString() {
    if (error != null) {
      return error;
    }
    else if (result != null) {
      return result.toString();
    }
    else {
      return super.toString();
    }
  }

  void setError(String error) {
    this.error = error;
  }

  void setResult(T result) {
    this.result = result;
  }
}

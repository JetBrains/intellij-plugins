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
package org.dartlang.vm.service.element;

// This is a generated file.

import com.google.gson.JsonObject;

/**
 * Every non-error response returned by the Service Protocol extends {@link Response}. By using the
 * {@link type} property, the client can determine which type of response has been provided.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Response extends Element {

  public Response(JsonObject json) {
    super(json);
  }

  /**
   * Every response returned by the VM Service has the type property. This allows the client
   * distinguish between different kinds of responses.
   */
  public String getType() {
    return json.get("type").getAsString();
  }
}

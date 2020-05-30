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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A {@link Sentinel} is used to indicate that the normal response is not available.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Sentinel extends Response {

  public Sentinel(JsonObject json) {
    super(json);
  }

  /**
   * What kind of sentinel is this?
   */
  public SentinelKind getKind() {
    final JsonElement value = json.get("kind");
    try {
      return value == null ? SentinelKind.Unknown : SentinelKind.valueOf(value.getAsString());
    } catch (IllegalArgumentException e) {
      return SentinelKind.Unknown;
    }
  }

  /**
   * A reasonable string representation of this sentinel.
   */
  public String getValueAsString() {
    return getAsString("valueAsString");
  }
}

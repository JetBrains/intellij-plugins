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

@SuppressWarnings({"WeakerAccess", "unused"})
public class ContextElement extends Element {

  public ContextElement(JsonObject json) {
    super(json);
  }

  /**
   * @return one of <code>InstanceRef</code> or <code>Sentinel</code>
   */
  public InstanceRef getValue() {
    final JsonElement elem = json.get("value");
    if (!elem.isJsonObject()) return null;
    final JsonObject child = elem.getAsJsonObject();
    final String type = child.get("type").getAsString();
    if ("Sentinel".equals(type)) return null;
    return new InstanceRef(child);
  }
}

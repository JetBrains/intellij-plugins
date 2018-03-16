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

@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class HeapSpace extends Response {

  public HeapSpace(JsonObject json) {
    super(json);
  }

  public double getAvgCollectionPeriodMillis() {
    return json.get("avgCollectionPeriodMillis") == null ? 0.0 : json.get("avgCollectionPeriodMillis").getAsDouble();
  }

  public int getCapacity() {
    return json.get("capacity") == null ? -1 : json.get("capacity").getAsInt();
  }

  public int getCollections() {
    return json.get("collections") == null ? -1 : json.get("collections").getAsInt();
  }

  public int getExternal() {
    return json.get("external") == null ? -1 : json.get("external").getAsInt();
  }

  public String getName() {
    return json.get("name").getAsString();
  }

  public double getTime() {
    return json.get("time") == null ? 0.0 : json.get("time").getAsDouble();
  }

  public int getUsed() {
    return json.get("used") == null ? -1 : json.get("used").getAsInt();
  }
}

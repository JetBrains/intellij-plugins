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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class AllocationProfile extends Response {

  public AllocationProfile(JsonObject json) {
    super(json);
  }

  public String getDateLastServiceGC() {
    return json.get("dateLastServiceGC").getAsString();
  }

  public ElementList<ClassHeapStats> getMembers() {
    return new ElementList<ClassHeapStats>(json.get("members").getAsJsonArray()) {
      @Override
      protected ClassHeapStats basicGet(JsonArray array, int index) {
        return new ClassHeapStats(array.get(index).getAsJsonObject());
      }
    };
  }
}

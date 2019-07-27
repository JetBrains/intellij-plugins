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

/**
 * A {@link FlagList} represents the complete set of VM command line flags.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FlagList extends Response {

  public FlagList(JsonObject json) {
    super(json);
  }

  /**
   * A list of all flags in the VM.
   */
  public ElementList<Flag> getFlags() {
    return new ElementList<Flag>(json.get("flags").getAsJsonArray()) {
      @Override
      protected Flag basicGet(JsonArray array, int index) {
        return new Flag(array.get(index).getAsJsonObject());
      }
    };
  }
}

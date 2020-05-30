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
 * See getRetainingPath.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RetainingPath extends Response {

  public RetainingPath(JsonObject json) {
    super(json);
  }

  /**
   * The chain of objects which make up the retaining path.
   */
  public ElementList<RetainingObject> getElements() {
    return new ElementList<RetainingObject>(json.get("elements").getAsJsonArray()) {
      @Override
      protected RetainingObject basicGet(JsonArray array, int index) {
        return new RetainingObject(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The type of GC root which is holding a reference to the specified object. Possible values
   * include:  * class table  * local handle  * persistent handle  * stack  * user global  * weak
   * persistent handle  * unknown
   */
  public String getGcRootType() {
    return getAsString("gcRootType");
  }

  /**
   * The length of the retaining path.
   */
  public int getLength() {
    return getAsInt("length");
  }
}

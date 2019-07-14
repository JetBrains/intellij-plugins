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
 * A {@link TypeArguments} object represents the type argument vector for some instantiated generic
 * type.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class TypeArguments extends Obj {

  public TypeArguments(JsonObject json) {
    super(json);
  }

  /**
   * A name for this type argument list.
   */
  public String getName() {
    return json.get("name").getAsString();
  }

  /**
   * A list of types.
   *
   * The value will always be one of the kinds: Type, TypeRef, TypeParameter, BoundedType.
   */
  public ElementList<InstanceRef> getTypes() {
    return new ElementList<InstanceRef>(json.get("types").getAsJsonArray()) {
      @Override
      protected InstanceRef basicGet(JsonArray array, int index) {
        return new InstanceRef(array.get(index).getAsJsonObject());
      }
    };
  }
}

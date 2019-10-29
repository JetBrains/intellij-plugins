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
import org.jetbrains.annotations.Nullable;

/**
 * An {@link Obj} is a persistent object that is owned by some isolate.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Obj extends Response {

  public Obj(JsonObject json) {
    super(json);
  }

  /**
   * If an object is allocated in the Dart heap, it will have a corresponding class object.
   *
   * The class of a non-instance is not a Dart class, but is instead an internal vm object.
   *
   * Moving an Object into or out of the heap is considered a backwards compatible change for types
   * other than Instance.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public ClassRef getClassRef() {
    JsonObject obj = (JsonObject) json.get("class");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ClassRef(obj);
  }

  /**
   * Provided and set to true if the id of an Object is fixed. If true, the id of an Object is
   * guaranteed not to change or expire. The object may, however, still be _Collected_.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public boolean getFixedId() {
    return getAsBoolean("fixedId");
  }

  /**
   * A unique identifier for an Object. Passed to the getObject RPC to reload this Object.
   *
   * Some objects may get a new id when they are reloaded.
   */
  public String getId() {
    return getAsString("id");
  }

  /**
   * The size of this object in the heap.
   *
   * If an object is not heap-allocated, then this field is omitted.
   *
   * Note that the size can be zero for some objects. In the current VM implementation, this occurs
   * for small integers, which are stored entirely within their object pointers.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public int getSize() {
    return getAsInt("size");
  }
}

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
 * An {@link Obj} is a persistent object that is owned by some isolate.
 */
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
   */
  public ClassRef getClassRef() {
    return json.get("class") == null ? null : new ClassRef((JsonObject) json.get("class"));
  }

  /**
   * A unique identifier for an Object. Passed to the getObject RPC to reload this Object.
   *
   * Some objects may get a new id when they are reloaded.
   */
  public String getId() {
    return json.get("id").getAsString();
  }

  /**
   * The size of this object in the heap.
   *
   * If an object is not heap-allocated, then this field is omitted.
   *
   * Note that the size can be zero for some objects. In the current VM implementation, this occurs
   * for small integers, which are stored entirely within their object pointers.
   */
  public int getSize() {
    return json.get("size") == null ? -1 : json.get("size").getAsInt();
  }
}

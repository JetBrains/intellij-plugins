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
 * {@link ObjRef} is a reference to a {@link Obj}.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ObjRef extends Response {

  public ObjRef(JsonObject json) {
    super(json);
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
   * A unique identifier for an Object. Passed to the getObject RPC to load this Object.
   */
  public String getId() {
    return getAsString("id");
  }
}

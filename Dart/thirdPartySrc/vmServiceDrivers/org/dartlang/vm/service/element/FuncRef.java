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
 * An {@link FuncRef} is a reference to a {@link Func}.
 */
public class FuncRef extends ObjRef {

  public FuncRef(JsonObject json) {
    super(json);
  }

  /**
   * The name of this function.
   */
  public String getName() {
    return json.get("name").getAsString();
  }

  /**
   * The owner of this function, which can be a Library, Class, or a Function.
   *
   * @return one of <code>LibraryRef</code>, <code>ClassRef</code> or <code>FuncRef</code>
   */
  public Object getOwner() {
    JsonObject elem = (JsonObject)json.get("owner");
    if (elem == null) return null;

    if (elem.get("type").getAsString().equals("@Library")) return new LibraryRef(elem);
    if (elem.get("type").getAsString().equals("@Class")) return new ClassRef(elem);
    if (elem.get("type").getAsString().equals("@Func")) return new FuncRef(elem);
    return null;
  }

  /**
   * Is this function const?
   */
  public boolean isConst() {
    return json.get("const").getAsBoolean();
  }

  /**
   * Is this function static?
   */
  public boolean isStatic() {
    return json.get("static").getAsBoolean();
  }
}

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
 * {@link CodeRef} is a reference to a {@link Code} object.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class CodeRef extends ObjRef {

  public CodeRef(JsonObject json) {
    super(json);
  }

  /**
   * What kind of code object is this?
   */
  public CodeKind getKind() {
    String name = json.get("kind").getAsString();
    try {
      return CodeKind.valueOf(name);
    } catch (IllegalArgumentException e) {
      return CodeKind.Unknown;
    }
  }

  /**
   * A name for this code object.
   */
  public String getName() {
    return json.get("name").getAsString();
  }
}

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
 * A {@link BoundVariable} represents a local variable bound to a particular value in a {@link
 * Frame}.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BoundVariable extends Response {

  public BoundVariable(JsonObject json) {
    super(json);
  }

  /**
   * The token position where this variable was declared.
   */
  public int getDeclarationTokenPos() {
    return getAsInt("declarationTokenPos");
  }

  public String getName() {
    return getAsString("name");
  }

  /**
   * The last token position where this variable is visible to the scope.
   */
  public int getScopeEndTokenPos() {
    return getAsInt("scopeEndTokenPos");
  }

  /**
   * The first token position where this variable is visible to the scope.
   */
  public int getScopeStartTokenPos() {
    return getAsInt("scopeStartTokenPos");
  }

  /**
   * @return one of <code>InstanceRef</code>, <code>TypeArgumentsRef</code> or
   * <code>Sentinel</code>
   */
  public Object getValue() {
    final JsonObject elem = (JsonObject)json.get("value");
    if (elem == null) return null;

    if (elem.get("type").getAsString().equals("@Instance")) return new InstanceRef(elem);
    if (elem.get("type").getAsString().equals("@TypeArguments")) return new TypeArgumentsRef(elem);
    if (elem.get("type").getAsString().equals("Sentinel")) return new Sentinel(elem);
    return null;
  }
}

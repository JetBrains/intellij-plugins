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
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Context} is a data structure which holds the captured variables for some closure.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Context extends Obj {

  public Context(JsonObject json) {
    super(json);
  }

  /**
   * The number of variables in this context.
   */
  public int getLength() {
    return getAsInt("length");
  }

  /**
   * The enclosing context for this context.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public Context getParent() {
    JsonObject obj = (JsonObject) json.get("parent");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new Context(obj);
  }

  /**
   * The variables in this context object.
   */
  public ElementList<ContextElement> getVariables() {
    return new ElementList<ContextElement>(json.get("variables").getAsJsonArray()) {
      @Override
      protected ContextElement basicGet(JsonArray array, int index) {
        return new ContextElement(array.get(index).getAsJsonObject());
      }
    };
  }
}

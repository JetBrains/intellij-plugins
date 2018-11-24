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
 * A {@link Context} is a data structure which holds the captured variables for some closure.
 */
public class Context extends Obj {

  public Context(JsonObject json) {
    super(json);
  }

  /**
   * The number of variables in this context.
   */
  public int getLength() {
    return json.get("length") == null ? -1 : json.get("length").getAsInt();
  }

  /**
   * The enclosing context for this context.
   */
  public Context getParent() {
    return json.get("parent") == null ? null : new Context((JsonObject) json.get("parent"));
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

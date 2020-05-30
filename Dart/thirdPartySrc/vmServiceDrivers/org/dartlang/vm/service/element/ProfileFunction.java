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
 * A {@link ProfileFunction} contains profiling information about a Dart or native function.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ProfileFunction extends Element {

  public ProfileFunction(JsonObject json) {
    super(json);
  }

  /**
   * The number of times function appeared on the top of the stack during sampling events.
   */
  public int getExclusiveTicks() {
    return getAsInt("exclusiveTicks");
  }

  /**
   * The function captured during profiling.
   *
   * @return one of <code>FuncRef</code> or <code>NativeFunction</code>
   */
  public Object getFunction() {
    final JsonObject elem = (JsonObject)json.get("function");
    if (elem == null) return null;

    if (elem.get("type").getAsString().equals("@Func")) return new FuncRef(elem);
    if (elem.get("type").getAsString().equals("NativeFunction")) return new NativeFunction(elem);
    return null;
  }

  /**
   * The number of times function appeared on the stack during sampling events.
   */
  public int getInclusiveTicks() {
    return getAsInt("inclusiveTicks");
  }

  /**
   * The kind of function this object represents.
   */
  public String getKind() {
    return getAsString("kind");
  }

  /**
   * The resolved URL for the script containing function.
   */
  public String getResolvedUrl() {
    return getAsString("resolvedUrl");
  }
}

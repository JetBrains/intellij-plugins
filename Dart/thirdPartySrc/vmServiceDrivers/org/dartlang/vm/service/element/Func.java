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
 * A {@link Func} represents a Dart language function.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Func extends Obj {

  public Func(JsonObject json) {
    super(json);
  }

  /**
   * The compiled code associated with this function.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public CodeRef getCode() {
    JsonObject obj = (JsonObject) json.get("code");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new CodeRef(obj);
  }

  /**
   * The location of this function in the source code.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public SourceLocation getLocation() {
    JsonObject obj = (JsonObject) json.get("location");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new SourceLocation(obj);
  }

  /**
   * The name of this function.
   */
  public String getName() {
    return getAsString("name");
  }

  /**
   * The owner of this function, which can be a Library, Class, or a Function.
   *
   * @return one of <code>LibraryRef</code>, <code>ClassRef</code> or <code>FuncRef</code>
   */
  public Object getOwner() {
    final JsonObject elem = (JsonObject)json.get("owner");
    if (elem == null) return null;

    if (elem.get("type").getAsString().equals("@Library")) return new LibraryRef(elem);
    if (elem.get("type").getAsString().equals("@Class")) return new ClassRef(elem);
    if (elem.get("type").getAsString().equals("@Func")) return new FuncRef(elem);
    return null;
  }
}

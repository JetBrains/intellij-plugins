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
 * A {@link Library} provides information about a Dart language library.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Library extends Obj {

  public Library(JsonObject json) {
    super(json);
  }

  /**
   * A list of all classes in this library.
   */
  public ElementList<ClassRef> getClasses() {
    return new ElementList<ClassRef>(json.get("classes").getAsJsonArray()) {
      @Override
      protected ClassRef basicGet(JsonArray array, int index) {
        return new ClassRef(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * Is this library debuggable? Default true.
   */
  public boolean getDebuggable() {
    return getAsBoolean("debuggable");
  }

  /**
   * A list of the imports for this library.
   */
  public ElementList<LibraryDependency> getDependencies() {
    return new ElementList<LibraryDependency>(json.get("dependencies").getAsJsonArray()) {
      @Override
      protected LibraryDependency basicGet(JsonArray array, int index) {
        return new LibraryDependency(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * A list of the top-level functions in this library.
   */
  public ElementList<FuncRef> getFunctions() {
    return new ElementList<FuncRef>(json.get("functions").getAsJsonArray()) {
      @Override
      protected FuncRef basicGet(JsonArray array, int index) {
        return new FuncRef(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The name of this library.
   */
  public String getName() {
    return getAsString("name");
  }

  /**
   * A list of the scripts which constitute this library.
   */
  public ElementList<ScriptRef> getScripts() {
    return new ElementList<ScriptRef>(json.get("scripts").getAsJsonArray()) {
      @Override
      protected ScriptRef basicGet(JsonArray array, int index) {
        return new ScriptRef(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The uri of this library.
   */
  public String getUri() {
    return getAsString("uri");
  }

  /**
   * A list of the top-level variables in this library.
   */
  public ElementList<FieldRef> getVariables() {
    return new ElementList<FieldRef>(json.get("variables").getAsJsonArray()) {
      @Override
      protected FieldRef basicGet(JsonArray array, int index) {
        return new FieldRef(array.get(index).getAsJsonObject());
      }
    };
  }
}

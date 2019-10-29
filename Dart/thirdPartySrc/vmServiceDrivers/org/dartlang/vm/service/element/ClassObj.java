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
 * A {@link ClassObj} provides information about a Dart language class.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ClassObj extends Obj {

  public ClassObj(JsonObject json) {
    super(json);
  }

  /**
   * The error which occurred during class finalization, if it exists.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public ErrorRef getError() {
    JsonObject obj = (JsonObject) json.get("error");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ErrorRef(obj);
  }

  /**
   * A list of fields in this class. Does not include fields from superclasses.
   */
  public ElementList<FieldRef> getFields() {
    return new ElementList<FieldRef>(json.get("fields").getAsJsonArray()) {
      @Override
      protected FieldRef basicGet(JsonArray array, int index) {
        return new FieldRef(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * A list of functions in this class. Does not include functions from superclasses.
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
   * A list of interface types for this class.
   *
   * The values will be of the kind: Type.
   */
  public ElementList<InstanceRef> getInterfaces() {
    return new ElementList<InstanceRef>(json.get("interfaces").getAsJsonArray()) {
      @Override
      protected InstanceRef basicGet(JsonArray array, int index) {
        return new InstanceRef(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The library which contains this class.
   */
  public LibraryRef getLibrary() {
    return new LibraryRef((JsonObject) json.get("library"));
  }

  /**
   * The location of this class in the source code.
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
   * The mixin type for this class, if any.
   *
   * The value will be of the kind: Type.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public InstanceRef getMixin() {
    JsonObject obj = (JsonObject) json.get("mixin");
    if (obj == null) return null;
    return new InstanceRef(obj);
  }

  /**
   * The name of this class.
   */
  public String getName() {
    return getAsString("name");
  }

  /**
   * A list of subclasses of this class.
   */
  public ElementList<ClassRef> getSubclasses() {
    return new ElementList<ClassRef>(json.get("subclasses").getAsJsonArray()) {
      @Override
      protected ClassRef basicGet(JsonArray array, int index) {
        return new ClassRef(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The superclass of this class, if any.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public ClassRef getSuperClass() {
    JsonObject obj = (JsonObject) json.get("super");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ClassRef(obj);
  }

  /**
   * The supertype for this class, if any.
   *
   * The value will be of the kind: Type.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public InstanceRef getSuperType() {
    JsonObject obj = (JsonObject) json.get("superType");
    if (obj == null) return null;
    return new InstanceRef(obj);
  }

  /**
   * Is this an abstract class?
   */
  public boolean isAbstract() {
    return getAsBoolean("abstract");
  }

  /**
   * Is this a const class?
   */
  public boolean isConst() {
    return getAsBoolean("const");
  }
}

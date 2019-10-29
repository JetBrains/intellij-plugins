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
 * A {@link Field} provides information about a Dart language field or variable.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Field extends Obj {

  public Field(JsonObject json) {
    super(json);
  }

  /**
   * The declared type of this field.
   *
   * The value will always be of one of the kinds: Type, TypeRef, TypeParameter, BoundedType.
   */
  public InstanceRef getDeclaredType() {
    return new InstanceRef((JsonObject) json.get("declaredType"));
  }

  /**
   * The location of this field in the source code.
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
   * The name of this field.
   */
  public String getName() {
    return getAsString("name");
  }

  /**
   * The owner of this field, which can be either a Library or a Class.
   */
  public ObjRef getOwner() {
    return new ObjRef((JsonObject) json.get("owner"));
  }

  /**
   * The value of this field, if the field is static.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public InstanceRef getStaticValue() {
    JsonObject obj = (JsonObject) json.get("staticValue");
    if (obj == null) return null;
    return new InstanceRef(obj);
  }

  /**
   * Is this field const?
   */
  public boolean isConst() {
    return getAsBoolean("const");
  }

  /**
   * Is this field final?
   */
  public boolean isFinal() {
    return getAsBoolean("final");
  }

  /**
   * Is this field static?
   */
  public boolean isStatic() {
    return getAsBoolean("static");
  }
}

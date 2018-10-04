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
 * A {@link Field} provides information about a Dart language field or variable.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
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
  public SourceLocation getLocation() {
    return json.get("location") == null ? null : new SourceLocation((JsonObject) json.get("location"));
  }

  /**
   * The name of this field.
   */
  public String getName() {
    return json.get("name").getAsString();
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
  public InstanceRef getStaticValue() {
    return json.get("staticValue") == null ? null : new InstanceRef((JsonObject) json.get("staticValue"));
  }

  /**
   * Is this field const?
   */
  public boolean isConst() {
    return json.get("const").getAsBoolean();
  }

  /**
   * Is this field final?
   */
  public boolean isFinal() {
    return json.get("final").getAsBoolean();
  }

  /**
   * Is this field static?
   */
  public boolean isStatic() {
    return json.get("static").getAsBoolean();
  }
}

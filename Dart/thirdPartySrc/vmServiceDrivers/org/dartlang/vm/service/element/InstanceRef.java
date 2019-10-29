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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

/**
 * {@link InstanceRef} is a reference to an {@link Instance}.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class InstanceRef extends ObjRef {

  public InstanceRef(JsonObject json) {
    super(json);
  }

  /**
   * Instance references always include their class.
   */
  public ClassRef getClassRef() {
    return new ClassRef((JsonObject) json.get("class"));
  }

  /**
   * The context associated with a Closure instance.
   *
   * Provided for instance kinds:
   *  - Closure
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public ContextRef getClosureContext() {
    JsonObject obj = (JsonObject) json.get("closureContext");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ContextRef(obj);
  }

  /**
   * The function associated with a Closure instance.
   *
   * Provided for instance kinds:
   *  - Closure
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public FuncRef getClosureFunction() {
    JsonObject obj = (JsonObject) json.get("closureFunction");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new FuncRef(obj);
  }

  /**
   * What kind of instance is this?
   */
  public InstanceKind getKind() {
    final JsonElement value = json.get("kind");
    try {
      return value == null ? InstanceKind.Unknown : InstanceKind.valueOf(value.getAsString());
    } catch (IllegalArgumentException e) {
      return InstanceKind.Unknown;
    }
  }

  /**
   * The length of a List or the number of associations in a Map or the number of codeunits in a
   * String.
   *
   * Provided for instance kinds:
   *  - String
   *  - List
   *  - Map
   *  - Uint8ClampedList
   *  - Uint8List
   *  - Uint16List
   *  - Uint32List
   *  - Uint64List
   *  - Int8List
   *  - Int16List
   *  - Int32List
   *  - Int64List
   *  - Float32List
   *  - Float64List
   *  - Int32x4List
   *  - Float32x4List
   *  - Float64x2List
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public int getLength() {
    return getAsInt("length");
  }

  /**
   * The name of a Type instance.
   *
   * Provided for instance kinds:
   *  - Type
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public String getName() {
    return getAsString("name");
  }

  /**
   * The parameterized class of a type parameter:
   *
   * Provided for instance kinds:
   *  - TypeParameter
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public ClassRef getParameterizedClass() {
    JsonObject obj = (JsonObject) json.get("parameterizedClass");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ClassRef(obj);
  }

  /**
   * The pattern of a RegExp instance.
   *
   * The pattern is always an instance of kind String.
   *
   * Provided for instance kinds:
   *  - RegExp
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public InstanceRef getPattern() {
    JsonObject obj = (JsonObject) json.get("pattern");
    if (obj == null) return null;
    return new InstanceRef(obj);
  }

  /**
   * The corresponding Class if this Type has a resolved typeClass.
   *
   * Provided for instance kinds:
   *  - Type
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public ClassRef getTypeClass() {
    JsonObject obj = (JsonObject) json.get("typeClass");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ClassRef(obj);
  }

  /**
   * The value of this instance as a string.
   *
   * Provided for the instance kinds:
   *  - Null (null)
   *  - Bool (true or false)
   *  - Double (suitable for passing to Double.parse())
   *  - Int (suitable for passing to int.parse())
   *  - String (value may be truncated)
   *  - Float32x4
   *  - Float64x2
   *  - Int32x4
   *  - StackTrace
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public String getValueAsString() {
    return getAsString("valueAsString");
  }

  /**
   * The valueAsString for String references may be truncated. If so, this property is added with
   * the value 'true'.
   *
   * New code should use 'length' and 'count' instead.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public boolean getValueAsStringIsTruncated() {
    final JsonElement elem = json.get("valueAsStringIsTruncated");
    return elem != null ? elem.getAsBoolean() : false;
  }

  /**
   * Returns whether this instance represents null.
   */
  public boolean isNull() {
    return getKind() == InstanceKind.Null;
  }
}

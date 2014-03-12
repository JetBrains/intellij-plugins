/*
 * Copyright 2012 Dart project authors.
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

package com.jetbrains.lang.dart.ide.runner.server.google;

import org.json.JSONObject;

/**
 * This class represents a VM value.
 */
public class VmValue extends VmRef {

  static VmValue createFrom(VmIsolate isolate, JSONObject obj) {
    if (obj == null) {
      return null;
    }

    // {"objectId":4,"kind":"object","text":"Instance of '_HttpServer@14117cc4'"}

    VmValue value = new VmValue(isolate);

    value.objectId = obj.optInt("objectId");
    value.classId = obj.optInt("classId");
    value.kind = obj.optString("kind");
    value.text = obj.optString("text");
    value.length = obj.optInt("length", 0);

    // for kind == function
    value.name = obj.optString("name");
    value.signature = obj.optString("signature");

    if (value.isFunction()) {
      value.text = value.name + value.signature;
    }

    return value;
  }

  private String text;

  private String kind;

  private int objectId;

  private int classId;

  private int length;

  private String name;

  private String signature;

  private VmObject vmObject;

  private VmValue(VmIsolate isolate) {
    super(isolate);
  }

  /**
   * Returns the classId for this value. This is only valid if isObject() is true.
   *
   * @return the classId for this value
   */
  public int getClassId() {
    return classId;
  }

  /**
   * @return one of "string", "number", "object", "boolean", "function".
   */
  public String getKind() {
    return kind;
  }

  /**
   * If this value is a list type, this method returns the list length;
   *
   * @return
   */
  public int getLength() {
    return length;
  }

  public String getName() {
    return name;
  }

  public int getObjectId() {
    return objectId;
  }

  public String getSignature() {
    return signature;
  }

  public String getText() {
    return text;
  }

  public VmObject getVmObject() {
    return vmObject;
  }

  public boolean isFunction() {
    return "function".equals(kind);
  }

  public boolean isList() {
    return "list".equals(getKind());
  }

  public boolean isNull() {
    if (isObject() && objectId == 0 && text == null) {
      return true;
    }

    return isObject() && (text == null || "null".equals(text));
  }

  public boolean isNumber() {
    return "number".equals(kind);
  }

  public boolean isObject() {
    return "object".equals(getKind());
  }

  public boolean isPrimitive() {
    return "number".equals(kind) || "integer".equals(kind) || "boolean".equals(kind)
           || "string".equals(kind);
  }

  public boolean isString() {
    return "string".equals(getKind());
  }

  @Override
  public String toString() {
    return getKind() + "," + getText();
  }

  public void setVmObject(VmObject object) {
    this.vmObject = object;
  }
}

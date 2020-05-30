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
 * See getInboundReferences.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class InboundReference extends Element {

  public InboundReference(JsonObject json) {
    super(json);
  }

  /**
   * If source is a field of an object, parentField is the field containing the inbound reference.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public FieldRef getParentField() {
    JsonObject obj = (JsonObject) json.get("parentField");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new FieldRef(obj);
  }

  /**
   * If source is a List, parentListIndex is the index of the inbound reference.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public int getParentListIndex() {
    return getAsInt("parentListIndex");
  }

  /**
   * The object holding the inbound reference.
   */
  public ObjRef getSource() {
    return new ObjRef((JsonObject) json.get("source"));
  }
}

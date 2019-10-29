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
 * See RetainingPath.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RetainingObject extends Element {

  public RetainingObject(JsonObject json) {
    super(json);
  }

  /**
   * The name of the field containing the retaining object within an object.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public String getParentField() {
    return getAsString("parentField");
  }

  /**
   * The offset of the retaining object in a containing list.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public int getParentListIndex() {
    return getAsInt("parentListIndex");
  }

  /**
   * The key mapping to the retaining object in a containing map.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public ObjRef getParentMapKey() {
    JsonObject obj = (JsonObject) json.get("parentMapKey");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ObjRef(obj);
  }

  /**
   * An object that is part of a retaining path.
   */
  public ObjRef getValue() {
    return new ObjRef((JsonObject) json.get("value"));
  }
}

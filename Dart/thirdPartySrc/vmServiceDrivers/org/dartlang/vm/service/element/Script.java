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
import java.util.List;

/**
 * A {@link Script} provides information about a Dart language script.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class Script extends Obj {

  public Script(JsonObject json) {
    super(json);
  }

  /**
   * The library which owns this script.
   */
  public LibraryRef getLibrary() {
    return new LibraryRef((JsonObject) json.get("library"));
  }

  /**
   * The source code for this script. This can be null for certain built-in scripts.
   *
   * Can return <code>null</code>.
   */
  public String getSource() {
    return json.get("source") == null ? null : json.get("source").getAsString();
  }

  /**
   * A table encoding a mapping from token position to line and column.
   */
  public List<List<Integer>> getTokenPosTable() {
    return getListListInt("tokenPosTable");
  }

  /**
   * The uri from which this script was loaded.
   */
  public String getUri() {
    return json.get("uri").getAsString();
  }
}

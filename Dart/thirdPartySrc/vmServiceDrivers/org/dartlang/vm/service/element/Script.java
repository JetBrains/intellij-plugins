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

import java.util.List;

/**
 * A {@link Script} provides information about a Dart language script.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Script extends Obj {

  public Script(JsonObject json) {
    super(json);
  }

  /**
   * Can return <code>null</code>.
   */
  @Nullable
  public int getColumnOffset() {
    return getAsInt("columnOffset");
  }

  /**
   * The library which owns this script.
   */
  public LibraryRef getLibrary() {
    return new LibraryRef((JsonObject) json.get("library"));
  }

  /**
   * Can return <code>null</code>.
   */
  @Nullable
  public int getLineOffset() {
    return getAsInt("lineOffset");
  }

  /**
   * The source code for this script. This can be null for certain built-in scripts.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public String getSource() {
    return getAsString("source");
  }

  /**
   * A table encoding a mapping from token position to line and column. This field is null if
   * sources aren't available.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public List<List<Integer>> getTokenPosTable() {
    return getListListInt("tokenPosTable");
  }

  /**
   * The uri from which this script was loaded.
   */
  public String getUri() {
    return getAsString("uri");
  }
}

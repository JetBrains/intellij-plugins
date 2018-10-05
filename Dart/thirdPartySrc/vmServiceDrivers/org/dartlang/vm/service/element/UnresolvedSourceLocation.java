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
 * The {@link UnresolvedSourceLocation} class is used to refer to an unresolved breakpoint
 * location. As such, it is meant to approximate the final location of the breakpoint but it is not
 * exact.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class UnresolvedSourceLocation extends Response {

  public UnresolvedSourceLocation(JsonObject json) {
    super(json);
  }

  /**
   * An approximate column number for the source location. This may change when the location is
   * resolved.
   *
   * Can return <code>null</code>.
   */
  public int getColumn() {
    return json.get("column") == null ? -1 : json.get("column").getAsInt();
  }

  /**
   * An approximate line number for the source location. This may change when the location is
   * resolved.
   *
   * Can return <code>null</code>.
   */
  public int getLine() {
    return json.get("line") == null ? -1 : json.get("line").getAsInt();
  }

  /**
   * The script containing the source location if the script has been loaded.
   *
   * Can return <code>null</code>.
   */
  public ScriptRef getScript() {
    return json.get("script") == null ? null : new ScriptRef((JsonObject) json.get("script"));
  }

  /**
   * The uri of the script containing the source location if the script has yet to be loaded.
   *
   * Can return <code>null</code>.
   */
  public String getScriptUri() {
    return json.get("scriptUri") == null ? null : json.get("scriptUri").getAsString();
  }

  /**
   * An approximate token position for the source location. This may change when the location is
   * resolved.
   *
   * Can return <code>null</code>.
   */
  public int getTokenPos() {
    return json.get("tokenPos") == null ? -1 : json.get("tokenPos").getAsInt();
  }
}

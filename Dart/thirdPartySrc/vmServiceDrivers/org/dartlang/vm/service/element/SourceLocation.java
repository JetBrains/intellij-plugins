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
 * The {@link SourceLocation} class is used to designate a position or range in some script.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class SourceLocation extends Response {

  public SourceLocation(JsonObject json) {
    super(json);
  }

  /**
   * The last token of the location if this is a range.
   *
   * Can return <code>null</code>.
   */
  public int getEndTokenPos() {
    return json.get("endTokenPos") == null ? -1 : json.get("endTokenPos").getAsInt();
  }

  /**
   * The script containing the source location.
   */
  public ScriptRef getScript() {
    return new ScriptRef((JsonObject) json.get("script"));
  }

  /**
   * The first token of the location.
   */
  public int getTokenPos() {
    return json.get("tokenPos") == null ? -1 : json.get("tokenPos").getAsInt();
  }
}

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
 * See Versioning.
 */
public class Version extends Response {

  public Version(JsonObject json) {
    super(json);
  }

  /**
   * The major version number is incremented when the protocol is changed in a potentially
   * incompatible way.
   */
  public int getMajor() {
    return json.get("major") == null ? -1 : json.get("major").getAsInt();
  }

  /**
   * The minor version number is incremented when the protocol is changed in a backwards compatible
   * way.
   */
  public int getMinor() {
    return json.get("minor") == null ? -1 : json.get("minor").getAsInt();
  }
}

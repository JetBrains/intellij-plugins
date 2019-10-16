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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@SuppressWarnings({"WeakerAccess", "unused"})
public class VM extends Response {

  public VM(JsonObject json) {
    super(json);
  }

  /**
   * Word length on target architecture (e.g. 32, 64).
   */
  public int getArchitectureBits() {
    return getAsInt("architectureBits");
  }

  /**
   * The CPU we are actually running on.
   */
  public String getHostCPU() {
    return getAsString("hostCPU");
  }

  /**
   * A list of isolates running in the VM.
   */
  public ElementList<IsolateRef> getIsolates() {
    return new ElementList<IsolateRef>(json.get("isolates").getAsJsonArray()) {
      @Override
      protected IsolateRef basicGet(JsonArray array, int index) {
        return new IsolateRef(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * A name identifying this vm. Not guaranteed to be unique.
   */
  public String getName() {
    return getAsString("name");
  }

  /**
   * The operating system we are running on.
   */
  public String getOperatingSystem() {
    return getAsString("operatingSystem");
  }

  /**
   * The process id for the VM.
   */
  public int getPid() {
    return getAsInt("pid");
  }

  /**
   * The time that the VM started in milliseconds since the epoch.
   *
   * Suitable to pass to DateTime.fromMillisecondsSinceEpoch.
   */
  public int getStartTime() {
    return getAsInt("startTime");
  }

  /**
   * The CPU we are generating code for.
   */
  public String getTargetCPU() {
    return getAsString("targetCPU");
  }

  /**
   * The Dart VM version string.
   */
  public String getVersion() {
    return getAsString("version");
  }
}

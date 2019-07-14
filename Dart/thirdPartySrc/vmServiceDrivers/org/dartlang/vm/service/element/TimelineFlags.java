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

@SuppressWarnings({"WeakerAccess", "unused"})
public class TimelineFlags extends Response {

  public TimelineFlags(JsonObject json) {
    super(json);
  }

  /**
   * The list of all available timeline streams.
   */
  public List<String> getAvailableStreams() {
    return getListString("availableStreams");
  }

  /**
   * The list of timeline streams that are currently enabled.
   */
  public List<String> getRecordedStreams() {
    return getListString("recordedStreams");
  }

  /**
   * The name of the recorder currently in use. Recorder types include, but are not limited to:
   * Callback, Endless, Fuchsia, Ring, Startup, and Systrace. Set to "null" if no recorder is
   * currently set.
   */
  public String getRecorderName() {
    return json.get("recorderName").getAsString();
  }
}

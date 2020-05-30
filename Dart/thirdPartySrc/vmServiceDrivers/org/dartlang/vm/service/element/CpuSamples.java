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

/**
 * See getCpuSamples and CpuSample.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CpuSamples extends Response {

  public CpuSamples(JsonObject json) {
    super(json);
  }

  /**
   * A list of functions seen in the relevant samples. These references can be looked up using the
   * indicies provided in a `CpuSample` `stack` to determine which function was on the stack.
   */
  public ElementList<ProfileFunction> getFunctions() {
    return new ElementList<ProfileFunction>(json.get("functions").getAsJsonArray()) {
      @Override
      protected ProfileFunction basicGet(JsonArray array, int index) {
        return new ProfileFunction(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The maximum possible stack depth for samples.
   */
  public int getMaxStackDepth() {
    return getAsInt("maxStackDepth");
  }

  /**
   * The process ID for the VM.
   */
  public int getPid() {
    return getAsInt("pid");
  }

  /**
   * The number of samples returned.
   */
  public int getSampleCount() {
    return getAsInt("sampleCount");
  }

  /**
   * The sampling rate for the profiler in microseconds.
   */
  public int getSamplePeriod() {
    return getAsInt("samplePeriod");
  }

  /**
   * A list of samples collected in the range `[timeOriginMicros, timeOriginMicros +
   * timeExtentMicros]`
   */
  public ElementList<CpuSample> getSamples() {
    return new ElementList<CpuSample>(json.get("samples").getAsJsonArray()) {
      @Override
      protected CpuSample basicGet(JsonArray array, int index) {
        return new CpuSample(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The duration of time covered by the returned samples.
   */
  public int getTimeExtentMicros() {
    return getAsInt("timeExtentMicros");
  }

  /**
   * The start of the period of time in which the returned samples were collected.
   */
  public int getTimeOriginMicros() {
    return getAsInt("timeOriginMicros");
  }

  /**
   * The timespan the set of returned samples covers, in microseconds.
   */
  public int getTimeSpan() {
    return getAsInt("timeSpan");
  }
}

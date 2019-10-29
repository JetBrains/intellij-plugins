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
 * See getCpuSamples and CpuSamples.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CpuSample extends Element {

  public CpuSample(JsonObject json) {
    super(json);
  }

  /**
   * The call stack at the time this sample was collected. The stack is to be interpreted as top to
   * bottom. Each element in this array is a key into the `functions` array in `CpuSamples`.
   *
   * Example:
   *
   * `functions[stack[0]] = @Function(bar())` `functions[stack[1]] = @Function(foo())`
   * `functions[stack[2]] = @Function(main())`
   */
  public List<Integer> getStack() {
    return getListInt("stack");
  }

  /**
   * The thread ID representing the thread on which this sample was collected.
   */
  public int getTid() {
    return getAsInt("tid");
  }

  /**
   * The time this sample was collected in microseconds.
   */
  public long getTimestamp() {
    return json.get("timestamp") == null ? -1 : json.get("timestamp").getAsLong();
  }

  /**
   * Provided and set to true if the sample's stack was truncated. This can happen if the stack is
   * deeper than the `stackDepth` in the `CpuSamples` response.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public boolean getTruncated() {
    return getAsBoolean("truncated");
  }

  /**
   * The name of the User tag set when this sample was collected. Omitted if no User tag was set
   * when this sample was collected.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public String getUserTag() {
    return getAsString("userTag");
  }

  /**
   * The name of VM tag set when this sample was collected. Omitted if the VM tag for the sample is
   * not considered valid.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public String getVmTag() {
    return getAsString("vmTag");
  }
}

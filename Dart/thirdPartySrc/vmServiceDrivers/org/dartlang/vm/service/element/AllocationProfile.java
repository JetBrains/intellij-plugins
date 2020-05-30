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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"WeakerAccess", "unused"})
public class AllocationProfile extends Response {

  public AllocationProfile(JsonObject json) {
    super(json);
  }

  /**
   * The timestamp of the last accumulator reset.
   *
   * If the accumulators have not been reset, this field is not present.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public int getDateLastAccumulatorReset() {
    return getAsInt("dateLastAccumulatorReset");
  }

  /**
   * The timestamp of the last manually triggered GC.
   *
   * If a GC has not been triggered manually, this field is not present.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public int getDateLastServiceGC() {
    return getAsInt("dateLastServiceGC");
  }

  /**
   * Allocation information for all class types.
   */
  public ElementList<ClassHeapStats> getMembers() {
    return new ElementList<ClassHeapStats>(json.get("members").getAsJsonArray()) {
      @Override
      protected ClassHeapStats basicGet(JsonArray array, int index) {
        return new ClassHeapStats(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * Information about memory usage for the isolate.
   */
  public MemoryUsage getMemoryUsage() {
    return new MemoryUsage((JsonObject) json.get("memoryUsage"));
  }
}

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
 * An {@link MemoryUsage} object provides heap usage information for a specific isolate at a given
 * point in time.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MemoryUsage extends Response {

  public MemoryUsage(JsonObject json) {
    super(json);
  }

  /**
   * The amount of non-Dart memory that is retained by Dart objects. For example, memory associated
   * with Dart objects through APIs such as Dart_NewWeakPersistentHandle and
   * Dart_NewExternalTypedData.  This usage is only as accurate as the values supplied to these
   * APIs from the VM embedder or native extensions. This external memory applies GC pressure, but
   * is separate from heapUsage and heapCapacity.
   */
  public int getExternalUsage() {
    return json.get("externalUsage") == null ? -1 : json.get("externalUsage").getAsInt();
  }

  /**
   * The total capacity of the heap in bytes. This is the amount of memory used by the Dart heap
   * from the perspective of the operating system.
   */
  public int getHeapCapacity() {
    return json.get("heapCapacity") == null ? -1 : json.get("heapCapacity").getAsInt();
  }

  /**
   * The current heap memory usage in bytes. Heap usage is always less than or equal to the heap
   * capacity.
   */
  public int getHeapUsage() {
    return json.get("heapUsage") == null ? -1 : json.get("heapUsage").getAsInt();
  }
}

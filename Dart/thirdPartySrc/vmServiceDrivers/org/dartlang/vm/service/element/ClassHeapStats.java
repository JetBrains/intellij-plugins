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
public class ClassHeapStats extends Response {

  public ClassHeapStats(JsonObject json) {
    super(json);
  }

  /**
   * The number of bytes allocated for instances of class since the accumulator was last reset.
   */
  public int getAccumulatedSize() {
    return json.get("accumulatedSize") == null ? -1 : json.get("accumulatedSize").getAsInt();
  }

  /**
   * The number of bytes currently allocated for instances of class.
   */
  public int getBytesCurrent() {
    return json.get("bytesCurrent") == null ? -1 : json.get("bytesCurrent").getAsInt();
  }

  /**
   * The class for which this memory information is associated.
   */
  public ClassRef getClassRef() {
    return new ClassRef((JsonObject) json.get("class"));
  }

  /**
   * The number of instances of class which have been allocated since the accumulator was last
   * reset.
   */
  public int getInstancesAccumulated() {
    return json.get("instancesAccumulated") == null ? -1 : json.get("instancesAccumulated").getAsInt();
  }

  /**
   * The number of instances of class which are currently alive.
   */
  public int getInstancesCurrent() {
    return json.get("instancesCurrent") == null ? -1 : json.get("instancesCurrent").getAsInt();
  }

  public List<Integer> getNew() {
    return getListInt("new");
  }

  public List<Integer> getOld() {
    return getListInt("old");
  }

  public int getPromotedBytes() {
    return json.get("promotedBytes") == null ? -1 : json.get("promotedBytes").getAsInt();
  }

  public int getPromotedInstances() {
    return json.get("promotedInstances") == null ? -1 : json.get("promotedInstances").getAsInt();
  }
}

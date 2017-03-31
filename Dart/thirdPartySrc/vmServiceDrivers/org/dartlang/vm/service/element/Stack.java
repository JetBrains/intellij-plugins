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

public class Stack extends Response {

  public Stack(JsonObject json) {
    super(json);
  }

  public ElementList<Frame> getAsyncCausalFrames() {
    if (json.get("asyncCausalFrames") == null) return null;
    
    return new ElementList<Frame>(json.get("asyncCausalFrames").getAsJsonArray()) {
      @Override
      protected Frame basicGet(JsonArray array, int index) {
        return new Frame(array.get(index).getAsJsonObject());
      }
    };
  }

  public ElementList<Frame> getAwaiterFrames() {
    if (json.get("awaiterFrames") == null) return null;

    return new ElementList<Frame>(json.get("awaiterFrames").getAsJsonArray()) {
      @Override
      protected Frame basicGet(JsonArray array, int index) {
        return new Frame(array.get(index).getAsJsonObject());
      }
    };
  }

  public ElementList<Frame> getFrames() {
    return new ElementList<Frame>(json.get("frames").getAsJsonArray()) {
      @Override
      protected Frame basicGet(JsonArray array, int index) {
        return new Frame(array.get(index).getAsJsonObject());
      }
    };
  }

  public ElementList<Message> getMessages() {
    return new ElementList<Message>(json.get("messages").getAsJsonArray()) {
      @Override
      protected Message basicGet(JsonArray array, int index) {
        return new Message(array.get(index).getAsJsonObject());
      }
    };
  }
}

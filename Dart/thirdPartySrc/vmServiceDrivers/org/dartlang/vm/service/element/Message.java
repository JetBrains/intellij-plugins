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

/**
 * A {@link Message} provides information about a pending isolate message and the function that
 * will be invoked to handle it.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Message extends Response {

  public Message(JsonObject json) {
    super(json);
  }

  /**
   * A reference to the function that will be invoked to handle this message.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public FuncRef getHandler() {
    JsonObject obj = (JsonObject) json.get("handler");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new FuncRef(obj);
  }

  /**
   * The index in the isolate's message queue. The 0th message being the next message to be
   * processed.
   */
  public int getIndex() {
    return getAsInt("index");
  }

  /**
   * The source location of handler.
   *
   * Can return <code>null</code>.
   */
  @Nullable
  public SourceLocation getLocation() {
    JsonObject obj = (JsonObject) json.get("location");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new SourceLocation(obj);
  }

  /**
   * An instance id for the decoded message. This id can be passed to other RPCs, for example,
   * getObject or evaluate.
   */
  public String getMessageObjectId() {
    return getAsString("messageObjectId");
  }

  /**
   * An advisory name describing this message.
   */
  public String getName() {
    return getAsString("name");
  }

  /**
   * The size (bytes) of the encoded message.
   */
  public int getSize() {
    return getAsInt("size");
  }
}

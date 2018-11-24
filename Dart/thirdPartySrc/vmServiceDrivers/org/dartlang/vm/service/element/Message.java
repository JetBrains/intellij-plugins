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
 * A {@link Message} provides information about a pending isolate message and the function that
 * will be invoked to handle it.
 */
public class Message extends Response {

  public Message(JsonObject json) {
    super(json);
  }

  /**
   * A reference to the function that will be invoked to handle this message.
   */
  public FuncRef getHandler() {
    return json.get("handler") == null ? null : new FuncRef((JsonObject) json.get("handler"));
  }

  /**
   * The index in the isolate's message queue. The 0th message being the next message to be
   * processed.
   */
  public int getIndex() {
    return json.get("index") == null ? -1 : json.get("index").getAsInt();
  }

  /**
   * The source location of handler.
   */
  public SourceLocation getLocation() {
    return json.get("location") == null ? null : new SourceLocation((JsonObject) json.get("location"));
  }

  /**
   * An instance id for the decoded message. This id can be passed to other RPCs, for example,
   * getObject or evaluate.
   */
  public String getMessageObjectId() {
    return json.get("messageObjectId").getAsString();
  }

  /**
   * An advisory name describing this message.
   */
  public String getName() {
    return json.get("name").getAsString();
  }

  /**
   * The size (bytes) of the encoded message.
   */
  public int getSize() {
    return json.get("size") == null ? -1 : json.get("size").getAsInt();
  }
}

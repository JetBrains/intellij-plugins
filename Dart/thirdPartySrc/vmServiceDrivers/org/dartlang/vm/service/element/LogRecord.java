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

@SuppressWarnings({"WeakerAccess", "unused"})
public class LogRecord extends Response {

  public LogRecord(JsonObject json) {
    super(json);
  }

  /**
   * An error object associated with this log event.
   */
  public InstanceRef getError() {
    return new InstanceRef((JsonObject) json.get("error"));
  }

  /**
   * The severity level (a value between 0 and 2000).
   *
   * See the package:logging `Level` class for an overview of the possible values.
   */
  public int getLevel() {
    return json.get("level") == null ? -1 : json.get("level").getAsInt();
  }

  /**
   * The name of the source of the log message.
   */
  public InstanceRef getLoggerName() {
    return new InstanceRef((JsonObject) json.get("loggerName"));
  }

  /**
   * The log message.
   */
  public InstanceRef getMessage() {
    return new InstanceRef((JsonObject) json.get("message"));
  }

  /**
   * A monotonically increasing sequence number.
   */
  public int getSequenceNumber() {
    return json.get("sequenceNumber") == null ? -1 : json.get("sequenceNumber").getAsInt();
  }

  /**
   * A stack trace associated with this log event.
   */
  public InstanceRef getStackTrace() {
    return new InstanceRef((JsonObject) json.get("stackTrace"));
  }

  /**
   * The timestamp.
   */
  public int getTime() {
    return json.get("time") == null ? -1 : json.get("time").getAsInt();
  }

  /**
   * The zone where the log was emitted.
   */
  public InstanceRef getZone() {
    return new InstanceRef((JsonObject) json.get("zone"));
  }
}

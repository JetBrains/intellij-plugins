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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * An {@link Event} is an asynchronous notification from the VM. It is delivered only when the
 * client has subscribed to an event stream using the streamListen RPC.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Event extends Response {

  public Event(JsonObject json) {
    super(json);
  }

  /**
   * The alias of the registered service.
   *
   * This is provided for the event kinds:
   *  - ServiceRegistered
   *
   * Can return <code>null</code>.
   */
  public String getAlias() {
    return json.get("alias") == null ? null : json.get("alias").getAsString();
  }

  /**
   * Is the isolate paused at an await, yield, or yield* statement?
   *
   * This is provided for the event kinds:
   *  - PauseBreakpoint
   *  - PauseInterrupted
   *
   * Can return <code>null</code>.
   */
  public boolean getAtAsyncSuspension() {
    return json.get("atAsyncSuspension") == null ? false : json.get("atAsyncSuspension").getAsBoolean();
  }

  /**
   * The breakpoint which was added, removed, or resolved.
   *
   * This is provided for the event kinds:
   *  - PauseBreakpoint
   *  - BreakpointAdded
   *  - BreakpointRemoved
   *  - BreakpointResolved
   *
   * Can return <code>null</code>.
   */
  public Breakpoint getBreakpoint() {
    JsonObject obj = (JsonObject) json.get("breakpoint");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new Breakpoint(obj);
  }

  /**
   * An array of bytes, encoded as a base64 string.
   *
   * This is provided for the WriteEvent event.
   *
   * Can return <code>null</code>.
   */
  public String getBytes() {
    return json.get("bytes") == null ? null : json.get("bytes").getAsString();
  }

  /**
   * The exception associated with this event, if this is a PauseException event.
   *
   * Can return <code>null</code>.
   */
  public InstanceRef getException() {
    JsonObject obj = (JsonObject) json.get("exception");
    if (obj == null) return null;
    return new InstanceRef(obj);
  }

  /**
   * The extension event data.
   *
   * This is provided for the Extension event.
   *
   * Can return <code>null</code>.
   */
  public ExtensionData getExtensionData() {
    JsonObject obj = (JsonObject) json.get("extensionData");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new ExtensionData(obj);
  }

  /**
   * The extension event kind.
   *
   * This is provided for the Extension event.
   *
   * Can return <code>null</code>.
   */
  public String getExtensionKind() {
    return json.get("extensionKind") == null ? null : json.get("extensionKind").getAsString();
  }

  /**
   * The RPC name of the extension that was added.
   *
   * This is provided for the ServiceExtensionAdded event.
   *
   * Can return <code>null</code>.
   */
  public String getExtensionRPC() {
    return json.get("extensionRPC") == null ? null : json.get("extensionRPC").getAsString();
  }

  /**
   * The argument passed to dart:developer.inspect.
   *
   * This is provided for the Inspect event.
   *
   * Can return <code>null</code>.
   */
  public InstanceRef getInspectee() {
    JsonObject obj = (JsonObject) json.get("inspectee");
    if (obj == null) return null;
    return new InstanceRef(obj);
  }

  /**
   * The isolate with which this event is associated.
   *
   * This is provided for all event kinds except for:
   *  - VMUpdate
   *
   * Can return <code>null</code>.
   */
  public IsolateRef getIsolate() {
    JsonObject obj = (JsonObject) json.get("isolate");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new IsolateRef(obj);
  }

  /**
   * What kind of event is this?
   */
  public EventKind getKind() {
    final JsonElement value = json.get("kind");
    try {
      return value == null ? EventKind.Unknown : EventKind.valueOf(value.getAsString());
    } catch (IllegalArgumentException e) {
      return EventKind.Unknown;
    }
  }

  /**
   * LogRecord data.
   *
   * This is provided for the Logging event.
   *
   * Can return <code>null</code>.
   */
  public LogRecord getLogRecord() {
    JsonObject obj = (JsonObject) json.get("logRecord");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new LogRecord(obj);
  }

  /**
   * The RPC method that should be used to invoke the service.
   *
   * This is provided for the event kinds:
   *  - ServiceRegistered
   *  - ServiceUnregistered
   *
   * Can return <code>null</code>.
   */
  public String getMethod() {
    return json.get("method") == null ? null : json.get("method").getAsString();
  }

  /**
   * The list of breakpoints at which we are currently paused for a PauseBreakpoint event.
   *
   * This list may be empty. For example, while single-stepping, the VM sends a PauseBreakpoint
   * event with no breakpoints.
   *
   * If there is more than one breakpoint set at the program position, then all of them will be
   * provided.
   *
   * This is provided for the event kinds:
   *  - PauseBreakpoint
   *
   * Can return <code>null</code>.
   */
  public ElementList<Breakpoint> getPauseBreakpoints() {
    if (json.get("pauseBreakpoints") == null) return null;
    
    return new ElementList<Breakpoint>(json.get("pauseBreakpoints").getAsJsonArray()) {
      @Override
      protected Breakpoint basicGet(JsonArray array, int index) {
        return new Breakpoint(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The service identifier.
   *
   * This is provided for the event kinds:
   *  - ServiceRegistered
   *  - ServiceUnregistered
   *
   * Can return <code>null</code>.
   */
  public String getService() {
    return json.get("service") == null ? null : json.get("service").getAsString();
  }

  /**
   * The status (success or failure) related to the event. This is provided for the event kinds:
   *  - IsolateReloaded
   *  - IsolateSpawn
   *
   * Can return <code>null</code>.
   */
  public String getStatus() {
    return json.get("status") == null ? null : json.get("status").getAsString();
  }

  /**
   * An array of TimelineEvents
   *
   * This is provided for the TimelineEvents event.
   *
   * Can return <code>null</code>.
   */
  public ElementList<TimelineEvent> getTimelineEvents() {
    if (json.get("timelineEvents") == null) return null;
    
    return new ElementList<TimelineEvent>(json.get("timelineEvents").getAsJsonArray()) {
      @Override
      protected TimelineEvent basicGet(JsonArray array, int index) {
        return new TimelineEvent(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The timestamp (in milliseconds since the epoch) associated with this event. For some isolate
   * pause events, the timestamp is from when the isolate was paused. For other events, the
   * timestamp is from when the event was created.
   */
  public long getTimestamp() {
    return json.get("timestamp") == null ? -1 : json.get("timestamp").getAsLong();
  }

  /**
   * The top stack frame associated with this event, if applicable.
   *
   * This is provided for the event kinds:
   *  - PauseBreakpoint
   *  - PauseInterrupted
   *  - PauseException
   *
   * For PauseInterrupted events, there will be no top frame if the isolate is idle (waiting in the
   * message loop).
   *
   * For the Resume event, the top frame is provided at all times except for the initial resume
   * event that is delivered when an isolate begins execution.
   *
   * Can return <code>null</code>.
   */
  public Frame getTopFrame() {
    JsonObject obj = (JsonObject) json.get("topFrame");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new Frame(obj);
  }

  /**
   * The vm with which this event is associated.
   *
   * This is provided for the event kind:
   *  - VMUpdate
   *
   * Can return <code>null</code>.
   */
  public VMRef getVm() {
    JsonObject obj = (JsonObject) json.get("vm");
    if (obj == null) return null;
    final String type = json.get("type").getAsString();
    if ("Instance".equals(type) || "@Instance".equals(type)) {
      final String kind = json.get("kind").getAsString();
      if ("Null".equals(kind)) return null;
    }
    return new VMRef(obj);
  }
}

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
 * An {@link Event} is an asynchronous notification from the VM. It is delivered only when the
 * client has subscribed to an event stream using the streamListen RPC.
 */
public class Event extends Response {

  public Event(JsonObject json) {
    super(json);
  }

  /**
   * Is the isolate paused at an await, yield, or yield* statement?
   *
   * This is provided for the event kinds:
   *  - PauseBreakpoint
   *  - PauseInterrupted
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
   */
  public Breakpoint getBreakpoint() {
    return json.get("breakpoint") == null ? null : new Breakpoint((JsonObject) json.get("breakpoint"));
  }

  /**
   * An array of bytes, encoded as a base64 string.
   *
   * This is provided for the WriteEvent event.
   */
  public String getBytes() {
    return json.get("bytes").getAsString();
  }

  /**
   * The exception associated with this event, if this is a PauseException event.
   */
  public InstanceRef getException() {
    return json.get("exception") == null ? null : new InstanceRef((JsonObject) json.get("exception"));
  }

  /**
   * The extension event data.
   *
   * This is provided for the Extension event.
   */
  public ExtensionData getExtensionData() {
    return json.get("extensionData") == null ? null : new ExtensionData((JsonObject) json.get("extensionData"));
  }

  /**
   * The extension event kind.
   *
   * This is provided for the Extension event.
   */
  public String getExtensionKind() {
    return json.get("extensionKind").getAsString();
  }

  /**
   * The RPC name of the extension that was added.
   *
   * This is provided for the ServiceExtensionAdded event.
   */
  public String getExtensionRPC() {
    return json.get("extensionRPC").getAsString();
  }

  /**
   * The argument passed to dart:developer.inspect.
   *
   * This is provided for the Inspect event.
   */
  public InstanceRef getInspectee() {
    return json.get("inspectee") == null ? null : new InstanceRef((JsonObject) json.get("inspectee"));
  }

  /**
   * The isolate with which this event is associated.
   *
   * This is provided for all event kinds except for:
   *  - VMUpdate
   */
  public IsolateRef getIsolate() {
    return json.get("isolate") == null ? null : new IsolateRef((JsonObject) json.get("isolate"));
  }

  /**
   * What kind of event is this?
   */
  public EventKind getKind() {
    String name = json.get("kind").getAsString();
    try {
      return EventKind.valueOf(name);
    } catch (IllegalArgumentException e) {
      return EventKind.Unknown;
    }
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
   */
  public ElementList<Breakpoint> getPauseBreakpoints() {
    return new ElementList<Breakpoint>(json.get("pauseBreakpoints").getAsJsonArray()) {
      @Override
      protected Breakpoint basicGet(JsonArray array, int index) {
        return new Breakpoint(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The status (success or failure) related to the event. This is provided for the event kinds:
   *  - IsolateReloaded
   *  - IsolateSpawn
   */
  public String getStatus() {
    return json.get("status").getAsString();
  }

  /**
   * An array of TimelineEvents
   *
   * This is provided for the TimelineEvents event.
   */
  public ElementList<TimelineEvent> getTimelineEvents() {
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
  public int getTimestamp() {
    return json.get("timestamp") == null ? -1 : json.get("timestamp").getAsInt();
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
   */
  public Frame getTopFrame() {
    return json.get("topFrame") == null ? null : new Frame((JsonObject) json.get("topFrame"));
  }

  /**
   * The vm with which this event is associated.
   *
   * This is provided for the event kind:
   *  - VMUpdate
   */
  public VMRef getVm() {
    return json.get("vm") == null ? null : new VMRef((JsonObject) json.get("vm"));
  }
}

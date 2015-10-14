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

/**
 * Adding new values to {@link EventKind} is considered a backwards compatible change. Clients
 * should ignore unrecognized events.
 */
public enum EventKind {

  /**
   * Notification that a new isolate has started.
   */
  IsolateStart,

  /**
   * Notification that an isolate is ready to run.
   */
  IsolateRunnable,

  /**
   * Notification that an isolate has exited.
   */
  IsolateExit,

  /**
   * Notification that isolate identifying information has changed. Currently used to notify of
   * changes to the isolate debugging name via setName.
   */
  IsolateUpdate,

  /**
   * An isolate has paused at start, before executing code.
   */
  PauseStart,

  /**
   * An isolate has paused at exit, before terminating.
   */
  PauseExit,

  /**
   * An isolate has paused at a breakpoint or due to stepping.
   */
  PauseBreakpoint,

  /**
   * An isolate has paused due to interruption via pause.
   */
  PauseInterrupted,

  /**
   * An isolate has paused due to an exception.
   */
  PauseException,

  /**
   * An isolate has started or resumed execution.
   */
  Resume,

  /**
   * A breakpoint has been added for an isolate.
   */
  BreakpointAdded,

  /**
   * An unresolved breakpoint has been resolved for an isolate.
   */
  BreakpointResolved,

  /**
   * A breakpoint has been removed.
   */
  BreakpointRemoved,

  /**
   * A garbage collection event.
   */
  GC,

  /**
   * Notification of bytes written, for example, to stdout/stderr.
   */
  WriteEvent;
}

/*
 * Copyright (c) 2012, the Dart project authors.
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

package com.jetbrains.lang.dart.ide.runner.server.google;

import java.util.List;

/**
 * A listener for VM debugging events.
 */
public interface VmListener {

  public static enum PausedReason {
    breakpoint,
    exception,
    interrupted,
    unknown;

    public static PausedReason parse(String str) {
      try {
        return valueOf(str);
      }
      catch (Throwable t) {
        return unknown;
      }
    }
  }

  /**
   * Sent when the given breakpoint is resolved. The breakpoint can be returned with a new, updated
   * location.
   *
   * @param isolate
   * @param breakpoint
   */
  public void breakpointResolved(VmIsolate isolate, VmBreakpoint breakpoint);

  /**
   * Handle the connection closed event.
   *
   * @param connection the VM connection
   */
  public void connectionClosed(VmConnection connection);

  /**
   * Handle the connection opened event.
   *
   * @param connection the VM connection
   */
  public void connectionOpened(VmConnection connection);

  /**
   * Handle the debugger paused event.
   *
   * @param reason    possible values are "breakpoint" and "exception"
   * @param isolate
   * @param frames
   * @param exception can be null
   */
  public void debuggerPaused(PausedReason reason, VmIsolate isolate, List<VmCallFrame> frames,
                             VmValue exception, boolean isStepping);

  /**
   * Handle the debugger resumed event.
   */
  public void debuggerResumed(VmIsolate isolate);

  /**
   * Handle the isolate created event.
   *
   * @param isolate
   */
  public void isolateCreated(VmIsolate isolate);

  /**
   * Handle the isolate shutdown event.
   *
   * @param isolate
   */
  public void isolateShutdown(VmIsolate isolate);
}

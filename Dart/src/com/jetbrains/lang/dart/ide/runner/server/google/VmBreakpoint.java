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

/**
 * The representation of a VM breakpoint.
 */
public class VmBreakpoint {
  private VmIsolate isolate;
  private VmLocation location;
  private int breakpointId;

  VmBreakpoint(VmIsolate isolate, VmLocation location, int breakpointId) {
    this.isolate = isolate;
    this.location = location;
    this.breakpointId = breakpointId;
  }

  public int getBreakpointId() {
    return breakpointId;
  }

  public VmIsolate getIsolate() {
    return isolate;
  }

  public VmLocation getLocation() {
    return location;
  }

  @Override
  public String toString() {
    return "[breakpoint " + getBreakpointId() + "," + getLocation() + "," + isolate.getName() + "]";
  }

  protected void updateLocation(VmLocation location) {
    this.location = location;
  }
}

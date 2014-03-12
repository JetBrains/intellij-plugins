/*
 * Copyright 2012 Dart project authors.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A VM frame object.
 */
public class VmCallFrame extends VmRef {

  static List<VmCallFrame> createFrom(VmIsolate isolate, JSONArray arr) throws JSONException {
    List<VmCallFrame> frames = new ArrayList<VmCallFrame>();

    for (int i = 0; i < arr.length(); i++) {
      VmCallFrame frame = createFrom(isolate, arr.getJSONObject(i));

      // If we are on the first frame and there are at least 3 frames:
      if (i == 0 && arr.length() > 2) {
        if (DebuggerUtils.isInternalMethodName(frame.getFunctionName())) {
          // Strip out the first frame if it's _noSuchMethod. There will be another
          // "Object.noSuchMethod" on the stack. This sucks, but it's where we're choosing to put
          // the fix.
          continue;
        }
      }

      frames.add(frame);
    }

    return frames;
  }

  private static VmCallFrame createFrom(VmIsolate isolate, JSONObject object) throws JSONException {
    VmCallFrame frame = new VmCallFrame(isolate);

    frame.functionName = JsonUtils.getString(object, "functionName");
    frame.location = VmLocation.createFrom(isolate, object.getJSONObject("location"));
    frame.locals = VmVariable.createFrom(isolate, object.optJSONArray("locals"), true);

    return frame;
  }

  private String functionName;

  private VmLocation location;

  private List<VmVariable> locals;

  private VmCallFrame(VmIsolate isolate) {
    super(isolate);
  }

  /**
   * Name of the Dart function called on this call frame.
   */
  public String getFunctionName() {
    return functionName;
  }

  public int getLibraryId() {
    return location.getLibraryId();
  }

  public List<VmVariable> getLocals() {
    return locals;
  }

  /**
   * Location in the source code.
   */
  public VmLocation getLocation() {
    return location;
  }

  public VmVariable getThisObject() {
    for (VmVariable variable : locals) {
      if (variable.isThisObject()) {
        return variable;
      }
    }

    return null;
  }

  public boolean isMain() {
    return "main".equals(functionName);
  }

  @Override
  public String toString() {
    return "[" + functionName + "]";
  }
}

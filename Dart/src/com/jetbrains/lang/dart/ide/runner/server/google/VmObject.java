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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A representation of a VM object.
 */
public class VmObject extends VmRef {

  static VmObject createFrom(VmIsolate isolate, JSONObject obj) throws JSONException {
    VmObject vmObject = new VmObject(isolate);

    // { classId : Integer , fields : FieldList }
    vmObject.classId = obj.optInt("classId");
    vmObject.fields = VmVariable.createFrom(isolate, obj.optJSONArray("fields"), false);

    return vmObject;
  }

  private int objectId;

  private int classId;

  private List<VmVariable> fields;

  private VmObject(VmIsolate isolate) {
    super(isolate);
  }

  public int getClassId() {
    return classId;
  }

  public List<VmVariable> getFields() {
    return fields;
  }

  public int getObjectId() {
    return objectId;
  }

  public void setObjectId(int objectId) {
    this.objectId = objectId;
  }
}

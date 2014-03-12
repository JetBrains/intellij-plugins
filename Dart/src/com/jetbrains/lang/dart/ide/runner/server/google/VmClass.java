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
 * A representation of a VM class.
 */
public class VmClass extends VmRef {

  static VmClass createFrom(VmIsolate isolate, JSONObject obj) throws JSONException {
    VmClass clazz = new VmClass(isolate);

    // { name : String , superclassId : Integer , libraryId : Integer , fields : FieldList } 
    clazz.name = obj.optString("name");
    clazz.superclassId = obj.optInt("superclassId");
    clazz.libraryId = obj.optInt("libraryId");
    clazz.fields = VmVariable.createFrom(isolate, obj.optJSONArray("fields"), false);

    return clazz;
  }

  private String name;

  private int classId;

  private int superclassId;

  private int libraryId;

  private List<VmVariable> fields;

  private VmClass(VmIsolate isolate) {
    super(isolate);
  }

  public int getClassId() {
    return classId;
  }

  public List<VmVariable> getFields() {
    return fields;
  }

  public int getLibraryId() {
    return libraryId;
  }

  public String getName() {
    return name;
  }

  public int getSuperclassId() {
    return superclassId;
  }

  public void setClassId(int classId) {
    this.classId = classId;
  }
}

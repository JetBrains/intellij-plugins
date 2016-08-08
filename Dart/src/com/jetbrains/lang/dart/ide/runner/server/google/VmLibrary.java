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
 * The representation of a VM library.
 */
public class VmLibrary {

  static VmLibrary createFrom(VmIsolate isolate, int libraryId, JSONObject obj)
    throws JSONException {
    VmLibrary lib = new VmLibrary(isolate);

    // url
    // imports
    // globals

    lib.libraryId = libraryId;

    // url
    lib.url = obj.optString("url");

    // imports
    lib.importedLibraryIds = new ArrayList<>();

    JSONArray arr = obj.getJSONArray("imports");

    for (int i = 0; i < arr.length(); i++) {
      JSONObject entry = arr.getJSONObject(i);

      lib.importedLibraryIds.add(entry.getInt("libraryId"));
    }

    // globals
    lib.globals = VmVariable.createFrom(isolate, obj.optJSONArray("globals"), false);

    return lib;
  }

  private int libraryId;

  private String url;

  private List<Integer> importedLibraryIds;

  private List<VmVariable> globals;

  private VmIsolate isolate;

  private VmLibrary(VmIsolate isolate) {
    this.isolate = isolate;
  }

  public List<VmVariable> getGlobals() {
    return globals;
  }

  public List<Integer> getImportedLibraryIds() {
    return importedLibraryIds;
  }

  public VmIsolate getIsolate() {
    return isolate;
  }

  public int getLibraryId() {
    return libraryId;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return "[" + libraryId + "," + url + "]";
  }
}

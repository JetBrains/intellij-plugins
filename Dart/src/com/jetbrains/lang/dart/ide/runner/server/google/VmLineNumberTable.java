/*
 * Copyright (c) 2013, the Dart project authors.
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

import java.util.HashMap;
import java.util.Map;

import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.LOG;

/**
 * A VM LineNumberTable object.
 */
public class VmLineNumberTable {

  static VmLineNumberTable createFrom(VmIsolate isolate, int libraryId, String url,
                                      JSONObject object) throws JSONException {
    VmLineNumberTable lineNumberTable = new VmLineNumberTable(libraryId, url);

    // { "id": 2, "result": { "lines": [[1,0,0,1,5,2,9,3,10,4,12], [2, ...

    JSONArray lineInfos = object.getJSONArray("lines");

    for (int i = 0; i < lineInfos.length(); i++) {
      JSONArray lineInfo = lineInfos.getJSONArray(i);

      // Retrieve the line number of the current line.
      Integer lineNumber = lineInfo.getInt(0);

      // Index over the remaining (tokenOffset, charOffset) tuples.
      for (int index = 1; index < lineInfo.length(); index += 2) {
        Integer tokenOffset = lineInfo.getInt(index);

        // We don't use this info currently, so we don't decode it.
        //Integer columnNumber = lineInfo.getInt(index + 1);

        lineNumberTable.lineMap.put(tokenOffset, lineNumber);
      }
    }

    return lineNumberTable;
  }

  Map<Integer, Integer> lineMap = new HashMap<>();

  private int libraryId;

  private String url;

  VmLineNumberTable(int libraryId, String url) {
    this.libraryId = libraryId;
    this.url = url;
  }

  public int getLibraryId() {
    return libraryId;
  }

  public int getLineForLocation(VmLocation location) {
    Integer ret = lineMap.get(location.getTokenOffset());

    if (ret != null) {
      return ret.intValue();
    }

    LOG.warn("no line mapping found for " + location);

    return 0;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return "[lineNumberTable for " + url + "," + lineMap.size() + " mappings]";
  }
}

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

import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A VM location object.
 */
public class VmLocation {

  static VmLocation createFrom(VmIsolate isolate, JSONObject object) throws JSONException {
    if (object == null) {
      return null;
    }

    VmLocation location = new VmLocation(isolate);

    location.libraryId = JsonUtils.getInt(object, "libraryId", -1);
    location.url = VmUtils.vmUrlToEclipse(JsonUtils.getString(object, "url"));
    location.tokenOffset = JsonUtils.getInt(object, "tokenOffset", -1);

    return location;
  }

  private VmIsolate isolate;

  private int libraryId;

  private int tokenOffset;

  private String url;

  VmLocation(VmIsolate isolate) {
    this.isolate = isolate;
  }

  public VmIsolate getIsolate() {
    return isolate;
  }

  public int getLibraryId() {
    return libraryId;
  }

  public int getLineNumber(VmConnection connection) {
    return connection.getLineNumberFromLocation(isolate, this);
  }

  public int getTokenOffset() {
    return tokenOffset;
  }

  public String getUrl() {
    return url;
  }

  @Nullable
  public String getUnescapedUrl() {
    return url == null ? null : URLUtil.unescapePercentSequences(url);
  }

  public JSONObject toJSONObject() throws JSONException {
    JSONObject object = new JSONObject();

    object.put("url", url);
    object.put("tokenOffset", tokenOffset);

    return object;
  }

  @Override
  public String toString() {
    return "[" + url + ", tokenOffset=" + tokenOffset + "]";
  }
}

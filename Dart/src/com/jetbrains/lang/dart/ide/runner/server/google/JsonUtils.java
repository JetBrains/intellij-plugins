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

/**
 * A static utility class for working with JSON objects.
 */
public class JsonUtils {

  public static boolean getBoolean(JSONObject object, String key) throws JSONException {
    if (object.has(key)) {
      return object.getBoolean(key);
    }
    else {
      return false;
    }
  }

  public static int getInt(JSONObject object, String key) throws JSONException {
    return getInt(object, key, 0);
  }

  public static int getInt(JSONObject object, String key, int defaultValue) throws JSONException {
    if (object.has(key)) {
      return object.getInt(key);
    }
    else {
      return defaultValue;
    }
  }

  public static String getString(JSONObject object, String key) throws JSONException {
    if (object.has(key)) {
      return object.getString(key);
    }
    else {
      return null;
    }
  }

  private JsonUtils() {

  }
}

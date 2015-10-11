/*
 * Copyright (c) 2014, the Dart project authors.
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
package com.google.dart.server.utilities.general;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The class {@code JsonUtilities} defines some general utility methods that are useful with the
 * {@link JsonElement}s.
 * 
 * @coverage dart.server.utilities
 */
public class JsonUtilities {

  public static Boolean[] decodeBooleanArray(JsonArray jsonArray) {
    if (jsonArray == null) {
      return new Boolean[0];
    }
    int i = 0;
    Boolean[] booleanArray = new Boolean[jsonArray.size()];
    Iterator<JsonElement> iterator = jsonArray.iterator();
    while (iterator.hasNext()) {
      booleanArray[i] = iterator.next().getAsBoolean();
      i++;
    }
    return booleanArray;
  }

  public static int[] decodeIntArray(JsonArray jsonArray) {
    if (jsonArray == null) {
      return new int[0];
    }
    int i = 0;
    int[] intArray = new int[jsonArray.size()];
    Iterator<JsonElement> iterator = jsonArray.iterator();
    while (iterator.hasNext()) {
      intArray[i] = iterator.next().getAsInt();
      i++;
    }
    return intArray;
  }

  public static Integer[] decodeIntegerArray(JsonArray jsonArray) {
    if (jsonArray == null) {
      return new Integer[0];
    }
    int i = 0;
    Integer[] intArray = new Integer[jsonArray.size()];
    Iterator<JsonElement> iterator = jsonArray.iterator();
    while (iterator.hasNext()) {
      intArray[i] = iterator.next().getAsInt();
      i++;
    }
    return intArray;
  }

  public static List<String> decodeStringList(JsonArray jsonArray) {
    if (jsonArray == null) {
      return StringUtilities.EMPTY_LIST;
    }
    List<String> stringList = new ArrayList<String>(jsonArray.size());
    Iterator<JsonElement> iterator = jsonArray.iterator();
    while (iterator.hasNext()) {
      stringList.add(iterator.next().getAsString());
    }
    return stringList;
  }

}

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
package com.google.dart.server.internal.remote.utilities;

import com.google.common.collect.Maps;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Tests for {@link RequestUtilities}.
 */
public class RequestUtilitiesTest extends TestCase {

  public void test_buildJsonElement_map_nullKey() throws Exception {
    try {
      Map<String, String> map = Maps.newHashMap();
      map.put(null, "bar");
      RequestUtilities.buildJsonElement(map);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

  public void test_buildJsonElement_null() throws Exception {
    try {
      RequestUtilities.buildJsonElement(null);
      fail();
    } catch (IllegalArgumentException e) {
    }
  }

}

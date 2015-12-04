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
package com.google.dart.server.internal.remote;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import junit.framework.TestCase;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;

/**
 * Test for {@link ByteResponseStream}.
 */
public class ByteResposeStreamTest extends TestCase {
  public void test_lastRequestProcessed() throws Exception {
    byte[] bytes = "".getBytes(Charsets.UTF_8);
    ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
    ByteResponseStream responseStream = new ByteResponseStream(byteStream, null);
    responseStream.lastRequestProcessed();
  }

  public void test_take() throws Exception {
    String jsonStringA = "{'id': '0', 'error': 'aaa'}";
    String jsonStringB = "{'id': '1', 'error': 'bbb'}";
    JsonObject jsonObjectA = parseJson(jsonStringA);
    JsonObject jsonObjectB = parseJson(jsonStringB);
    byte[] bytes = (jsonStringA + "\n" + jsonStringB).getBytes(Charsets.UTF_8);
    ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
    ByteResponseStream responseStream = new ByteResponseStream(byteStream, null);
    // take a JsonObject(s)
    assertEquals(jsonObjectA, responseStream.take());
    assertEquals(jsonObjectB, responseStream.take());
    // EOF
    assertNull(responseStream.take());
  }

  public void test_take_debugStream() throws Exception {
    DebugPrintStream debugStream = mock(DebugPrintStream.class);
    byte[] bytes = ("some text\n" + "{}\n").getBytes(Charsets.UTF_8);
    ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
    ByteResponseStream responseStream = new ByteResponseStream(byteStream, debugStream);
    // take a JsonObject
    JsonObject jsonObject = responseStream.take();
    assertTrue(jsonObject.entrySet().isEmpty());
    verify(debugStream, times(2)).println(anyString());
  }

  /**
   * Parses the given {@link String} as a {@link JsonObject}.
   */
  private JsonObject parseJson(String json) {
    return (JsonObject) new JsonParser().parse(json);
  }
}

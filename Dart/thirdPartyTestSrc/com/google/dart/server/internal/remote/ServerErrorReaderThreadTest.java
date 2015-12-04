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

import com.google.common.base.Joiner;
import com.google.dart.server.AnalysisServerListener;
import com.google.dart.server.MockAnalysisServerListener;
import com.google.dart.server.internal.remote.processor.NotificationServerErrorProcessor;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;

public class ServerErrorReaderThreadTest extends TestCase {

  class MockLineReaderStream implements LineReaderStream {
    private LineNumberReader reader;

    public MockLineReaderStream(String text) {
      reader = new LineNumberReader(new StringReader(text));
    }

    @Override
    public String readLine() throws Exception {
      return reader.readLine();
    }

    @Override
    public boolean ready() throws IOException {
      return reader.ready();
    }
  }

  private ByteArrayOutputStream errorStream;
  private int errorCount;
  private String message;
  private String stackTrace;

  public void test_exception1() throws Exception {
    process(
        "Unhandled exception:",
        "Uncaught Error: RangeError: value 90",
        "Stack Trace:",
        "#0      _StringBase.substring (dart:core-patch/string_patch.dart:230)",
        "#1      ... etc ...",
        "",
        "#0      _rootHandleUncaughtError.<anonymous closure> (dart:async/zone.dart:820)",
        "#1      _asyncRunCallbackLoop (dart:async/schedule_microtask.dart:41)",
        "#2      ... etc ...");
    assertEquals(1, errorCount);
    assertTrue(errorStream.size() > 0);
    assertTrue(message.contains("RangeError"));
    assertTrue(stackTrace.contains("dart:core"));
  }

  public void test_exception2() throws Exception {
    process(
        "Unhandled exception:",
        "The null object does not have a method 'accept'.",
        "",
        "NoSuchMethodError: method not found: 'accept'",
        "Receiver: null",
        "Arguments: [Instance of 'Foo']",
        "",
        "#0      Object.noSuchMethod (dart:core-patch/object_patch.dart:45)",
        "#1      ... etc ...");
    assertEquals(1, errorCount);
    assertTrue(errorStream.size() > 0);
    assertTrue(message.contains("NoSuchMethodError"));
    assertTrue(stackTrace.contains("dart:core"));
  }

  public void test_no_exception() throws Exception {
    process("One", "Two", "Three");
    assertEquals(0, errorCount);
  }

  public void test_partial_exception1() throws Exception {
    process("Unhandled exception:");
    assertEquals(1, errorCount);
    assertTrue(errorStream.size() > 0);
    assertEquals(0, message.length());
    assertEquals(0, stackTrace.length());
  }

  public void test_partial_exception2() throws Exception {
    process("Unhandled exception:", "The null object does not have a method 'accept'.");
    assertEquals(1, errorCount);
    assertTrue(errorStream.size() > 0);
    assertTrue(message.contains("null object"));
    assertEquals(0, stackTrace.length());
  }

  private void process(String... lines) throws Exception {
    errorCount = 0;
    String text = Joiner.on('\n').join(lines);
    MockLineReaderStream stream = new MockLineReaderStream(text);
    AnalysisServerListener listener = new MockAnalysisServerListener() {
      @Override
      public void serverError(boolean isFatal, String message, String stackTrace) {
        errorCount++;
        ServerErrorReaderThreadTest.this.message = message;
        ServerErrorReaderThreadTest.this.stackTrace = stackTrace;
      }
    };
    NotificationServerErrorProcessor processor = new NotificationServerErrorProcessor(listener);
    errorStream = new ByteArrayOutputStream();
    new ServerErrorReaderThread(stream, processor, new PrintStream(errorStream)).processStream();
  }
}

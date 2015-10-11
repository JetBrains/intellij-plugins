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

import com.google.dart.server.AnalysisServerListener;
import com.google.dart.server.internal.remote.processor.NotificationServerErrorProcessor;
import com.google.gson.JsonObject;

import java.io.PrintStream;

/**
 * A thread which reads input from the {@link LineReaderStream} error stream and translates
 * unhandled exceptions into server error notifications.
 */
public class ServerErrorReaderThread extends Thread {
  private final LineReaderStream stream;
  private final NotificationServerErrorProcessor processor;
  private final PrintStream syserr;

  public ServerErrorReaderThread(LineReaderStream errorStream, AnalysisServerListener listener) {
    this(errorStream, new NotificationServerErrorProcessor(listener), System.err);
  }

  public ServerErrorReaderThread(LineReaderStream stream,
      NotificationServerErrorProcessor processor, PrintStream errorStream) {
    this.processor = processor;
    this.stream = stream;
    this.syserr = errorStream;
    setDaemon(true);
    setName("ServerErrorReaderThread");
  }

  /**
   * Read and process lines from the stream.
   */
  public void processStream() throws Exception {
    while (true) {
      String line = stream.readLine();
      if (line == null) {
        return;
      }
      syserr.println(line);
      if (!line.startsWith("Unhandled exception:")) {
        continue;
      }

      /*
       * Construct a special response if server has crashed
       * 
       * Unhandled exception:
       * Uncaught Error: RangeError: value 90
       * Stack Trace:
       * #0      _StringBase.substring (dart:core-patch/string_patch.dart:230)
       * #1      ... etc ...
       * 
       * #0      _rootHandleUncaughtError.<anonymous closure> (dart:async/zone.dart:820)
       * #1      _asyncRunCallbackLoop (dart:async/schedule_microtask.dart:41)
       * #2      ... etc ...
       * 
       * -- or -- 
       * 
       * Unhandled exception:
       * The null object does not have a method 'accept'.
       * 
       * NoSuchMethodError: method not found: 'accept'
       * Receiver: null
       * Arguments: [Instance of 'Foo']
       * #0      Object.noSuchMethod (dart:core-patch/object_patch.dart:45)
       * #1      ... etc ...
       */

      // Exception message
      StringBuilder message = new StringBuilder(200);
      while (true) {
        line = readAvailableLine();
        if (line == null) {
          break;
        }
        line = line.trim();
        if (line.startsWith("#")) {
          break;
        }
        if (line.length() > 0) {
          if (message.length() > 0) {
            message.append("\n");
          }
          message.append(line);
        }
      }

      // Stack trace
      StringBuilder stack = new StringBuilder();
      if (line != null) {
        while (true) {
          stack.append(line);
          line = readAvailableLine();
          if (line == null) {
            break;
          }
          stack.append("\n");
        }
      }

      JsonObject response = new JsonObject();
      response.addProperty("event", RemoteAnalysisServerImpl.SERVER_NOTIFICATION_ERROR);
      JsonObject paramsObject = new JsonObject();
      paramsObject.addProperty("isFatal", true);
      paramsObject.addProperty("message", message.toString());
      paramsObject.addProperty("stackTrace", stack.toString());
      response.add("params", paramsObject);
      processor.process(response);
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        processStream();
        return;
      } catch (Exception e) {
        // TODO (jwren) handle error messages
        e.printStackTrace();
      }
    }
  }

  private String readAvailableLine() throws Exception {
    if (!stream.ready()) {
      return null;
    }
    String line = stream.readLine();
    if (line != null) {
      syserr.println(line);
    }
    return line;
  }
}

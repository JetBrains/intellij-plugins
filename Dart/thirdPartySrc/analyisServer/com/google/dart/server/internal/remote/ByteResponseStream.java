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
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An {@link InputStream} based implementation of {@link ResponseStream}. Each line must contain
 * exactly one complete JSON object.
 * 
 * @coverage dart.server.remote
 */
public class ByteResponseStream implements ResponseStream {
  private class LinesReaderThread extends Thread {
    public LinesReaderThread() {
      setName("ByteResponseStream.LinesReaderThread");
      setDaemon(true);
    }

    @Override
    public void run() {
      while (true) {
        String line;
        try {
          line = reader.readLine();
        } catch (IOException e) {
          line = null;
        }
        // check for EOF
        if (line == null) {
          lineQueue.add(EOF_LINE);
          return;
        }
        // debug output
        if (debugStream != null) {
          debugStream.println(System.currentTimeMillis() + " <= " + line);
        }
        // ignore non-JSON (debug) lines
        if (!line.startsWith("{")) {
          continue;
        }
        // add a JSON line
        lineQueue.add(line);
      }
    }
  }

  private static String EOF_LINE = "EOF line";

  /**
   * The {@link BufferedReader} to read JSON strings from.
   */
  private final BufferedReader reader;

  /**
   * The {@link DebugPrintStream} to print all lines to.
   */
  private final DebugPrintStream debugStream;

  /**
   * The queue of lines.
   */
  private final BlockingQueue<String> lineQueue = new LinkedBlockingQueue<String>();

  /**
   * Initializes a newly created response stream.
   * 
   * @param stream the byte stream to read JSON strings from
   * @param debugStream the {@link PrintStream} to print all lines to, may be {@code null}
   */
  public ByteResponseStream(InputStream stream, DebugPrintStream debugStream) {
    reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
    this.debugStream = debugStream;
    new LinesReaderThread().start();
  }

  @Override
  public void lastRequestProcessed() {
  }

  @Override
  public JsonObject take() throws Exception {
    String line = lineQueue.take();
    if (line == EOF_LINE) {
      lineQueue.add(line);
      return null;
    }
    try {
      return (JsonObject) new JsonParser().parse(line);
    } catch (JsonSyntaxException e) {
      // Include the line in the message so that we can better diagnose the problem
      throw new JsonSyntaxException("Parse server message failed: " + line, e);
    }
  }
}

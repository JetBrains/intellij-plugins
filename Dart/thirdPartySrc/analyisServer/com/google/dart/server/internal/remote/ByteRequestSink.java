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
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An {@link OutputStream} based implementation of {@link RequestSink}.
 * 
 * @coverage dart.server.remote
 */
public class ByteRequestSink implements RequestSink {
  private class LinesWriterThread extends Thread {
    public LinesWriterThread() {
      setName("ByteRequestSink.LinesWriterThread");
      setDaemon(true);
    }

    @Override
    public void run() {
      while (true) {
        try {
          String line = lineQueue.take();
          writer.println(line);
          writer.flush();
        } catch (InterruptedException e) {
        }
      }
    }
  }

  /**
   * The {@link PrintWriter} to print JSON strings to.
   */
  private final PrintWriter writer;

  /**
   * The {@link PrintStream} to print all lines to.
   */
  private DebugPrintStream debugStream;
  /**
   * The queue of lines.
   */
  private final BlockingQueue<String> lineQueue = new LinkedBlockingQueue<String>();

  /**
   * Initializes a newly created request sink.
   * 
   * @param stream the byte stream to write JSON strings to
   * @param debugStream the {@link PrintStream} to print all lines to, may be {@code null}
   */
  public ByteRequestSink(OutputStream stream, DebugPrintStream debugStream) {
    writer = new PrintWriter(new OutputStreamWriter(stream, Charsets.UTF_8));
    this.debugStream = debugStream;
    new LinesWriterThread().start();
  }

  @Override
  public void add(JsonObject request) {
    String text = request.toString();
    if (debugStream != null) {
      if (!text.contains("server.getVersion")) {
        debugStream.println(System.currentTimeMillis() + " => " + text);
      }
    }
    lineQueue.add(text);
  }

  @Override
  public void close() {
    writer.close();
  }

  public void waitForFlush() {
    while (true) {
      if (lineQueue.isEmpty()) {
        return;
      }
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
    }
  }
}

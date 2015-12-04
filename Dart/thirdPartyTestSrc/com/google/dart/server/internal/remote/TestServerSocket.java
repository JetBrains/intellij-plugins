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

import com.google.dart.server.AnalysisServerSocket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestServerSocket implements AnalysisServerSocket {
  protected TestRequestSink requestSink = new TestRequestSink();
  protected TestResponseStream responseStream = new TestResponseStream();
  private boolean stopped = false;
  private boolean started = false;
  private CountDownLatch startLatch;

  @Override
  public ByteLineReaderStream getErrorStream() {
    return null;
  }

  @Override
  public TestRequestSink getRequestSink() {
    return requestSink;
  }

  @Override
  public TestResponseStream getResponseStream() {
    return responseStream;
  }

  @Override
  public boolean isOpen() {
    return started;
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isStopped() {
    return stopped;
  }

  @Override
  public void start() throws Exception {
    started = true;
    if (startLatch != null) {
      startLatch.countDown();
    }
  }

  @Override
  public void stop() {
    stopped = true;
  }

  public boolean waitForRestart(long milliseconds) {
    started = false;
    stopped = false;
    startLatch = new CountDownLatch(1);
    try {
      return startLatch.await(milliseconds, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      return false;
    }
  }
}

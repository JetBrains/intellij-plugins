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
package com.google.dart.server;

import com.google.dart.server.generated.AnalysisServer;
import com.google.dart.server.internal.remote.ByteLineReaderStream;
import com.google.dart.server.internal.remote.RequestSink;
import com.google.dart.server.internal.remote.ResponseStream;

/**
 * A socket over used by {@link AnalysisServer} to communicate with the remote server process.
 * 
 * @coverage dart.server
 */
public interface AnalysisServerSocket {

  /**
   * Return the error stream.
   */
  ByteLineReaderStream getErrorStream();

  /**
   * Return the request sink.
   */
  RequestSink getRequestSink();

  /**
   * Return the response stream.
   */
  ResponseStream getResponseStream();

  /**
   * Return {@code true} if the socket is open.
   */
  boolean isOpen();

  /**
   * Start the remote server and initialize request sink and response stream.
   */
  void start() throws Exception;

  /**
   * Stop the remote server.
   */
  void stop();
}

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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.dart.server.AnalysisServerSocket;
import com.google.dart.server.utilities.logging.Logging;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A remote server socket over standard input and output.
 * 
 * @coverage dart.server.remote
 */
public class StdioServerSocket implements AnalysisServerSocket {
  private final String runtimePath;
  private final List<String> vmArguments;

  private final String analysisServerPath;
  private final List<String> serverArguments;
  private final DebugPrintStream debugStream;

  private RequestSink requestSink;
  private ResponseStream responseStream;
  private ByteLineReaderStream errorStream;
  private Process process;

  /**
   * The identifier used to identify this client to the server, or {@code null} if the client does
   * not choose to identify itself.
   */
  private String clientId;

  /**
   * The identifier used to identify this client to the server, or {@code null} if the client does
   * not choose to identify itself.
   */
  private String clientVersion;

  public StdioServerSocket(String runtimePath, List<String> additionalVmArguments,
      String analysisServerPath, List<String> additionalServerArguments,
      DebugPrintStream debugStream) {
    this.runtimePath = runtimePath;
    this.vmArguments = defaultIfNull(additionalVmArguments, Lists.<String> newArrayList());
    this.analysisServerPath = analysisServerPath;
    this.serverArguments = defaultIfNull(additionalServerArguments, Lists.<String> newArrayList());
    this.debugStream = debugStream;
  }

  @Override
  public ByteLineReaderStream getErrorStream() {
    Preconditions.checkNotNull(errorStream, "Server is not started.");
    return errorStream;
  }

  @Override
  public RequestSink getRequestSink() {
    Preconditions.checkNotNull(requestSink, "Server is not started.");
    return requestSink;
  }

  @Override
  public ResponseStream getResponseStream() {
    Preconditions.checkNotNull(responseStream, "Server is not started.");
    return responseStream;
  }

  @Override
  public boolean isOpen() {
    try {
      if (process != null) {
        process.exitValue();
      }
      return false;
    } catch (IllegalThreadStateException ex) {
      return true;
    }
  }

  /**
   * Set the identifier used to identify this client to the server to the given identifier. The
   * identifier must be set before the server has been started.
   */
  public void setClientId(String id) {
    clientId = id;
  }

  /**
   * Set the identifier used to identify this client version to the server to the given identifier.
   * The identifier must be set before the server has been started.
   */
  public void setClientVersion(String version) {
    clientVersion = version;
  }

  @Override
  public void start() throws Exception {
    String[] arguments = computeProcessArguments();
    if (debugStream != null) {
      StringBuilder builder = new StringBuilder();
      builder.append("  ");
      int count = arguments.length;
      for (int i = 0; i < count; i++) {
        if (i > 0) {
          builder.append(' ');
        }
        builder.append(arguments[i]);
      }
      debugStream.println(System.currentTimeMillis() + " started analysis server:");
      debugStream.println(builder.toString());
    }
    ProcessBuilder processBuilder = new ProcessBuilder(arguments);
    process = processBuilder.start();
    requestSink = new ByteRequestSink(process.getOutputStream(), debugStream);
    responseStream = new ByteResponseStream(process.getInputStream(), debugStream);
    errorStream = new ByteLineReaderStream(process.getErrorStream());
  }

  /**
   * Wait up to 5 seconds for process to gracefully exit, then forcibly terminate the process if it
   * is still running.
   */
  @Override
  public void stop() {
    if (process == null) {
      return;
    }
    final Process processToStop = process;
    process = null;
    long endTime = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < endTime) {
      try {
        int exit = processToStop.exitValue();
        if (exit != 0) {
          Logging.getLogger().logInformation(
              "Non-zero exit code: " + exit + " for\n   " + analysisServerPath);
        }
        return;
      } catch (IllegalThreadStateException e) {
        //$FALL-THROUGH$
      }
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        //$FALL-THROUGH$
      }
    }
    processToStop.destroy();
    Logging.getLogger().logInformation("Terminated " + analysisServerPath);
  }

  /**
   * Compute and return the command-line arguments used to start the analysis server process.
   * 
   * @return the command-line arguments that were computed
   */
  private String[] computeProcessArguments() {
    List<String> args = new ArrayList<String>();
    //
    // The path to the VM.
    //
    args.add(runtimePath);
    //
    // VM arguments.
    //
    args.addAll(vmArguments);
    //
    // The analysis server path.
    //
    args.add(analysisServerPath);
    //
    // Analysis server arguments.
    //
    if (clientId != null) {
      args.add("--client-id=" + clientId);
    }
    if (clientVersion != null) {
      args.add("--client-version=" + clientVersion);
    }
    args.addAll(serverArguments);
    // Done.
    return args.toArray(new String[args.size()]);
  }
}

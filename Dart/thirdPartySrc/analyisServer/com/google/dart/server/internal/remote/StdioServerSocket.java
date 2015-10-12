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
import com.google.dart.server.AnalysisServerSocket;
import com.google.dart.server.utilities.general.StringUtilities;
import com.google.dart.server.utilities.logging.Logging;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * A remote server socket over standard input and output.
 * 
 * @coverage dart.server.remote
 */
public class StdioServerSocket implements AnalysisServerSocket {
  /**
   * Find and return an unused server socket port.
   */
  public static int findUnusedPort() {
    try {
      ServerSocket ss = new ServerSocket(0);
      int port = ss.getLocalPort();
      ss.close();
      return port;
    } catch (IOException ioe) {
      //$FALL-THROUGH$
    }
    return -1;
  }

  private final String runtimePath;
  private final String analysisServerPath;
  private final DebugPrintStream debugStream;
  private final boolean debugRemoteProcess;
  private final boolean profileRemoteProcess;
  private int httpPort;
  private RequestSink requestSink;
  private ResponseStream responseStream;
  private ByteLineReaderStream errorStream;
  private Process process;
  private final String[] additionalProgramArguments;

  /**
   * If non-null, the package root that should be provided to Dart when running the analysis server.
   */
  private final String packageRoot;

  /**
   * Boolean used to have the {@code --no-error-notification} which disables all error notifications
   * from the server.
   * <p>
   * This should be {@code null} if the snapshot path is passed as the server path.
   */
  private final boolean noErrorNotification;

  /**
   * An option of the ways files can be read from disk, some clients normalize end of line
   * characters which would make the file offset and range information incorrect.
   */
  private final FileReadMode fileReadMode;

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

  public StdioServerSocket(String runtimePath, String analysisServerPath,
      DebugPrintStream debugStream, boolean debugRemoteProcess, boolean profileRemoteProcess,
      int httpPort) {
    this(
        runtimePath,
        analysisServerPath,
        StringUtilities.EMPTY,
        debugStream,
        StringUtilities.EMPTY_ARRAY,
        debugRemoteProcess,
        profileRemoteProcess,
        httpPort,
        false,
        FileReadMode.AS_IS);
  }

  public StdioServerSocket(String runtimePath, String analysisServerPath, String packageRoot,
      DebugPrintStream debugStream, String[] additionalProgramArguments,
      boolean debugRemoteProcess, boolean profileRemoteProcess, int httpPort,
      boolean noErrorNotification, FileReadMode fileReadMode) {
    this.runtimePath = runtimePath;
    this.analysisServerPath = analysisServerPath;
    this.packageRoot = packageRoot;
    this.debugStream = debugStream;
    this.additionalProgramArguments = additionalProgramArguments;
    this.debugRemoteProcess = debugRemoteProcess;
    this.profileRemoteProcess = profileRemoteProcess;
    this.httpPort = httpPort;
    this.noErrorNotification = noErrorNotification;
    this.fileReadMode = fileReadMode;
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
    int debugPort = findUnusedPort();
    String[] arguments = computeProcessArguments(debugPort);
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
    if (debugRemoteProcess) {
      Logging.getLogger().logInformation("Analysis server debug port " + debugPort);
    }
    if (httpPort != 0) {
      Logging.getLogger().logInformation("Analysis server http port " + httpPort);
    }
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
   * @param debugPort the port that the VM should use for debug connections
   * @return the command-line arguments that were computed
   */
  private String[] computeProcessArguments(int debugPort) {
    List<String> args = new ArrayList<String>();
    //
    // The path to the VM.
    //
    args.add(runtimePath);
    //
    // VM arguments.
    //
    if (packageRoot != null) {
      args.add("--package-root=" + packageRoot);
    }
    if (debugRemoteProcess) {
      args.add("--debug:" + debugPort);
    }
    if (profileRemoteProcess) {
      args.add("--observe");
      args.add("--pause-isolates-on-exit");
      args.add("--code_comments");
      args.add("--collect-code=false");
    }
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
    if (httpPort != 0) {
      args.add("--port=" + httpPort);
    }
    if (noErrorNotification) {
      args.add("--no-error-notification");
    }
    if (fileReadMode == FileReadMode.NORMALIZE_EOL_ALWAYS) {
      args.add("--file-read-mode=normalize-eol-always");
    }
    for (String arg : additionalProgramArguments) {
      args.add(arg);
    }
    return args.toArray(new String[args.size()]);
  }
}

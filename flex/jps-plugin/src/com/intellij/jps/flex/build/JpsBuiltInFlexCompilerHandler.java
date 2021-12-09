// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.jps.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.sdk.JpsFlexSdkType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.service.SharedThreadPool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class JpsBuiltInFlexCompilerHandler {
  private static final Logger LOG = Logger.getInstance(JpsBuiltInFlexCompilerHandler.class.getName());
  private static final String CONNECTION_SUCCESSFUL = "Connection successful";
  public static final String COMPILATION_FINISHED = "Compilation finished";

  private final JpsProject myProject;

  private String mySdkHome;

  private ServerSocket myServerSocket;
  private DataInputStream myDataInputStream;
  private DataOutputStream myDataOutputStream;

  private int commandNumber = 1;
  private final Map<String, Listener> myActiveListeners = new HashMap<>();

  public interface Listener {
    void textAvailable(String text);

    void compilationFinished();
  }

  JpsBuiltInFlexCompilerHandler(final JpsProject project) {
    myProject = project;
  }

  public synchronized boolean canBeUsedForSdk(final String sdkHome) {
    return mySdkHome == null || mySdkHome.equals(sdkHome);
  }

  public synchronized void startCompilerIfNeeded(final JpsSdk<?> sdk,
                                                 final CompileContext context,
                                                 final String compilerName) throws IOException {
    if (!Objects.equals(sdk.getHomePath(), mySdkHome)) {
      stopCompilerProcess();
    }

    if (myServerSocket == null) {
      try {
        //context.processMessage(new ProgressMessage("Starting Flex compiler"));
        myServerSocket = new ServerSocket(0);
        myServerSocket.setSoTimeout(10000);
        final int port = myServerSocket.getLocalPort();

        startCompilerProcess(sdk, port, context, compilerName);

        final Socket socket = myServerSocket.accept();
        myDataInputStream = new DataInputStream(socket.getInputStream());
        myDataOutputStream = new DataOutputStream(socket.getOutputStream());
        mySdkHome = sdk.getHomePath();
        scheduleInputReading();
      }
      catch (IOException e) {
        stopCompilerProcess();
        throw e;
      }
    }
  }

  private void startCompilerProcess(final JpsSdk<?> sdk,
                                    final int port,
                                    final CompileContext context,
                                    final String compilerName) throws IOException {
    final StringBuilder classpath = new StringBuilder();

    classpath.append(FlexCommonUtils.getPathToBundledJar("idea-flex-compiler-fix.jar"));
    classpath.append(File.pathSeparatorChar);
    classpath.append(FlexCommonUtils.getPathToBundledJar("flex-compiler.jar"));

    if (sdk.getSdkType() == JpsFlexSdkType.INSTANCE) {
      classpath.append(File.pathSeparator).append(FileUtil.toSystemDependentName(sdk.getHomePath() + "/lib/flex-compiler-oem.jar"));
    }

    final List<String> commandLine =
      FlexCommonUtils.getCommandLineForSdkTool(myProject, sdk, classpath.toString(), "com.intellij.flex.compiler.FlexCompiler");
    commandLine.add(String.valueOf(port));

    final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
    processBuilder.redirectErrorStream(true);
    processBuilder.directory(new File(FlexCommonUtils.getFlexCompilerWorkDirPath(myProject)));

    final String plainCommand = StringUtil.join(processBuilder.command(), s -> s.contains(" ") ? "\"" + s + "\"" : s, " ");
    context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.INFO, "Starting Flex compiler:\n" + plainCommand));

    final Process process = processBuilder.start();
    readInputStreamUntilConnected(process, context, compilerName);
  }

  private void readInputStreamUntilConnected(final Process process, final CompileContext context, final String compilerName) {
    SharedThreadPool.getInstance().execute(() -> {
      final InputStreamReader reader = FlexCommonUtils.createInputStreamReader(process.getInputStream());

      try {
        char[] buf = new char[1024];
        int read;
        while ((read = reader.read(buf, 0, buf.length)) >= 0) {
          final String output = new String(buf, 0, read);
          if (output.startsWith(CONNECTION_SUCCESSFUL)) {
            break;
          }
          else {
            closeSocket();
            context.processMessage(new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, output));
          }
        }
      }
      catch (IOException e) {
        closeSocket();
        context.processMessage(
          new CompilerMessage(compilerName, BuildMessage.Kind.ERROR, "Failed to start Flex compiler: " + e.toString()));
      }
      finally {
        try {
          reader.close();
        }
        catch (IOException e) {/*ignore*/}
      }
    });
  }

  private void scheduleInputReading() {
    SharedThreadPool.getInstance().execute(() -> {
      final StringBuilder buffer = new StringBuilder();
      while (true) {
        final DataInputStream dataInputStream = myDataInputStream;
        if (dataInputStream != null) {
          try {
            buffer.append(dataInputStream.readUTF());

            int index;
            while ((index = buffer.indexOf("\n")) > -1) {
              final String line = buffer.substring(0, index);
              buffer.delete(0, index + 1);
              handleInputLine(line);
            }
          }
          catch (IOException e) {
            if (dataInputStream == myDataInputStream) {
              stopCompilerProcess();
            }
            break;
          }
        }
        else {
          break;
        }
      }
    });
  }

  private synchronized void handleInputLine(final String line) {
    LOG.debug("RECEIVED: [" + line + "]");

    final int colonPos = line.indexOf(":");
    if (colonPos <= 0) {
      LOG.error("Incorrect command: [" + line + "]");
      return;
    }

    final String prefix = line.substring(0, colonPos + 1);
    final Listener listener = myActiveListeners.get(prefix);
    if (listener == null) {
      LOG.warn("No active listener for input line: [" + line + "]");  // could be message from cancelled compilation
    }
    else {
      final String text = line.substring(colonPos + 1);
      if (text.startsWith(COMPILATION_FINISHED)) {
        listener.compilationFinished();
        myActiveListeners.remove(prefix);
      }
      else {
        listener.textAvailable(text);
      }
    }
  }

  public synchronized void sendCompilationCommand(final String command, final Listener listener) {
    if (myDataOutputStream == null) {
      listener.textAvailable("Error: Compiler process is not started.");
      listener.compilationFinished();
      return;
    }

    try {
      final String prefix = (commandNumber++) + ":";
      final String commandToSend = prefix + command + "\n";
      LOG.debug("SENDING: [" + commandToSend + "]");
      myDataOutputStream.writeUTF(commandToSend);
      myActiveListeners.put(prefix, listener);
    }
    catch (IOException e) {
      listener.textAvailable("Error: Can't start compilation: " + e.toString());
      listener.compilationFinished();
    }
  }

  private synchronized void cancelAllCompilations(final boolean reportError) {
    for (final Listener listener : myActiveListeners.values()) {
      if (reportError) {
        listener.textAvailable("Error: Compilation terminated");
      }
      listener.compilationFinished();
    }
    myActiveListeners.clear();
  }

  public synchronized void stopCompilerProcess() {
    cancelAllCompilations(true);
    closeSocket();
  }

  private synchronized void closeSocket() {
    // compiler process exits when socket closes, so it's enough just to close streams

    if (myDataInputStream != null) {
      try {
        myDataInputStream.close();
      }
      catch (IOException ignored) {/**/}
    }

    if (myDataOutputStream != null) {
      try {
        myDataOutputStream.close();
      }
      catch (IOException ignored) {/**/}
    }

    if (myServerSocket != null) {
      try {
        myServerSocket.close();
      }
      catch (IOException ignored) {/**/}
    }

    myServerSocket = null;
    myDataInputStream = null;
    myDataOutputStream = null;
  }

  public synchronized void removeListener(final Listener listener) {
    String toRemove = null;
    for (final Map.Entry<String, Listener> entry : myActiveListeners.entrySet()) {
      if (entry.getValue() == listener) {
        toRemove = entry.getKey();
        break;
      }
    }

    if (toRemove != null) {
      myActiveListeners.remove(toRemove);
    }
  }

  public synchronized int getActiveCompilationsNumber() {
    return myActiveListeners.size();
  }
}


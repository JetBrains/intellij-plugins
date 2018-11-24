package com.intellij.lang.javascript.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class BuiltInFlexCompilerHandler {

  private static final Logger LOG = Logger.getInstance(BuiltInFlexCompilerHandler.class.getName());
  private static final String CONNECTION_SUCCESSFUL = "Connection successful";
  public static final String COMPILATION_FINISHED = "Compilation finished";

  private final Project myProject;

  private String mySdkHome;
  private ServerSocket myServerSocket;
  private DataInputStream myDataInputStream;
  private DataOutputStream myDataOutputStream;

  private int commandNumber = 1;
  private Map<String, Listener> myActiveListeners = new THashMap<>();

  public BuiltInFlexCompilerHandler(final Project project) {
    myProject = project;
  }

  public interface Listener {
    void textAvailable(String text);

    void compilationFinished();
  }

  public synchronized void startCompilerIfNeeded(final @NotNull Sdk sdk, final CompileContext context) throws IOException {
    if (!Comparing.equal(sdk.getHomePath(), mySdkHome)) {
      stopCompilerProcess();
    }

    if (myServerSocket == null) {
      try {
        context.getProgressIndicator().setText("Starting Flex compiler");
        myServerSocket = new ServerSocket(0);
        myServerSocket.setSoTimeout(10000);
        final int port = myServerSocket.getLocalPort();

        startCompilerProcess(sdk, port, context);

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

  private void startCompilerProcess(final Sdk sdk, final int port, final CompileContext context) throws IOException {
    final StringBuilder classpath = new StringBuilder();

    classpath.append(FlexCommonUtils.getPathToBundledJar("idea-flex-compiler-fix.jar"));
    classpath.append(File.pathSeparatorChar);
    classpath.append(FlexCommonUtils.getPathToBundledJar("flex-compiler.jar"));

    if (sdk.getSdkType() == FlexSdkType2.getInstance()) {
      classpath.append(File.pathSeparator).append(FileUtil.toSystemDependentName(sdk.getHomePath() + "/lib/flex-compiler-oem.jar"));
    }

    final List<String> commandLine =
      FlexSdkUtils.getCommandLineForSdkTool(myProject, sdk, classpath.toString(), "com.intellij.flex.compiler.FlexCompiler", null);
    commandLine.add(String.valueOf(port));

    final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
    processBuilder.redirectErrorStream(true);
    processBuilder.directory(new File(FlexUtils.getFlexCompilerWorkDirPath(myProject, null)));

    final String plainCommand = StringUtil.join(processBuilder.command(), s -> s.contains(" ") ? "\"" + s + "\"" : s, " ");
    context.addMessage(CompilerMessageCategory.INFORMATION, "Starting Flex compiler:\n" + plainCommand, null, -1, -1);

    final Process process = processBuilder.start();
    readInputStreamUntilConnected(process, context);
  }

  private void readInputStreamUntilConnected(final Process process, final CompileContext context) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
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
            context.addMessage(CompilerMessageCategory.ERROR, output, null, -1, -1);
          }
        }
      }
      catch (IOException e) {
        closeSocket();
        context.addMessage(CompilerMessageCategory.ERROR, "Failed to start Flex compiler: " + e.toString(), null, -1, -1);
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
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
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
      final String prefix = String.valueOf(commandNumber++) + ":";
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

  public void stopCompilerProcess() {
    final Runnable runnable = () -> {
      cancelAllCompilations(true);
      closeSocket();
    };

    final Application application = ApplicationManager.getApplication();
    if (application.isDispatchThread()) {
      application.executeOnPooledThread(runnable);
    }
    else {
      runnable.run();
    }
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

package com.intellij.flex.compiler;

import com.intellij.flex.compiler.flex3.Flex3Handler;
import com.intellij.flex.compiler.flex4.Flex4Handler;
import flex2.tools.VersionInfo;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class FlexCompiler implements MessageSender {

  private static final String CONNECTION_SUCCESSFUL = "Connection successful";

  private static final String FINISH_COMMAND = "Finish";
  public static final String CANCEL_COMMAND = "Cancel";
  public static final String COMPILATION_FINISHED = "Compilation finished";

  public static String SDK_MAJOR_VERSION = VersionInfo.FLEX_MAJOR_VERSION;
  public static String SDK_MINOR_VERSION = VersionInfo.FLEX_MINOR_VERSION;
  public static String SDK_REVISION_VERSION = VersionInfo.FLEX_NANO_VERSION;

  private DataInputStream myDataInputStream;
  private DataOutputStream myDataOutputStream;

  private void openSocket(int port) throws IOException {
    final int maxAttempts = 10;
    Socket socket;
    for (int i = 0; i < maxAttempts; i++) {
      try {
        socket = new Socket(InetAddress.getLocalHost(), port);
        myDataInputStream = new DataInputStream(socket.getInputStream());
        myDataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println(CONNECTION_SUCCESSFUL);
        break;
      }
      catch (IOException e) {
        if (i == maxAttempts - 1) {
          throw e;
        } else {
          try {
            Thread.sleep(100);
          }
          catch (InterruptedException ignore2) {/**/}
        }
      }
    }
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Missing port parameter");
    }
    final int port;
    try {
      port = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      System.out.println("Incorrect port parameter");
      return;
    }

    try {
      final FlexCompiler flexCompiler = new FlexCompiler();
      flexCompiler.openSocket(port);
      flexCompiler.processInput();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public synchronized void sendMessage(final String message) {
    try {
      //System.out.println("OUT: [" + message + "]");
      myDataOutputStream.writeUTF(message.replace('\n', ' ').replace('\r', ' ').trim() + "\n");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void processInput() throws IOException {
    final StringBuilder buffer = new StringBuilder();
    while (true) {
      final String line = myDataInputStream.readUTF();
      //System.out.println("IN: [" + line + "]");
      buffer.append(line);
      final boolean finish = processCommands(buffer);
      if (finish) {
        return;
      }
    }
  }

  private boolean processCommands(final StringBuilder buffer) {
    int index;
    while ((index = buffer.indexOf("\n")) > -1) {
      final String command = buffer.substring(0, index);
      buffer.delete(0, index + 1);

      if (CANCEL_COMMAND.equals(command)) {
        cancelAllCompilations();
      } else if (FINISH_COMMAND.equals(command)) {
        exit();
        return true;
      } else {
        startCompilationThread(command);
      }
    }

    return false;
  }

  private void startCompilationThread(final String command) {
    final int colonPos = command.indexOf(":");
    if (colonPos <= 0) {
      sendMessage("Error: Incorrect command: [" + command + "]");
      sendMessage(COMPILATION_FINISHED);
      return;
    }

    final String commandNumberStr = command.substring(0, colonPos);
    try {
      Integer.parseInt(commandNumberStr);
    } catch (NumberFormatException e) {
      sendMessage("Error: Incorrect command number: [" + commandNumberStr + "]");
      sendMessage(COMPILATION_FINISHED);
      return;
    }

    final String compilationCommand = command.substring(colonPos + 1);

    final boolean isSwf = compilationCommand.startsWith("mxmlc ");
    if (!isSwf && !compilationCommand.startsWith("compc")) {
      sendMessage("Error: Incorrect compilation command: [" + compilationCommand + "]");
      sendMessage(COMPILATION_FINISHED);
      return;
    }

    final String logMessagePrefix = commandNumberStr + ":";
    final OutputLogger logger = new OutputLogger(this, logMessagePrefix);
    final SdkSpecificHandler sdkSpecificHandler = getSdkSpecificHandler();
    if (sdkSpecificHandler == null) {
      logger.log(
        "Error: Flex SDK " + SDK_MAJOR_VERSION + '.' + SDK_MINOR_VERSION + '.' + SDK_REVISION_VERSION
          + " is not supported by built-in compiler shell. Please change compiler at File | Settings | Compiler | Flex Compiler.");
      logger.log(COMPILATION_FINISHED);
      return;
    }

    final CompilationThread compilationThread =
      new CompilationThread(isSwf, sdkSpecificHandler, getParams(compilationCommand), logger);
    compilationThread.setPriority(Thread.MAX_PRIORITY);
    compilationThread.setDaemon(true);
    compilationThread.start();
  }

  private SdkSpecificHandler getSdkSpecificHandler() {
    if ("3".equals(SDK_MAJOR_VERSION)) {
      return new Flex3Handler();
    }
    else if ("4".equals(SDK_MAJOR_VERSION)) {
      return new Flex4Handler();
    }

    return null;
  }

  private static String[] getParams(final String command) {
    final CommandLineArgumentsTokenizer tokenizer = new CommandLineArgumentsTokenizer(command);
    tokenizer.nextToken(); // mxmlc or compc

    final String[] params = new String[tokenizer.countTokens()];

    int i = 0;
    while (tokenizer.hasMoreTokens()) {
      params[i++] = tokenizer.nextToken();
    }
    return params;
  }

  private void exit() {
    cancelAllCompilations();
    try {
      myDataInputStream.close();
    } catch (IOException ignored) {
    }
  }

  private void cancelAllCompilations() {
    // todo implement
  }
}

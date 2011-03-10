package com.intellij.flex.compiler;

import flex2.tools.oem.Logger;
import flex2.tools.oem.Message;

import java.io.File;
import java.io.IOException;

public class OutputLogger extends LoggerAdapter implements Logger {
  private final MessageSender myMessageSender;
  private final String myLogMessagePrefix;

  // see SDKFilesResolver
  private static final String IGNORED_MESSAGE_START;
  private static final String IGNORED_MESSAGE_END = ".xml";

  static {
    String tmp = System.getProperty("java.io.tmpdir");
    try {
      tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + "fake-config").getCanonicalPath();
    }
    catch (IOException ignored) {/**/}

    IGNORED_MESSAGE_START = "Loading configuration file " + tmp ;
  }

  public OutputLogger(final MessageSender messageSender, final String logMessagePrefix) {
    myMessageSender = messageSender;
    myLogMessagePrefix = logMessagePrefix;
  }

  public void log(final String message) {
    /*
    if (message.startsWith(IGNORED_MESSAGE_START) && message.endsWith(IGNORED_MESSAGE_END)) {
      // do not tell anything about our fake config file. See SdkFilesResolver class
      return;
    }
    */

    myMessageSender.sendMessage(myLogMessagePrefix + message);
  }

  public void log(final Message msg, final int errorCode, final String source) {
    log(msg.getLevel(), msg.getPath(), msg.getLine(), msg.getColumn(), msg.toString());
  }

  //                                        path         line                    col          level       message
  //Pattern errorPattern = Pattern.compile("(.*?)(?:\\((-?\\d+)\\))?: ?(?:col: (-?\\d+))? (Warning|Error): (.*)");
  public void log(final String level, final String path, final int line, final int column, final String message) {
    final StringBuilder builder = new StringBuilder();
    if (isNotEmpty(path)) builder.append(path);
    if (line >= 0) builder.append('(').append(line).append(')');
    if (builder.length() > 0) builder.append(": ");
    if (column >= 0) builder.append("col: ").append(column).append(" ");

    if (WARNING_LEVEL.equalsIgnoreCase(level)) {
      builder.append(WARNING_LEVEL).append(": ");
    }
    else if (ERROR_LEVEL.equalsIgnoreCase(level)) {
      builder.append(ERROR_LEVEL).append(": ");
    }

    if (isNotEmpty(message)) builder.append(message);
    log(builder.toString());
  }

  private static boolean isNotEmpty(final String s) {
    return s != null && s.trim().length() >= 0;
  }
}

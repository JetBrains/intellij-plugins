package com.intellij.javascript.karma.execution;

import com.intellij.javascript.karma.util.CommandInputStream;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * @author Sergey Simonchik
 */
public class KarmaTestRunnerProcess extends Process {

  private final Process myProcess;
  private final CommandInputStream myFilteringInputStream;
  private final OutputStream myOutputStream;

  public KarmaTestRunnerProcess(@NotNull Process process) {
    myProcess = process;
    InputStream inputStream = process.getInputStream();
    myFilteringInputStream = new CommandInputStream(inputStream, CharsetToolkit.UTF8_CHARSET);
    myOutputStream = process.getOutputStream();
  }

  @Override
  public OutputStream getOutputStream() {
    return myOutputStream;
  }

  @Override
  public CommandInputStream getInputStream() {
    return myFilteringInputStream;
  }

  @Override
  public InputStream getErrorStream() {
    return myProcess.getErrorStream();
  }

  @Override
  public int waitFor() throws InterruptedException {
    return myProcess.waitFor();
  }

  @Override
  public int exitValue() {
    return myProcess.exitValue();
  }

  @Override
  public void destroy() {
    myProcess.destroy();
  }
}

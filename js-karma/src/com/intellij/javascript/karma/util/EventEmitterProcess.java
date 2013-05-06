package com.intellij.javascript.karma.util;

import com.intellij.openapi.vfs.CharsetToolkit;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * @author Sergey Simonchik
 */
public class EventEmitterProcess extends Process {

  private final Process myProcess;
  private final EventEmitterInputStream myFilteringInputStream;

  public EventEmitterProcess(@NotNull Process process) {
    myProcess = process;
    InputStream inputStream = process.getInputStream();
    //noinspection IOResourceOpenedButNotSafelyClosed
    myFilteringInputStream = new EventEmitterInputStream(inputStream, CharsetToolkit.UTF8_CHARSET);
  }

  @Override
  public OutputStream getOutputStream() {
    return myProcess.getOutputStream();
  }

  @Override
  public EventEmitterInputStream getInputStream() {
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

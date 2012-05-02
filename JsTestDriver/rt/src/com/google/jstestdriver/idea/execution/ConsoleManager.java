package com.google.jstestdriver.idea.execution;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

/**
 * @author Sergey Simonchik
 */
public class ConsoleManager {

  private final PrintStream myOutStream;
  private final PrintStream myErrStream;

  public ConsoleManager() {
    myOutStream = System.out;
    myErrStream = System.err;
  }

  public void printToStdOut(@NotNull String message) {
    myOutStream.println(message);
  }

  public void printToStdErr(@NotNull String message) {
    myErrStream.println(message);
  }

}

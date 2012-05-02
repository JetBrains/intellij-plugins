package com.google.jstestdriver.idea.execution.tc;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class TC {

  private TC() {}

  @NotNull
  public static TCMessage testSuiteStarted(@NotNull String name) {
    return new TCMessage(TCCommand.TEST_SUITE_STARTED).addAttribute(TCAttribute.NAME, name);
  }

  @NotNull
  public static TCMessage testSuiteFinished(@NotNull String name) {
    return new TCMessage(TCCommand.TEST_SUITE_FINISHED).addAttribute(TCAttribute.NAME, name);
  }

  @NotNull
  public static TCMessage testStarted(@NotNull String name) {
    return new TCMessage(TCCommand.TEST_STARTED).addAttribute(TCAttribute.NAME, name);
  }

  @NotNull
  public static TCMessage testFinished(@NotNull String name) {
    return new TCMessage(TCCommand.TEST_FINISHED).addAttribute(TCAttribute.NAME, name);
  }

  @NotNull
  public static TCMessage testStdOut(@NotNull String name) {
    return new TCMessage(TCCommand.TEST_STDOUT).addAttribute(TCAttribute.NAME, name);
  }

  @NotNull
  public static TCMessage testStdErr(@NotNull String name) {
    return new TCMessage(TCCommand.TEST_STDERR).addAttribute(TCAttribute.NAME, name);
  }

  @NotNull
  public static TCMessage testFailed(final String name) {
    return new TCMessage(TCCommand.TEST_FAILED).addAttribute(TCAttribute.NAME, name);
  }

}

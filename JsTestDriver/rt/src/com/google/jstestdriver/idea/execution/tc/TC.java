package com.google.jstestdriver.idea.execution.tc;

import com.google.jstestdriver.idea.execution.tree.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class TC {

  private TC() {}

  @NotNull
  public static TCMessage testSuiteStarted(@NotNull AbstractSuiteNode suiteNode) {
    TCMessage message = newMessageWithId(TCCommand.TEST_SUITE_STARTED, suiteNode);
    addParentNodeIdAttribute(message, suiteNode);
    message.addAttribute(TCAttribute.NAME, suiteNode.getName());
    return message;
  }

  @NotNull
  public static TCMessage testSuiteFinished(@NotNull AbstractSuiteNode suiteNode) {
    return newMessageWithId(TCCommand.TEST_SUITE_FINISHED, suiteNode);
  }

  @NotNull
  public static TCMessage testStarted(@NotNull TestNode testNode) {
    TCMessage message = newMessageWithId(TCCommand.TEST_STARTED, testNode);
    addParentNodeIdAttribute(message, testNode);
    message.addAttribute(TCAttribute.NAME, testNode.getName());
    return message;
  }

  private static void addParentNodeIdAttribute(@NotNull TCMessage message, @NotNull AbstractNodeWithParent node) {
    AbstractJstdNode parent = node.getParent();
    message.addIntAttribute(TCAttribute.PARENT_NODE_ID, parent.getId());
  }

  @NotNull
  public static TCMessage testFinished(@NotNull TestNode testNode) {
    return newMessageWithId(TCCommand.TEST_FINISHED, testNode);
  }

  @NotNull
  public static TCMessage testStdOut(@NotNull TestNode testNode) {
    return newMessageWithId(TCCommand.TEST_STDOUT, testNode);
  }

  @NotNull
  public static TCMessage testStdErr(@NotNull String name) {
    return new TCMessage(TCCommand.TEST_STDERR).addAttribute(TCAttribute.NAME, name);
  }

  @NotNull
  public static TCMessage testFailed(@NotNull TestNode testNode) {
    return newMessageWithId(TCCommand.TEST_FAILED, testNode);
  }

  private static TCMessage newMessageWithId(@NotNull TCCommand command,
                                            @NotNull AbstractNodeWithParent node) {
    return new TCMessage(command).addIntAttribute(TCAttribute.NODE_ID, node.getId());
  }
}

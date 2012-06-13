package com.google.jstestdriver.idea.execution.tc;

import com.google.jstestdriver.idea.execution.tree.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class TC {

  private TC() {}

  @NotNull
  public static TCMessage newTestSuiteStartedMessage(@NotNull AbstractSuiteNode suiteNode) {
    TCMessage message = newMessageWithId(TCCommand.TEST_SUITE_STARTED, suiteNode);
    addParentNodeIdAttribute(message, suiteNode);
    message.addAttribute(TCAttribute.NAME, suiteNode.getName());
    return message;
  }

  @NotNull
  public static TCMessage newTestSuiteFinishedMessage(@NotNull AbstractSuiteNode suiteNode) {
    return newMessageWithId(TCCommand.TEST_SUITE_FINISHED, suiteNode);
  }

  @NotNull
  public static TCMessage newTestStartedMessage(@NotNull TestNode testNode) {
    return newLeafStartedMessage(testNode);
  }

  @NotNull
  public static TCMessage newTestFinishedMessage(@NotNull TestNode testNode) {
    return newMessageWithId(TCCommand.TEST_FINISHED, testNode);
  }

  @NotNull
  public static TCMessage newTestStdOutMessage(@NotNull TestNode testNode) {
    return newMessageWithId(TCCommand.TEST_STDOUT, testNode);
  }

  @NotNull
  public static TCMessage newTestFailedMessage(@NotNull TestNode testNode) {
    return newMessageWithId(TCCommand.TEST_FAILED, testNode);
  }

  @NotNull
  public static TCMessage newConfigErrorStartedMessage(@NotNull ConfigErrorNode configErrorNode) {
    return newLeafStartedMessage(configErrorNode);
  }

  @NotNull
  public static TCMessage newConfigErrorFinishedMessage(@NotNull ConfigErrorNode configErrorNode) {
    return newLeafFinishedAsErrorMessage(configErrorNode);
  }

  @NotNull
  public static TCMessage newRootErrorStartedMessage(@NotNull RootErrorNode rootErrorNode) {
    return newLeafStartedMessage(rootErrorNode);
  }

  @NotNull
  public static TCMessage newRootErrorFinishedMessage(@NotNull RootErrorNode rootErrorNode) {
    return newLeafFinishedAsErrorMessage(rootErrorNode);
  }

  private static TCMessage newLeafStartedMessage(@NotNull AbstractNodeWithParent node) {
    TCMessage message = newMessageWithId(TCCommand.TEST_STARTED, node);
    addParentNodeIdAttribute(message, node);
    message.addAttribute(TCAttribute.NAME, node.getName());
    return message;
  }

  @NotNull
  private static TCMessage newLeafFinishedAsErrorMessage(@NotNull AbstractNodeWithParent node) {
    TCMessage message = newMessageWithId(TCCommand.TEST_FAILED, node);
    message.addAttribute(TCAttribute.IS_TEST_ERROR, "yes");
    return message;
  }

  private static void addParentNodeIdAttribute(@NotNull TCMessage message, @NotNull AbstractNodeWithParent node) {
    AbstractNode parent = node.getParent();
    message.addIntAttribute(TCAttribute.PARENT_NODE_ID, parent.getId());
  }

  private static TCMessage newMessageWithId(@NotNull TCCommand command,
                                            @NotNull AbstractNodeWithParent node) {
    return new TCMessage(command).addIntAttribute(TCAttribute.NODE_ID, node.getId());
  }
}

package com.google.jstestdriver.idea.rt.execution.tree;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.rt.execution.tc.TC;
import com.google.jstestdriver.idea.rt.execution.tc.TCMessage;
import com.google.jstestdriver.idea.rt.util.EscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class TestNode extends AbstractNodeWithParent<TestNode> {

  public TestNode(@NotNull String testName, @NotNull TestCaseNode parent) {
    super(testName, parent);
  }

  @NotNull
  @Override
  public TestCaseNode getParent() {
    return (TestCaseNode) super.getParent();
  }

  @Override
  public String getProtocolId() {
    return "test";
  }

  @Override
  public String getLocationPath() {
    TestCaseNode testCase = getParent();
    String jsTestFilePath = testCase.getJsTestFilePath();
    if (jsTestFilePath == null) {
      jsTestFilePath = "";
    }
    List<String> path = Lists.newArrayList(jsTestFilePath, testCase.getName(), getName());
    return EscapeUtils.join(path, ':');
  }

  @NotNull
  @Override
  public TCMessage createStartedMessage() {
    return  TC.newTestStartedMessage(this);
  }
}

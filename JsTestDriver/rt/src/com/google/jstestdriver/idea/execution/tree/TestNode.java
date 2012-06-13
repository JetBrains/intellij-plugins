package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.util.EscapeUtils;
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
      return null;
    }
    List<String> path = Lists.newArrayList(jsTestFilePath, testCase.getName(), getName());
    return EscapeUtils.join(path, ':');
  }
}

package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.util.EscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class TestCaseNode extends AbstractSuiteNode<TestNode> {
  private final String myJsTestFilePath;

  public TestCaseNode(@NotNull String testCaseName,
                      @Nullable String jsTestFilePath,
                      @NotNull BrowserNode parent) {
    super(testCaseName, parent);
    myJsTestFilePath = jsTestFilePath;
  }

  @Override
  public String getProtocolId() {
    return "testCase";
  }

  @Nullable
  public String getJsTestFilePath() {
    return myJsTestFilePath;
  }

  @Override
  public String getLocationPath() {
    if (myJsTestFilePath == null) {
      return null;
    }
    List<String> path = Lists.newArrayList(myJsTestFilePath, getName());
    return EscapeUtils.join(path, ':');
  }
}

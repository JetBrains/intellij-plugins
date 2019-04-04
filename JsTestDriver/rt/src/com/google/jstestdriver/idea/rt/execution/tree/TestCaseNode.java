package com.google.jstestdriver.idea.rt.execution.tree;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.rt.util.EscapeUtils;
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
    String jsTestFilePath = myJsTestFilePath;
    if (jsTestFilePath == null) {
      jsTestFilePath = "";
    }
    List<String> path = Lists.newArrayList(jsTestFilePath, getName());
    return EscapeUtils.join(path, ':');
  }
}

package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;
import com.google.jstestdriver.TestResult.Result;

import java.util.Map;

/**
 * In the test results tree UI, this is an element representing a browser. It will have results for that browser as
 * children nodes.
 * @author alexeagle@google.com (Alex Eagle)
 */
public class BrowserNode extends Node {

  private final Map<String, JstdConfigFileNode> myJstdConfigFileMap = Maps.newHashMap();
  private Result worstResult = Result.passed;

  public BrowserNode(String browserName) {
    super(new SMTestProxyWithPrinterAndLocation(browserName, true, LocationProvider.EMPTY));
  }

  public JstdConfigFileNode getJstdConfigFileNodeByPath(String absoluteFilePath) {
    return myJstdConfigFileMap.get(absoluteFilePath);
  }

  public boolean allJstdConfigFilesComplete() {
    for (JstdConfigFileNode jstdConfigFileNode : myJstdConfigFileMap.values()) {
      if (!jstdConfigFileNode.allTestCasesComplete()) {
        return false;
      }
    }
    return true;
  }

  public void setTestFailed(Result result) {
    if (result == Result.error && worstResult != Result.error) {
      getTestProxy().setTestFailed("", "", true);
    } else if (result == Result.failed && worstResult == Result.passed) {
      getTestProxy().setTestFailed("", "", false);
    }
  }

  public void registerJstdConfigFileNode(JstdConfigFileNode jstdConfigFileNode) {
    myJstdConfigFileMap.put(jstdConfigFileNode.getAbsoluteFilePath(), jstdConfigFileNode);
  }

}

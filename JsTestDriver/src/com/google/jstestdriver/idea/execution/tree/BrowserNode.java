package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

/**
 * In the test results tree UI, this is an element representing a browser. It will have results for that browser as
 * children nodes.
 * @author alexeagle@google.com (Alex Eagle)
 */
@Deprecated
public class BrowserNode extends Node {

  private final Map<String, JstdConfigFileNode> myJstdConfigFileMap = Maps.newHashMap();
//  private Result worstResult = Result.passed;

  public BrowserNode(String browserName) {
    super(new SMTestProxyWithPrinterAndLocation(browserName, true, LocationProvider.EMPTY));
  }

  public JstdConfigFileNode getJstdConfigFileNodeByPath(String absoluteFilePath) {
    return myJstdConfigFileMap.get(absoluteFilePath);
  }

  public void registerJstdConfigFileNode(JstdConfigFileNode jstdConfigFileNode) {
    myJstdConfigFileMap.put(jstdConfigFileNode.getAbsoluteFilePath(), jstdConfigFileNode);
  }

  @Override
  public Collection<? extends Node> getChildren() {
    return myJstdConfigFileMap.values();
  }
}

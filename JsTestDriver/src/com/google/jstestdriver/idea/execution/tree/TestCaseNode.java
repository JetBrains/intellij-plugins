package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * In the test results tree UI, this is an element representing a test case result.
 * @author alexeagle@google.com (Alex Eagle)
 */
class TestCaseNode extends Node {

  private final JstdConfigFileNode myJstdConfigFileNode;
  private final Map<String, TestNode> myTestNodeMap = Maps.newHashMap();

  public TestCaseNode(final JstdConfigFileNode jstdConfigFileNode, @Nullable final String jsTestFilePath, String testCaseName) {
    myJstdConfigFileNode = jstdConfigFileNode;
    setTestProxy(new SMTestProxyWithPrinterAndLocation(testCaseName, true, new LocationProvider() {
      @Override
      Location provideLocation(@NotNull Project project) {
        if (jsTestFilePath == null) {
          return null;
        }
        PsiElement element = NavUtils.findPsiElement(
          project,
          new File(jsTestFilePath),
          getName(),
          null
        );
        return PsiLocation.fromPsiElement(element);
      }
    }));
    myJstdConfigFileNode.registerTestCaseNode(this);
  }

  public JstdConfigFileNode getJstdConfigFileNode() {
    return myJstdConfigFileNode;
  }

  public TestNode getTestByName(String testName) {
    return myTestNodeMap.get(testName);
  }

  public void registerTestNode(TestNode testNode) {
    myTestNodeMap.put(testNode.getTestProxy().getName(), testNode);
  }

  @Override
  public Collection<? extends Node> getChildren() {
    return myTestNodeMap.values();
  }
}

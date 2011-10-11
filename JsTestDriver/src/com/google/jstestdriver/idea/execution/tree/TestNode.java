package com.google.jstestdriver.idea.execution.tree;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

class TestNode extends Node {

  private final TestCaseNode myTestCaseNode;
  private boolean myDone = false;

  public TestNode(@NotNull final TestCaseNode testCaseNode, @NotNull final String testName) {
    myTestCaseNode = testCaseNode;
    setTestProxy(new SMTestProxyWithPrinterAndLocation(testName, false, new LocationProvider() {
      @Override
      Location provideLocation(@NotNull Project project) {
        PsiElement element = NavUtils.findPsiElement(project,
                                testCaseNode.getJstdConfigFileNode().getConfigFile(),
                                testCaseNode.getName(),
                                getName());
        return PsiLocation.fromPsiElement(element);
      }
    }));
    myTestCaseNode.registerTestNode(this);
  }

  public TestCaseNode getTestCaseNode() {
    return myTestCaseNode;
  }

  public void done() {
    myDone = true;
  }

  @Override
  public boolean isComplete() {
    return myDone;
  }

  @Override
  public Collection<? extends Node> getChildren() {
    return Collections.emptyList();
  }
}

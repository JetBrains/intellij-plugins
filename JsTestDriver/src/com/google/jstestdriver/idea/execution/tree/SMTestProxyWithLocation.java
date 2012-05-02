package com.google.jstestdriver.idea.execution.tree;

import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

@Deprecated
class SMTestProxyWithLocation extends SMTestProxy {

  private final Location location;

  public SMTestProxyWithLocation(String testName, boolean isSuite, @Nullable Location location) {
    super(testName, isSuite, null);
    this.location = location;
  }

  @Override
  public Location getLocation(Project project) {
    return location;
  }
}

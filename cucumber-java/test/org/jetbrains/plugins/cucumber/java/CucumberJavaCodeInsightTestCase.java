package org.jetbrains.plugins.cucumber.java;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;

abstract public class CucumberJavaCodeInsightTestCase extends CucumberCodeInsightTestCase {
  // Making it abstract again to force implementors to explicitly specify what version of Cucumber they want to use.
  @Override
  protected abstract LightProjectDescriptor getProjectDescriptor();
}

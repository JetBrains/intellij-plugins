package org.jetbrains.plugins.cucumber.java;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;

abstract public class CucumberJavaCodeInsightTestCase extends CucumberCodeInsightTestCase {
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber2ProjectDescriptor();
  }
}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.testFramework.LightProjectDescriptor;

public class CucumberJavaVersionUtilTest extends CucumberJavaCodeInsightTestCase {
  public void testCucumber_4_5_VersionDetection() {
    assertEquals("4.5", CucumberJavaVersionUtil.getCucumberCoreVersion(getModule(), getProject()));
  }

  public void testCucumber_3_0_VersionDetection() {
    assertEquals("3", CucumberJavaVersionUtil.getCucumberCoreVersion(getModule(), getProject()));
  }

  public void testCucumber_1_2_VersionDetection() {
    assertEquals("1.2", CucumberJavaVersionUtil.getCucumberCoreVersion(getModule(), getProject()));
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    if (getTestName(false).contains("3_0")) {
      return CucumberJavaTestUtil.createCucumber3ProjectDescriptor();
    }
    if (getTestName(false).contains("2_0")) {
      return CucumberJavaTestUtil.createCucumber2ProjectDescriptor();
    }
    if (getTestName(false).contains("1_2")) {
      return CucumberJavaTestUtil.createCucumber1ProjectDescriptor();
    }

    return CucumberJavaTestUtil.createCucumber4_5ProjectDescriptor();
  }
}

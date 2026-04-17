// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.documentation;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

import static com.intellij.platform.backend.documentation.impl.ImplKt.computeHtmlDocBlocking;

/**
 * Tests that Quick Documentation (Ctrl+Q) shows Javadoc from Java step definitions
 * when invoked on Gherkin steps.
 */
public class CucumberJavaDocumentationTest extends BasePlatformTestCase {

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "documentation";
  }

  /// Test for IDEA-383824.
  public void testSimpleStep() {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile("test.feature");

    String html = computeHtmlDocBlocking(myFixture.getEditor(), myFixture.getFile());
    assertNotNull("Documentation HTML should not be null", html);
    assertTrue("Documentation should contain the Javadoc description", html.contains("This step logs in a user"));
  }
}

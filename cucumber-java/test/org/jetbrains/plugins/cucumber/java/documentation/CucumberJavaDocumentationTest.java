// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.documentation;

import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.platform.backend.documentation.DocumentationData;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.impl.ImplKt;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

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

    Editor editor = myFixture.getEditor();
    PsiFile file = myFixture.getFile();
    int offset = editor.getCaretModel().getOffset();

    var targets = PlatformTestUtil.callOnBgtSynchronously(() -> ReadAction.compute(() -> {
      return IdeDocumentationTargetProvider.getInstance(getProject()).documentationTargets(editor, file, offset);
    }), 10);

    assertFalse("Expected at least one documentation target", targets.isEmpty());

    DocumentationTarget target = targets.getFirst();
    DocumentationData documentationData = ImplKt.computeDocumentationBlocking(target.createPointer());

    assertNotNull("Documentation result should not be null", documentationData);

    String html = documentationData.getHtml();
    assertNotNull("Documentation HTML should not be null", html);
    assertTrue("Documentation should contain the Javadoc description", html.contains("This step logs in a user"));
  }
}

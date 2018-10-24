// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.codeInsight.navigation.actions.GotoTypeDeclarationAction;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class ComponentDeclarationNavigationTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testGoToDeclarationHandler() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("customUsage.html", "custom.html", "custom.ts", "package.json");

      Presentation result = myFixture.testAction(new GotoDeclarationAction());
      assertEquals("Component Declaration", result.getText());

      Editor focusedEditor = FileEditorManager.getInstance(myFixture.getProject()).getSelectedTextEditor();
      PsiFile file = PsiDocumentManager.getInstance(myFixture.getProject()).getPsiFile(focusedEditor.getDocument());
      assertEquals("custom.ts", file.getName());
    });
  }

  public void testTypeDeclarationHandler() {
    JSTestUtils.testES6(getProject(), () -> {
      myFixture.configureByFiles("customUsage.html", "custom.html", "custom.ts", "package.json");

      Presentation result = myFixture.testAction(new GotoTypeDeclarationAction());
      assertEquals("Component Template", result.getText());

      Editor focusedEditor = FileEditorManager.getInstance(myFixture.getProject()).getSelectedTextEditor();
      PsiFile file = PsiDocumentManager.getInstance(myFixture.getProject()).getPsiFile(focusedEditor.getDocument());
      assertEquals("custom.html", file.getName());
    });
  }
}

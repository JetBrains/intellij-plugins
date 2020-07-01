// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.codeInsight.navigation.actions.GotoTypeDeclarationAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static com.intellij.util.containers.ContainerUtil.newArrayList;

@RunWith(Parameterized.class)
public class ComponentDeclarationNavigationTest extends Angular2CodeInsightFixtureTestCase {

  @Parameterized.Parameter
  public boolean myIsComponent;

  @Parameterized.Parameter(value = 1)
  public boolean muIsInjected;

  @Parameterized.Parameter(value = 2)
  public String myLocation;

  @Parameterized.Parameter(value = 3)
  public String myElementText;

  @Parameterized.Parameters(name = "Component={0}, Injected={1}: {2}, {3}")
  public static Collection<Object> data() {
    return newArrayList(
      new Object[]{true, false, "<my-cu<caret>st", "my-customer"},
      new Object[]{true, true, "<my-cu<caret>st", "my-customer"},
      new Object[]{true, false, "my-cu<caret>stomer-att", "my-customer-attr"},
      new Object[]{true, true, "my-cu<caret>stomer-att", "my-customer-attr"},
      new Object[]{false, false, "foo-<caret>dir", "foo-directive"},
      new Object[]{false, true, "foo-<caret>dir", "foo-directive"}
    );
  }

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  @Test
  public void testGoToDeclarationHandler() throws Exception {
    doTest(new GotoDeclarationAction(),
           muIsInjected ? "customUsage.ts" : "customUsage.html",
           myLocation,
           myIsComponent ? "Component Declaration" : "Directive Declaration",
           "custom.ts",
           myElementText);
  }

  @Test
  public void testTypeDeclarationHandler() throws Exception {
    if (myIsComponent) {
      doTest(new GotoTypeDeclarationAction(),
             muIsInjected ? "customUsage.ts" : "customUsage.html",
             myLocation,
             "Component Template",
             "custom.html",
             "<div>custom</div>");
    }
    else {
      doTest(new GotoTypeDeclarationAction(),
             muIsInjected ? "customUsage.ts" : "customUsage.html",
             myLocation,
             null,
             muIsInjected ? "customUsage.ts" : "customUsage.html",
             null);
    }
  }

  private void doTest(@NotNull AnAction action,
                      @NotNull String testFile,
                      @NotNull String location,
                      @Nullable String actionLabel,
                      @Nullable String targetFile,
                      @Nullable String elementText) throws Exception {
    myFixture.configureByFiles(testFile, "custom.html", "custom.ts", "package.json");

    AngularTestUtil.moveToOffsetBySignature(location, myFixture);

    Presentation result = myFixture.testAction(action);
    assertEquals(actionLabel, result.getText());

    Editor focusedEditor = FileEditorManager.getInstance(myFixture.getProject()).getSelectedTextEditor();
    PsiFile file = PsiDocumentManager.getInstance(myFixture.getProject()).getPsiFile(focusedEditor.getDocument());
    assertEquals(targetFile, file.getName());

    if (elementText == null) {
      return;
    }
    int findTargetFlags = TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED | TargetElementUtil.ELEMENT_NAME_ACCEPTED;
    PsiElement element = TargetElementUtil.findTargetElement(focusedEditor, findTargetFlags);
    assertNotNull(element);
    assertEquals(elementText, element.getText());
  }
}

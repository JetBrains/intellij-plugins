// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.editor;

import com.intellij.codeInsight.generation.surroundWith.SurroundWithHandler;
import com.intellij.codeInsight.template.impl.InvokeTemplateAction;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.surroundWith.JSStatementsSurroundDescriptor;
import com.intellij.lang.javascript.surroundWith.JSWithCastSurrounder;
import com.intellij.lang.javascript.surroundWith.JSWithIfSurrounder;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class ActionScriptSurroundWithTest extends BasePlatformTestCase {
  private static final int IF_SURROUNDER_INDEX = 0;
  private static final int TRY_SURROUNDER_INDEX = 5;
  private static final int TRY_FINALLY_SURROUNDER_INDEX = 7;

  private void runLiveTemplate(String ext, final String templateName, final String descr, final boolean shouldBeAvailable) {
    doTest("", ext, () -> {
      final List<AnAction> anActions = SurroundWithHandler.buildSurroundActions(getProject(), myFixture.getEditor(), myFixture.getFile());
      InvokeTemplateAction action = null;
      for (AnAction a : anActions) {
        if (a instanceof InvokeTemplateAction) {
          final TemplateImpl template = ((InvokeTemplateAction)a).getTemplate();
          if (templateName.equals(template.getKey()) && template.getDescription().contains(descr)) {
            action = (InvokeTemplateAction)a;
            break;
          }
        }
      }

      if (shouldBeAvailable) {
        assertNotNull(action);
        action.actionPerformed(AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance().getDataContext()));
      }
      else {
        assertNull(action);
      }
    });
  }

  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  private void doTest(final Surrounder handler, String dir, String ext) {
    doTest(dir, ext, () -> SurroundWithHandler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), handler));
  }

  private void doTest(String dir, String ext, Runnable action) {
    String baseName = "/surroundWith/";
    if (dir.length() > 0) baseName += dir + "/";
    baseName += getTestName(false);
    myFixture.configureByFile(baseName + "." + ext);
    action.run();

    String name = baseName + "_after." + ext;
    final File file = new File(getTestDataPath() + "/" + name);
    myFixture.checkResultByFile(file.exists() ? name : baseName + "." + ext);
  }

  public void testIf() {
    final Surrounder surrounder = new JSStatementsSurroundDescriptor().getSurrounders()[IF_SURROUNDER_INDEX];

    doTest(surrounder, "", "js2");
    doTest(surrounder, "", "as");
  }

  public void testIf2() {
    final Surrounder surrounder = new JSWithIfSurrounder(true);

    doTest(surrounder, "", "js2");
  }

  public void testIf2_2() {
    final Surrounder surrounder = new JSStatementsSurroundDescriptor().getSurrounders()[IF_SURROUNDER_INDEX];

    doTest(surrounder, "", "js2");
  }

  public void testCast() {
    final Surrounder surrounder = new JSWithCastSurrounder();

    doTest(surrounder, "", "js2");
  }

  public void testTry() {
    final Surrounder surrounder = new JSStatementsSurroundDescriptor().getSurrounders()[TRY_SURROUNDER_INDEX];

    doTest(surrounder, "", "js2");
  }

  public void testTryFinally() {
    final Surrounder surrounder = new JSStatementsSurroundDescriptor().getSurrounders()[TRY_FINALLY_SURROUNDER_INDEX];

    doTest(surrounder, "", "js2");
  }

  public void testTag() {
    runLiveTemplate("mxml", "T", "Surround with <tag>", true);
  }

  public void testTag2() {
    runLiveTemplate("mxml", "P", "Surround with {}", false);
  }
}

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring;

import com.intellij.openapi.editor.EditorSettings;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public abstract class BaseIntroduceVariableRefactoringTest extends BasePlatformTestCase {

  protected void doTest() {
    doTest(true);
  }

  protected void doTest(boolean replaceAll) {
    doTest(replaceAll, "foo");
  }

  protected void doTest(boolean replaceAll, String name) {
    myFixture.configureByFile(getTestName(false) + ".tf");
    final EditorSettings settings = myFixture.getEditor().getSettings();
    boolean inplaceEnabled = settings.isVariableInplaceRenameEnabled();
    try {
      settings.setVariableInplaceRenameEnabled(false);
      BaseIntroduceVariableHandler handler = createHandler();
      final BaseIntroduceOperation operation = createIntroduceOperation(name);
      operation.setReplaceAll(replaceAll);
      //noinspection unchecked
      handler.performAction(operation);
      myFixture.checkResultByFile(getTestName(false) + ".after" + ".tf");
    } finally {
      settings.setVariableInplaceRenameEnabled(inplaceEnabled);
    }
  }

  @NotNull
  protected abstract BaseIntroduceOperation createIntroduceOperation(String name);

  @NotNull
  protected abstract BaseIntroduceVariableHandler createHandler();

}

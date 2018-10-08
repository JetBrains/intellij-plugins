// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.base;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.javascript.refactoring.inline.JSInlineHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public abstract class FlexInlineVarOrFunctionTestBase extends BaseFlexRefactoringTestCase {

  protected Runnable myAfterCommitRunnable;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myAfterCommitRunnable = null;
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);
    if (myAfterCommitRunnable != null) {
      myAfterCommitRunnable.run();
    }
  }

  protected void doTest(final String baseName, String ext) throws Exception {
    doTest(new String[]{baseName + "." + ext});
  }

  protected void doTest(String[] filenames) throws Exception {
    doTest(filenames, false);
  }

  protected void doTest(String[] filenames, boolean onlyOneRef) throws Exception {
    configureByFiles(null, filenames);
    Editor injectedEditor = setupInjectedEditor();

    invokeHandler(onlyOneRef);

    resetInjectedEditor(injectedEditor);
    String[] nameExt = filenames[0].split("\\.");
    checkResultByFile(nameExt[0] + "_after." + nameExt[1]);
  }


  protected void doTestFailure(final String baseName, String ext, String reason) {
    doTestFailure(new String[]{baseName + "." + ext}, reason);
  }

  protected void doTestFailure(final String[] filenames, String reason) {
    configureByFiles(null, filenames);
    Editor injectedEditor = setupInjectedEditor();

    assertErrorHint(() -> invokeHandler(false), reason);
    resetInjectedEditor(injectedEditor);
  }

  protected void doTestConflicts(final String baseName, String ext, String[] conflicts) {
    doTestConflicts(new String[]{baseName + "." + ext}, conflicts);
  }

  protected void doTestConflicts(final String[] filenames, String[] conflicts) {
    configureByFiles(null, filenames);
    Editor injectedEditor = setupInjectedEditor();

    assertConflicts(() -> invokeHandler(false), conflicts);
    resetInjectedEditor(injectedEditor);
  }

  protected static void assertErrorHint(ThrowableRunnable<? extends Exception> r, String reason) {
    try {
      r.run();
      fail("Refactoring should not be performed because of " + reason);
    }
    catch (CommonRefactoringUtil.RefactoringErrorHintException ex) {
      assertEquals("Unexpected fail reason", reason, ex.getLocalizedMessage());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected static void assertConflicts(ThrowableRunnable<? extends Exception> r, String... conflicts) {
    try {
      r.run();
      fail("Conflicts expected:\n" + toString(Arrays.asList(conflicts)));
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertSameElements(e.getMessages(), conflicts);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void invokeHandler(boolean onlyOneRef) {
    final PsiElement targetElement = TargetElementUtil.findTargetElement(
      myEditor,
      TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED | TargetElementUtil.ELEMENT_NAME_ACCEPTED
    );
    if (targetElement == null) {
      fail("Element at caret not found");
    }
    final JSInlineHandler handler = getInlineHandler(onlyOneRef);
    handler.inlineElement(getProject(), getEditor(), targetElement);
  }

  @NotNull
  protected JSInlineHandler getInlineHandler(boolean onlyOneRef) {
    return new JSInlineHandler() {
        @Nullable
        @Override
        protected Settings getSettingsForElement(@NotNull PsiElement element,
                                                 Editor editor,
                                                 @Nullable PsiReference invocationReference,
                                                 NotNullLazyValue<Collection<PsiReference>> elementUsages) {
          Settings settings = super.getSettingsForElement(element, editor, invocationReference, elementUsages);
          if (settings != null) {
            settings.setOneRefToInline(onlyOneRef);
          }
          return settings;
        }
      };
  }
}

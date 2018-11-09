// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.base;

import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.extractMethod.DefaultJSExtractFunctionSettings;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionHandler;
import com.intellij.lang.javascript.refactoring.extractMethod.JSScopeSelectionUI;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.Function;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;

import java.util.List;
import java.util.function.Supplier;

public abstract class FlexExtractFunctionBaseTest extends BaseFlexRefactoringTestCase {
  protected static String BASE_PATH = "/refactoring/extractFunction/";

  protected JSExtractFunctionHandler createMockHandler(JSScopeSelectionUI scopeSelector) {
    return new JSExtractFunctionHandler(scopeSelector);
  }

  protected boolean assertInplace() {return false;}
  

  protected void doTestWithScopeSelection(Function<List<JSExtractFunctionHandler.IntroductionScope>, JSExtractFunctionHandler.IntroductionScope> scopeSelector,
                                          String extension) throws Exception {
    JSAttributeList.AccessType _accessType = JSAttributeList.AccessType.PUBLIC;
    doTestWithScopeSelection(
      () -> new DefaultJSExtractFunctionSettings("created", false, false, _accessType, null, null),
      scopeSelector, getTestName(false), getTestName(false), extension);
  }

  protected void doTest(final String varName, String ext) throws Exception {
    doTest(() -> new DefaultJSExtractFunctionSettings(varName, false, false, JSAttributeList.AccessType.PUBLIC, null, null), ext);
  }

  protected void doTest(Supplier<DefaultJSExtractFunctionSettings> extractFunctionSettingsProducer,
                        String ext)
    throws Exception {
    String fileName = getTestName(false);
    doTest(extractFunctionSettingsProducer, fileName, fileName, ext);
  }

  protected void doTest(Supplier<DefaultJSExtractFunctionSettings> extractFunctionSettingsProducer,
                        String fileName,
                        String fileNameAfter,
                        String ext)
    throws Exception {
    doTestWithScopeSelection(extractFunctionSettingsProducer, scopes -> {
      Editor injectedEditor = setupInjectedEditor();
      int start = injectedEditor.getSelectionModel().getSelectionStart();
      final PsiFile file = PsiDocumentManager.getInstance(injectedEditor.getProject()).getPsiFile(injectedEditor.getDocument());
      JSExtractFunctionHandler.IntroductionScope base = (new JSExtractFunctionHandler()).findBase(file.findElementAt(start), false);
      JSExtractFunctionHandler.IntroductionScope scope = ContainerUtil.find(scopes, t -> t.parent == base.parent);
      if (scope != null) {
        return scope;
      }
      return ContainerUtil.getFirstItem(scopes);
    }, fileName, fileNameAfter, ext);
  }

  protected void doTestWithScopeSelection(Supplier<DefaultJSExtractFunctionSettings> extractFunctionSettingsProducer,
                                          Function<List<JSExtractFunctionHandler.IntroductionScope>, JSExtractFunctionHandler.IntroductionScope> scopeSelector,
                                          String fileName,
                                          String fileNameAfter,
                                          String ext)
    throws Exception {
    configureByFile(fileName + "." + ext);
    Editor injectedEditor = setupInjectedEditor();

    TemplateManagerImpl.setTemplateTesting(getProject(), getTestRootDisposable());
    try {
      DefaultJSExtractFunctionSettings settings = extractFunctionSettingsProducer.get();

      createMockHandler(JSScopeSelectionUI.syncTestScopeSelector(scopeSelector))
        .invokeWithSettings(getEditor(), getFile(), settings);
      TemplateState state = TemplateManagerImpl.getTemplateState(getEditor());
      if (state == null && assertInplace()) {
        fail("Inplace template was not started");
      }
      if (state != null) {
        type(settings.getMethodName());
      }
      state = TemplateManagerImpl.getTemplateState(getEditor());
      if (state != null) {
        state.gotoEnd(false);
      }
    }
    finally {
      resetInjectedEditor(injectedEditor);
    }
    checkResultByFile(fileNameAfter + "_after." + ext);
  }

  protected static void assertFails(ThrowableRunnable<? extends Exception> r) throws Exception {
    try {
      r.run();
      fail("Refactoring should not be performed");
    }
    catch (CommonRefactoringUtil.RefactoringErrorHintException ignored) {
    }
  }

}

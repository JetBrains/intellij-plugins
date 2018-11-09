// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.base;

import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableHandler;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.CaretSpecificDataContext;
import com.intellij.openapi.util.Pass;
import com.intellij.refactoring.actions.IntroduceVariableAction;
import com.intellij.refactoring.introduce.inplace.OccurrencesChooser;
import com.intellij.testFramework.MapDataContext;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class FlexInplaceIntroduceVariableTestCase extends BaseFlexRefactoringTestCase {

  private static final String BASE_PATH = "/refactoring/introduceVariable/";

  @NotNull
  @Override
  protected String getTestDataPath() {
    return JSTestUtils.getTestDataPath() + BASE_PATH;
  }

  protected void defaultReplaceAllTest() throws Exception {
    String fileName = getTestName(false);
    doTest("created", fileName, "." + getExtension(), true, null, null, fileName);
  }

  protected void defaultTest() throws Exception {
    doTest(getTestName(false), "." + getExtension(), null);
  }

  protected void doTest(String fileName, final String extension) throws Exception {
    doTest(fileName, extension, null);
  }

  protected void doTest(String fileName, final String extension, @Nullable Function<List<? extends JSExpression>, JSExpression> expressionChooser)
    throws Exception {
    doTest("created", fileName, extension, false, null, expressionChooser, fileName);
  }

  protected void doTest(@Nullable String typedName, String fileNameAfter, final String extension, String... files) throws Exception {
    doTest(typedName, fileNameAfter, extension, false, null, null, files);
  }

  protected void doTest(@Nullable String typedName,
                        String fileNameAfter,
                        final String extension,
                        final boolean replaceAll,
                        @Nullable Runnable afterTypingRunnable,
                        @Nullable Function<List<? extends JSExpression>, JSExpression> expressionChooser,
                        String... files) throws Exception {
    final String[] names = new String[files.length];
    for (int i = 0; i < names.length; i++) {
      names[i] = files[i] + extension;
    }
    configureByFiles(null, names);

    final Editor editor = getEditor();
    final Editor injectedEditor = setupInjectedEditor();
    try {
      if (typedName != null) {
        TemplateManagerImpl.setTemplateTesting(getProject(), getTestRootDisposable());
      }
      final JSIntroduceVariableHandler handler = new JSIntroduceVariableHandler((editor1, elements, callback, presenter) -> {
        final JSExpression selectedExpression = expressionChooser != null
                                                ? expressionChooser.fun(elements)
                                                : ContainerUtil.getLastItem(elements);
        callback.pass(selectedExpression);
      }) {
        @Override
        protected void chooseOccurrencesToReplace(Editor editor,
                                                  Map<OccurrencesChooser.ReplaceChoice, List<JSExpression>> occurrencesMap,
                                                  Pass<OccurrencesChooser.ReplaceChoice> callback) {
          callback.pass(replaceAll ? OccurrencesChooser.ReplaceChoice.ALL : OccurrencesChooser.ReplaceChoice.NO);
        }
      };
      handler.invoke(getProject(), getEditor(), getFile(), null);
      if (typedName != null) {
        type(typedName);
      }
      if (afterTypingRunnable != null) {
        afterTypingRunnable.run();
      }
    }
    finally {
      if (typedName != null) {
        final TemplateState state = TemplateManagerImpl.getTemplateState(editor);
        if (state != null) {
          WriteCommandAction.runWriteCommandAction(null, () -> state.gotoEnd(false));
        }
      }
      resetInjectedEditor(injectedEditor);
    }
    checkResultByFile(fileNameAfter + "_after" + extension);
  }

  protected  void performActionIntroduce() {
    IntroduceVariableAction action = new IntroduceVariableAction();
    MapDataContext delegate = new MapDataContext();
    delegate.put(CommonDataKeys.PROJECT, getProject());
    delegate.put(CommonDataKeys.EDITOR, getEditor());
    delegate.put(CommonDataKeys.PSI_FILE, getFile());
    DataContext context = new CaretSpecificDataContext(delegate, getEditor().getCaretModel().getCurrentCaret());

    TestActionEvent event = new TestActionEvent(context, action);
    action.actionPerformed(event);
  }
}

// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.refactoring.JSIntroduceTargetChooser;
import com.intellij.lang.javascript.refactoring.introduce.BasicIntroducedEntityInfoProvider;
import com.intellij.lang.javascript.refactoring.introduce.JSBaseIntroduceHandler;
import com.intellij.lang.javascript.refactoring.introduceParameter.JSIntroduceParameterHandler;
import com.intellij.lang.javascript.refactoring.introduceParameter.JSIntroduceParameterSettings;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableTestCase;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ActionScriptIntroduceParameterTest extends BasePlatformTestCase {
  @Override
  protected @NotNull String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("/refactoring/introduceParameter/");
  }

  private void doTest(final boolean optionalParameter, final String initialValue, String ext) {
    doTest(getHandler(optionalParameter, initialValue, null, null), ext);
  }

  @SuppressWarnings("SameParameterValue")
  private static @NotNull JSIntroduceParameterHandler getHandler(boolean optionalParameter,
                                                                 @Nullable String initialValue,
                                                                 @Nullable Function<? super List<? extends JSExpression>, ? extends JSExpression> expressionChooser,
                                                                 @Nullable Function<? super List<? extends JSFunction>, ? extends JSFunction> scopeChooser) {
    return new JSIntroduceParameterHandler(toTargetChooser(expressionChooser, JSBaseIntroduceHandler.DEFAULT_EXPRESSION_CHOOSER),
                                           toTargetChooser(scopeChooser, JSIntroduceParameterHandler.DEFAULT_SCOPE_CHOOSER)) {
      @Override
      protected JSIntroduceParameterSettings getSettings(Project project,
                                                         Editor editor,
                                                         final Pair<JSExpression, TextRange> expressionDescriptor,
                                                         JSExpression[] occurrences,
                                                         final PsiElement scope) {
        return new JSIntroduceParameterSettings.Base(expressionDescriptor) {
          @Override
          public boolean addOptionalParameter() {
            return optionalParameter;
          }

          @Override
          public String getInitialValue() {
            return initialValue != null ? initialValue : myExpressionDescriptor.first.getText();
          }

          @Override
          public boolean isReplaceAllOccurrences() {
            return true;
          }

          @Override
          public String getVariableName() {
            return "created";
          }

          @Override
          public String getVariableType() {
            return null;
          }

          @Override
          public JSFunction functionForIntroduceParameter() {
            if (scope instanceof JSFunction) return (JSFunction)scope;
            return super.functionForIntroduceParameter();
          }
        };
      }
    };
  }

  private static @NotNull <T> JSIntroduceTargetChooser<T> toTargetChooser(@Nullable Function<? super List<? extends T>, ? extends T> scopeChooser, @NotNull JSIntroduceTargetChooser<T> defaultVal) {
    return scopeChooser != null ? (editor, elements, callback, presenter) -> callback.accept(scopeChooser.fun(elements)) : defaultVal;
  }

  private void doTest(JSIntroduceParameterHandler handler, String ext) {
    String fileName = getTestName(false);
    myFixture.configureByFile(fileName + "." + ext);
    invokeHandler(handler);
    myFixture.checkResultByFile(fileName + "_after." + ext);
  }

  private void invokeHandler(JSIntroduceParameterHandler handler) {
    final Editor editor = myFixture.getEditor();
    editor.getSettings().setVariableInplaceRenameEnabled(false);
    handler.invoke(getProject(), myFixture.getEditor(), myFixture.getFile(), null);
    JSIntroduceVariableTestCase.waitIntroduceHandler();
  }

  public void testBasic() {
    doTest(false, null, "js2");
  }

  public void testBasic_2() {
    doTest(true, "", "js2");
  }

  public void testOverride() {
    doTest(false, null, "js2");
  }

  public void testBasicInitialValue() {
    doTest(true, "\"\"", "js2");
  }

  public void testBasicInitialValue_2() {
    doTest(true, "\"\"", "js2");
  }

  public void testConstructorCall() {
    doTest(false, "123", "js2");
  }

  public void testArrayLiteral() {
    doTest(true, "[1]", "js2");
  }

  public void testConstructorCall2() {
    doTest(new JSIntroduceParameterHandler() {

      @Override
      protected JSIntroduceParameterSettings getSettings(Project project,
                                                         Editor editor,
                                                         final Pair<JSExpression, TextRange> expressionDescriptor,
                                                         final JSExpression[] occurrences,
                                                         final PsiElement scope) {
        return new JSIntroduceParameterSettings.Base(expressionDescriptor) {
          @Override
          public boolean addOptionalParameter() {
            return true;
          }

          @Override
          public String getInitialValue() {
            return "100";
          }

          @Override
          public boolean isReplaceAllOccurrences() {
            return false;
          }

          @Override
          public String getVariableName() {
            return "_x";
          }

          @Override
          public String getVariableType() {
            return new BasicIntroducedEntityInfoProvider(expressionDescriptor.first, occurrences, scope).evaluateType();
          }
        };
      }
    }, "js2");
  }
}

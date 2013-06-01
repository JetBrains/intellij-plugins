package com.jetbrains.lang.dart.refactoring.introduce;

import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.Consumer;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceHandler;
import com.jetbrains.lang.dart.ide.refactoring.introduce.DartIntroduceOperation;
import com.jetbrains.lang.dart.psi.DartExpression;
import com.jetbrains.lang.dart.util.DartNameSuggesterUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author: Fedor.Korotkov
 */
public abstract class DartIntroduceTestBase extends LightPlatformCodeInsightFixtureTestCase {
  protected void doTestSuggestions(Class<? extends DartExpression> parentClass, String... expectedNames) {
    final Collection<String> names = buildSuggestions(parentClass);
    for (String expectedName : expectedNames) {
      assertTrue(StringUtil.join(names, ", "), names.contains(expectedName));
    }
  }

  protected Collection<String> buildSuggestions(Class<? extends DartExpression> parentClass) {
    myFixture.configureByFile(getTestName(false) + getFileExtension());
    DartIntroduceHandler handler = createHandler();
    DartExpression expr = PsiTreeUtil.getParentOfType(
      myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset()),
      parentClass
    );
    return DartNameSuggesterUtil.getSuggestedNames(expr);
  }

  protected String getFileExtension() {
    return ".dart";
  }

  protected abstract DartIntroduceHandler createHandler();

  protected void doTest() {
    doTest(null, true);
  }

  protected void doTest(@Nullable Consumer<DartIntroduceOperation> customization, boolean replaceAll) {
    myFixture.configureByFile(getTestName(false) + getFileExtension());
    boolean inplaceEnabled = myFixture.getEditor().getSettings().isVariableInplaceRenameEnabled();
    try {
      myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(false);
      DartIntroduceHandler handler = createHandler();
      final DartIntroduceOperation operation =
        new DartIntroduceOperation(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), "foo");
      operation.setReplaceAll(replaceAll);
      if (customization != null) {
        customization.consume(operation);
      }
      handler.performAction(operation);
      doCheck();
    }
    finally {
      myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(inplaceEnabled);
    }
  }

  protected void doCheck() {
    myFixture.checkResultByFile(getTestName(false) + ".after" + getFileExtension());
  }

  protected void doTestInplace(@Nullable Consumer<DartIntroduceOperation> customization) {
    String name = getTestName(false);
    myFixture.configureByFile(name + getFileExtension());
    final boolean enabled = myFixture.getEditor().getSettings().isVariableInplaceRenameEnabled();
    try {
      TemplateManagerImpl.setTemplateTesting(getProject(), getTestRootDisposable());
      myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(true);

      DartIntroduceHandler handler = createHandler();
      final DartIntroduceOperation introduceOperation =
        new DartIntroduceOperation(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), "a");
      introduceOperation.setReplaceAll(true);
      if (customization != null) {
        customization.consume(introduceOperation);
      }
      handler.performAction(introduceOperation);

      TemplateState state = TemplateManagerImpl.getTemplateState(myFixture.getEditor());
      assert state != null;
      state.gotoEnd(false);
      myFixture.checkResultByFile(name + ".after" + getFileExtension(), true);
    }
    finally {
      myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(enabled);
    }
  }
}

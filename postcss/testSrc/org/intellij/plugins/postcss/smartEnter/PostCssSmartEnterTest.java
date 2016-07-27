package org.intellij.plugins.postcss.smartEnter;

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessors;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PostCssSmartEnterTest extends PostCssFixtureTestCase {

  public void testCompleteCustomSelectorWithSemicolon() {
    doTest();
  }

  private void doTest() throws IncorrectOperationException {
    myFixture.configureByFile(getTestName(true) + "_before.pcss");
    final List<SmartEnterProcessor> processors = SmartEnterProcessors.INSTANCE.allForLanguage(PostCssLanguage.INSTANCE);
    new WriteCommandAction(myFixture.getProject()) {
      @Override
      protected void run(@NotNull Result result) throws Throwable {
        final Editor editor = myFixture.getEditor();
        for (SmartEnterProcessor processor : processors) {
          processor.process(myFixture.getProject(), editor, myFixture.getFile());
        }
      }
    }.execute();
    myFixture.checkResultByFile(getTestName(true) + "_after.pcss");
  }
}
package org.angularjs.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.javascript.JSInjectionBracesUtil;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularTypedHandlerTest extends LightPlatformCodeInsightFixtureTestCase {
  public void testBracketsClosing() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>");
    myFixture.type("{");
    myFixture.checkResult("{{<caret>}}");
  }

  public void testBracketsNotClosingTwice() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>}}");
    myFixture.type("{");
    myFixture.checkResult("{{<caret>}}");
  }

  public void testBracketsNotBreakingAtEnd() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>");
    myFixture.type("}");
    myFixture.checkResult("{{}}<caret>");
  }

  public void testClosingBracketsSkipped() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>}}");
    myFixture.type("}");
    myFixture.checkResult("{{}<caret>}");
  }

  public void testSecondClosingBracket() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{}<caret>");
    myFixture.type("}");
    myFixture.checkResult("{{}}<caret>");
  }

  public void testInsertWhitespace() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>");
    JSCodeStyleSettings settings = JSCodeStyleSettings.getSettings(myFixture.getFile());
    boolean oldWhitespace = settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS;
    try {
      settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = true;
      myFixture.type("{");
      myFixture.checkResult("{{ <caret> }}");
    }
    finally {
      settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = oldWhitespace;
    }
  }

  public void testOneSymbolDelimiterStartCompletes() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "<caret>");
    doInterpolationBracesCompleterTest("$", "#", '$', true);
    myFixture.checkResult("$ <caret> #");
  }

  public void testMixedDelimitersAlreadyHasEnding() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>#");
    doInterpolationBracesCompleterTest("$$", "#", '$', false);
    myFixture.checkResult("$$<caret>#");
  }

  public void testMixedDelimitersCompletionNoStartTypeOver() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>$");
    doInterpolationBracesCompleterTest("$$", "#", '$', false);
    myFixture.checkResult("$$<caret>#$");
  }

  public void testOneSymbolDelimiterEndAdded() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>");
    doInterpolationBracesCompleterTest("$", "$", '$', false);
    myFixture.checkResult("$$<caret>");
  }

  public void testOneSymbolDelimiterTypeOver() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>$");
    doInterpolationBracesCompleterTest("$", "$", '$', false);
    myFixture.checkResult("$$<caret>");
  }

  public void testOneSymbolDelimiterTypeOverOneSymbol() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "<caret>$");
    doInterpolationBracesCompleterTest("#", "$", '$', false);
    myFixture.checkResult("$<caret>");
  }

  public void testThreeSymbolDelimiters() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>");
    doInterpolationBracesCompleterTest("{{{", "}}}", '{', false);
    myFixture.checkResult("{{{<caret>}}}");
  }

  public void testThreeSymbolDelimitersEndTypeOver() {
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{{text}}<caret>}");
    doInterpolationBracesCompleterTest("{{{", "}}}", '}', false);
    myFixture.checkResult("{{{text}}}<caret>");
  }

  private void doInterpolationBracesCompleterTest(@NotNull final String start,
                                                  @NotNull final String end,
                                                  final char typed, boolean addSpace) {
    CommandProcessor.getInstance().executeCommand(
      myFixture.getProject(),
      () -> WriteAction.run(
        () -> {
          JSCodeStyleSettings settings = JSCodeStyleSettings.getSettings(myFixture.getFile());
          boolean oldWhitespace = settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS;
          try {
            settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = addSpace;
            final TypedHandlerDelegate.Result result =
              new JSInjectionBracesUtil.InterpolationBracesCompleter(element -> Pair.create(start, end))
                .beforeCharTyped(typed, myFixture.getProject(), myFixture.getEditor(), myFixture.getFile());
            if (TypedHandlerDelegate.Result.CONTINUE.equals(result)) {
              myFixture.type(typed);
            }
          }
          finally {
            settings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = oldWhitespace;
          }
        }), null, null);
  }
}

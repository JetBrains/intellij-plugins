package org.angularjs.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.javascript.JSInjectionBracesUtil;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class AngularTypedHandlerTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @Test
  public void testBracketsClosing() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>");
    myFixture.type("{");
    myFixture.checkResult("{{<caret>}}");
  }

  @Test
  public void testBracketsNotClosingTwice() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "{<caret>}}");
    myFixture.type("{");
    myFixture.checkResult("{{<caret>}}");
  }

  @Test
  public void testBracketsNotBreakingAtEnd() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>");
    myFixture.type("}");
    myFixture.checkResult("{{}}<caret>");
  }

  @Test
  public void testClosingBracketsSkipped() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>}}");
    myFixture.type("}");
    myFixture.checkResult("{{}<caret>}");
  }

  @Test
  public void testSecondClosingBracket() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{}<caret>");
    myFixture.type("}");
    myFixture.checkResult("{{}}<caret>");
  }

  @Test
  public void testInsertWhitespace() {
    CodeInsightTestFixture myFixture = codeInsight.get();
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

  @Test
  public void testOneSymbolDelimiterStartCompletes() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "<caret>");
    doInterpolationBracesCompleterTest("$", "#", '$', true);
    myFixture.checkResult("$ <caret> #");
  }

  @Test
  public void testMixedDelimitersAlreadyHasEnding() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>#");
    doInterpolationBracesCompleterTest("$$", "#", '$', false);
    myFixture.checkResult("$$<caret>#");
  }

  @Test
  public void testMixedDelimitersCompletionNoStartTypeOver() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>$");
    doInterpolationBracesCompleterTest("$$", "#", '$', false);
    myFixture.checkResult("$$<caret>#$");
  }

  @Test
  public void testOneSymbolDelimiterEndAdded() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>");
    doInterpolationBracesCompleterTest("$", "$", '$', false);
    myFixture.checkResult("$$<caret>");
  }

  @Test
  public void testOneSymbolDelimiterTypeOver() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "$<caret>$");
    doInterpolationBracesCompleterTest("$", "$", '$', false);
    myFixture.checkResult("$$<caret>");
  }

  @Test
  public void testOneSymbolDelimiterTypeOverOneSymbol() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "#<caret>$");
    doInterpolationBracesCompleterTest("#", "$", '$', false);
    myFixture.checkResult("#$<caret>");
  }

  @Test
  public void testDoNotCompleteEndWithoutStart() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "abc {<caret>");
    doInterpolationBracesCompleterTest("{{", "}}", '}', false);
    myFixture.checkResult("abc {}<caret>");
  }

  @Test
  public void testThreeSymbolDelimiters() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{<caret>");
    doInterpolationBracesCompleterTest("{{{", "}}}", '{', false);
    myFixture.checkResult("{{{<caret>}}}");
  }

  @Test
  public void testThreeSymbolDelimitersEndTypeOver() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByText(HtmlFileType.INSTANCE, "{{{text}}<caret>}");
    doInterpolationBracesCompleterTest("{{{", "}}}", '}', false);
    myFixture.checkResult("{{{text}}}<caret>");
  }

  private void doInterpolationBracesCompleterTest(@NotNull final String start,
                                                  @NotNull final String end,
                                                  final char typed, boolean addSpace) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    EdtTestUtil.runInEdtAndWait(() -> CommandProcessor.getInstance().executeCommand(
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
        }), null, null));
  }
}

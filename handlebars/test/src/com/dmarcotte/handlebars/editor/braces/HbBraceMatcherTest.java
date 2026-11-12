package com.dmarcotte.handlebars.editor.braces;

import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class HbBraceMatcherTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  private static final String ourBraceMatchIndicator = "<brace_match>";

  /**
   * Expects "fileText" to have two "&lt;brace_match&gt;" tokens, placed in front of two braces which are
   * expected to be matched by the built-in brace matching (i.e. when the caret is at one of the &lt;brace_match&gt;
   * tokens, the brace match subsystem highlights the brace at the other &lt;brace_match&gt;)
   * <p/>
   * NOTE: the &lt;brace_match&gt; before you close brace should have a bit of whitespace before it to make this
   * test work correctly.  For example, have "{{/ foo <brace_match>}}" rather than "{{/foo<brace_match>}}"
   */
  private void doBraceTest(String fileText) {
    String textForTest = fileText;

    int firstBracePosition = textForTest.indexOf(ourBraceMatchIndicator);
    textForTest = textForTest.replaceFirst(ourBraceMatchIndicator, ""); // remove first brace from input

    int secondBracePosition = textForTest.indexOf(ourBraceMatchIndicator);
    textForTest = textForTest.replaceFirst(ourBraceMatchIndicator, ""); // remove second brace from input

    assertTrue(firstBracePosition > -1 && secondBracePosition > -1,
               "Should have two \"" + ourBraceMatchIndicator + "\" tokens in fileText.  Given fileText:\n"
               + fileText);

    String firstBraceResult = findMatchBraceForBraceAtCaret(textForTest, firstBracePosition, secondBracePosition);
    assertEquals(fileText, firstBraceResult, "Result with caret at first <brace_match>");
    String secondBraceResult = findMatchBraceForBraceAtCaret(textForTest, secondBracePosition, firstBracePosition);
    assertEquals(fileText, secondBraceResult, "Result with caret at second <brace_match>");
  }

  /**
   * Method to do the actual invocation of the brace match subsystem on a given file for a given caret position
   *
   * @param fileText                     the source to test brace matching on
   * @param caretPosition                caret position to compute a matched brace for
   * @param expectedMatchedBracePosition the expected position of the brace which matches the brace at caretPosition
   * @return the given file text with the {link #ourBraceMatchIndicator} tokens place where
   *         the the brace matching subsystem dictated
   */
  private String findMatchBraceForBraceAtCaret(String fileText, int caretPosition, int expectedMatchedBracePosition) {
    CodeInsightTestFixture myFixture = fixture.get();

    String caretIndicator = "<caret>";
    String textWithCaret = new StringBuilder(fileText).insert(caretPosition, caretIndicator).toString();

    myFixture.configureByText(HbFileType.INSTANCE, textWithCaret);

    boolean caretFirst = expectedMatchedBracePosition > caretPosition;
    int actualBraceMatchPosition
      = ReadAction.computeBlocking(() -> BraceMatchingUtil.getMatchedBraceOffset(myFixture.getEditor(),
                                                                                 caretFirst,
                                                                                 myFixture.getFile()));

    // we want to have an easy to read result, so we insert a <brace_match> where
    // BraceMatchingUtil.getMatchedBraceOffset told us it should go.
    String result = new StringBuilder(textWithCaret)
      // note that we need to compensate for the length of the caretIndicator if it comes before the ourBraceMatchIndicator
      .insert(actualBraceMatchPosition + (caretFirst ? caretIndicator.length() : 0), ourBraceMatchIndicator)
      .toString();

    // replace the caret indicator with a ourBraceMatchIndicator so that our result format matches our input format
    result = result.replace(caretIndicator, ourBraceMatchIndicator);

    return result;
  }

  /**
   * Convenience property for quickly setting up brace match tests.
   * <p/>
   * Things to note about this text:
   * - The braces we want to match have some whitespace around them (this lets them match when the caret is before them)
   * - All mustache ids (foo, foo2, bar, etc) are unique so that they can be easily targeted
   * by string replace functions.
   */
  private static final String ourTestSource =
    """
      {{# foo1 }}
          {{ bar }}
          {{^ }}
          {{# foo2 }}
              <div>
                  {{^ foo3 }}
                      Content
                  {{/ foo3 }}
              </div>
              {{{ baz }}}
              {{ bat }}
              {{> partial }}
          {{/ foo2 }}
      {{/ foo1 }}

      {{^ foo4 }}
          Content
      {{/ foo4 }}
      """;

  @Test
  public void testSimpleMustache() {
    doBraceTest(
      ourTestSource.replace("{{ bar }}", "<brace_match>{{ bar }}")
        .replace("{{ bar }}", "{{ bar <brace_match>}}")
    );
  }

  @Test
  public void testUnEscapedMustache() {
    doBraceTest(
      ourTestSource.replace("{{{ baz }}}", "<brace_match>{{{ baz }}}")
        .replace("{{{ baz }}}", "{{{ baz <brace_match>}}}")
    );
  }

  @Test
  public void testPartial() {
    doBraceTest(
      ourTestSource.replace("{{> partial }}", "<brace_match>{{> partial }}")
        .replace("{{> partial }}", "{{> partial <brace_match>}}")
    );
  }

  @Test
  public void testBlockMustache() {
    doBraceTest(
      ourTestSource.replace("{{# foo1 }}", "<brace_match>{{# foo1 }}")
        .replace("{{/ foo1 }}", "{{/ foo1 <brace_match>}}")
    );
  }

  @Test
  public void testInverseBlockMustache() {
    doBraceTest(
      ourTestSource.replace("{{^ foo4 }}", "<brace_match>{{^ foo4 }}")
        .replace("{{/ foo4 }}", "{{/ foo4 <brace_match>}}")
    );
  }

  @Test
  public void testSimpleInverseMustache() {
    doBraceTest(
      ourTestSource.replace("{{^ }}", "<brace_match>{{^ }}")
        .replace("{{^ }}", "{{^ <brace_match>}}")
    );
  }

  @Test
  public void testNestedBlockStache() {
    doBraceTest(
      ourTestSource.replace("{{# foo2 }}", "<brace_match>{{# foo2 }}")
        .replace("{{/ foo2 }}", "{{/ foo2 <brace_match>}}")
    );
  }

  @Test
  public void testInverseBlockStache() {
    doBraceTest(
      ourTestSource.replace("{{^ foo3 }}", "<brace_match>{{^ foo3 }}")
        .replace("{{/ foo3 }}", "{{/ foo3 <brace_match>}}")
    );
  }
}

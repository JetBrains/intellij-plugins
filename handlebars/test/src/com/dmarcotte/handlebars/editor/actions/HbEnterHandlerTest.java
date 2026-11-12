package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.dmarcotte.handlebars.editor.actions.HbActionHandlerTestUtil.doEnterTest;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class HbEnterHandlerTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  private boolean myPrevFormatSetting;

  @BeforeEach
  void setUp() {
    // disable the formatter for these tests
    myPrevFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);
  }

  @AfterEach
  void tearDown() {
    HbConfig.setFormattingEnabled(myPrevFormatSetting);
  }

  /**
   * On Enter between matching open/close tags,
   * expect an extra newline to be inserted with the caret placed
   * between the tags
   */
  @Test
  public void testEnterBetweenMatchingHbTags() {
    doEnterTest(fixture.get(),

      "{{#foo}}<caret>{{/foo}}",

      """
        {{#foo}}
        <caret>
        {{/foo}}"""
    );
  }

  /**
   * On Enter between MIS-matched open/close tags,
   * we still get the standard behavior
   */
  @Test
  public void testEnterBetweenMismatchedHbTags() {
    doEnterTest(fixture.get(),

      "{{#foo}}<caret>{{/bar}}" +
      "stuff",

      """
        {{#foo}}
        <caret>
        {{/bar}}stuff"""
    );
  }

  /**
   * On Enter at an open tag with no close tag,
   * expect a standard newline
   * (Notice that we have "other stuff" our test string.  When the caret is at the file
   * boundary, it's actually a special case.  See {@link #testEnterAtOpenTagOnFileBoundary}
   */
  @Test
  public void testEnterAtOpenTag() {
    doEnterTest(fixture.get(),

      "{{#foo}}<caret>" +
      "other stuff",

      "{{#foo}}\n" +
      "<caret>" +
      "other stuff"

    );
  }

  /**
   * On Enter at an open tag with no close tag,
   * expect a standard newline.
   * <p/>
   * Note: this used to result in an error.  The was a bug where we checked beyond the
   * end of the file for a close tag to go with this open tag.
   */
  @Test
  public void testEnterAtOpenTagOnFileBoundary() {
    doEnterTest(fixture.get(),

      "{{#foo}}<caret>",

      "{{#foo}}\n" +
      "<caret>"
    );
  }

  @Test
  public void testTagAttributeIndent() {
    HbConfig.setFormattingEnabled(true);
    doEnterTest(fixture.get(),
      """
        {{#ffo fooo="1"<caret>
        }}

        {{/ffo}}""",
      """
        {{#ffo fooo="1"
               <caret>
        }}

        {{/ffo}}""");
  }
}

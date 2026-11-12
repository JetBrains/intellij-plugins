package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.dmarcotte.handlebars.editor.actions.HbActionHandlerTestUtil.doBlockCommentTest;
import static com.dmarcotte.handlebars.editor.actions.HbActionHandlerTestUtil.doLineCommentTest;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class HbCommentActionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  private Language myPrevCommenterLang;

  @BeforeEach
  void setUp() {
    myPrevCommenterLang = HbConfig.getCommenterLanguage();

    // ensure that no commenter is selected to make sure that we test defaulting to HTML comments
    HbConfig.setCommenterLanguage(null);
  }

  @Test
  public void testInsertLineComment1() {
    doLineCommentTest(fixture.get(),

      "{{#foo}}<caret>",

      "<!--{{#foo}}<caret>-->"
    );
  }

  @Test
  public void testInsertLineComment2() {
    doLineCommentTest(fixture.get(),

      """
        {{#foo}}
        <caret>    {{bar}}
        {{/foo}}""",

      """
        {{#foo}}
        <!--    {{bar}}-->
        <caret>{{/foo}}"""
    );
  }

  @Test
  public void testInsertBlockComment1() {
    doBlockCommentTest(fixture.get(),

      "{{#foo}}<caret>",

      "{{#foo}}<!--<caret>-->"
    );
  }

  @Test
  public void testInsertBlockComment2() {
    doBlockCommentTest(fixture.get(),

      """
        {{#foo}}
            <caret>{{bar}}
        {{/foo}""",

      """
        {{#foo}}
            <!--<caret>-->{{bar}}
        {{/foo}"""
    );
  }

  @Test
  public void testInsertBlockCommentWithSelection() {
    doBlockCommentTest(fixture.get(),

      "<selection><caret>{{#foo}}" +
      "    {{bar}}</selection>" +
      "{{/foo}",

      "<selection><!--<caret>{{#foo}}" +
      "    {{bar}}--></selection>" +
      "{{/foo}"
    );
  }

  @Test
  public void testInsertNonDefaultLineComment() {
    Language prevCommenterLanguage = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(JavascriptLanguage.INSTANCE);

    doLineCommentTest(fixture.get(),

      "{{#foo}}<caret>",

      "//{{#foo}}<caret>"
    );

    HbConfig.setCommenterLanguage(prevCommenterLanguage);
  }

  @Test
  public void testInsertNonDefaultBlockComment() {
    Language prevCommenterLanguage = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(JavascriptLanguage.INSTANCE);

    doBlockCommentTest(fixture.get(),

      "{{#foo}}<caret>",

      "{{#foo}}/*<caret>*/"
    );

    HbConfig.setCommenterLanguage(prevCommenterLanguage);
  }

  @Test
  public void testNativeInsertLineComment1() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doLineCommentTest(fixture.get(),

      "{{#foo}}<caret>",

      "{{!--{{#foo}}<caret>--}}"
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  @Test
  public void testNativeInsertLineComment2() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doLineCommentTest(fixture.get(),

      """
        {{#foo}}
        <caret>    {{bar}}
        {{/foo}}""",

      """
        {{#foo}}
        {{!--    {{bar}}--}}
        <caret>{{/foo}}"""
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  @Test
  public void testNativeInsertBlockComment1() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doBlockCommentTest(fixture.get(),

      "{{#foo}}<caret>",

      "{{#foo}}{{!--<caret>--}}"
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  @Test
  public void testNativeInsertBlockComment2() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doBlockCommentTest(fixture.get(),

      """
        {{#foo}}
            <caret>{{bar}}
        {{/foo}""",

      """
        {{#foo}}
            {{!--<caret>--}}{{bar}}
        {{/foo}"""
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  @Test
  public void testNativeInsertBlockCommentWithSelection() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doBlockCommentTest(fixture.get(),

      "<selection><caret>{{#foo}}" +
      "    {{bar}}</selection>" +
      "{{/foo}",

      "<selection>{{!--<caret>{{#foo}}" +
      "    {{bar}}--}}</selection>" +
      "{{/foo}"
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  @AfterEach
  void tearDown() {
    HbConfig.setCommenterLanguage(myPrevCommenterLang);
  }
}

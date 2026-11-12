// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.format.FormatterTestSettings;
import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.dmarcotte.handlebars.editor.actions.HbActionHandlerTestUtil.doCharTest;
import static com.dmarcotte.handlebars.editor.actions.HbActionHandlerTestUtil.doEnterTest;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

/**
 * These tests are based on other children of {@code BasePlatformTestCase},
 * in particular {@code com.intellij.application.options.codeInsight.editor.quotes.SelectionQuotingTypedHandlerTest}
 */
@TestApplication
public class HbTypedHandlerTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private boolean myPrevAutoCloseSetting;
  private FormatterTestSettings formatterTestSettings;

  @BeforeEach
  void setUp() {
    myPrevAutoCloseSetting = HbConfig.isAutoGenerateCloseTagEnabled();
    HbConfig.setAutoGenerateCloseTagEnabled(true);

    formatterTestSettings = new FormatterTestSettings(CodeStyle.getSettings(fixture.get().getProject()));
    formatterTestSettings.setUp();
  }

  @AfterEach
  void tearDown() {
    HbConfig.setAutoGenerateCloseTagEnabled(myPrevAutoCloseSetting);
    if (formatterTestSettings != null) {
      formatterTestSettings.tearDown();
    }
  }

  /**
   * Sanity check that we do nothing when something other than "}" completes a stache
   */
  @Test
  public void testNonStacheClosingCharacter() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), 'X', "{{#foo}<caret>", "{{#foo}X<caret>");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest(fixture.get(), 'X', "{{#foo}<caret>", "{{#foo}X<caret>");
  }

  @Test
  public void testCloseDoubleBraces() {
    HbConfig.setAutocompleteMustachesEnabled(false);
    doCharTest(fixture.get(), '}', "foo {{bar<caret>", "foo {{bar}<caret>");
    doCharTest(fixture.get(), '}', "foo {{&bar<caret>", "foo {{&bar}<caret>");
    doCharTest(fixture.get(), '}', "foo {{/bar<caret>", "foo {{/bar}<caret>");

    HbConfig.setAutocompleteMustachesEnabled(true);
    doCharTest(fixture.get(), '}', "foo {{bar<caret>", "foo {{bar}}<caret>");
    doCharTest(fixture.get(), '}', "foo {{&bar<caret>", "foo {{&bar}}<caret>");
    doCharTest(fixture.get(), '}', "foo {{/bar<caret>", "foo {{/bar}}<caret>");

    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "foo {{#bar<caret>", "foo {{#bar}}<caret>{{/bar}}");
    doCharTest(fixture.get(), '}', "foo {{^bar<caret>", "foo {{^bar}}<caret>{{/bar}}");
    doCharTest(fixture.get(), '}', "foo {{#bar}}{{#baz<caret>{{/bar}}", "foo {{#bar}}{{#baz}}<caret>{{/baz}}{{/bar}}");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest(fixture.get(), '}', "foo {{#bar<caret>", "foo {{#bar}}<caret>");
    doCharTest(fixture.get(), '}', "foo {{^bar<caret>", "foo {{^bar}}<caret>");
    doCharTest(fixture.get(), '}', "foo {{#bar}}{{#baz<caret>{{/bar}}", "foo {{#bar}}{{#baz}}<caret>{{/bar}}");
  }

  @Test
  public void testCloseTripleBraces() {
    HbConfig.setAutocompleteMustachesEnabled(false);
    doCharTest(fixture.get(), '}', "foo {{{bar<caret>", "foo {{{bar}<caret>");

    HbConfig.setAutocompleteMustachesEnabled(true);
    doCharTest(fixture.get(), '}', "foo {{{bar<caret>", "foo {{{bar}}}<caret>");
  }

  @Test
  public void testInsertCloseTagForOpenBlockStache() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "{{#foo}<caret>", "{{#foo}}<caret>{{/foo}}");
    doCharTest(fixture.get(), '}', "{{#foo bar baz}<caret>", "{{#foo bar baz}}<caret>{{/foo}}");
    doCharTest(fixture.get(), '}', "{{#foo bar baz bat=\"bam\"}<caret>", "{{#foo bar baz bat=\"bam\"}}<caret>{{/foo}}");

    // test when caret is not at file boundary
    doCharTest(fixture.get(), '}', "{{#foo}<caret>some\nother content", "{{#foo}}<caret>{{/foo}}some\nother content");
    doCharTest(fixture.get(), '}', "{{#foo bar baz}<caret>some\nother content", "{{#foo bar baz}}<caret>{{/foo}}some\nother content");
    doCharTest(fixture.get(), '}', "{{#foo bar baz bat=\"bam\"}<caret>some\nother content",
               "{{#foo bar baz bat=\"bam\"}}<caret>{{/foo}}some\nother content");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest(fixture.get(), '}', "{{#foo}<caret>", "{{#foo}}<caret>");
    doCharTest(fixture.get(), '}', "{{#foo bar baz}<caret>", "{{#foo bar baz}}<caret>");
    doCharTest(fixture.get(), '}', "{{#foo bar baz bat=\"bam\"}<caret>", "{{#foo bar baz bat=\"bam\"}}<caret>");
  }

  @Test
  public void testInsertCloseTagForNestedBlocks() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "{{#foo}}{{#bar}<caret>{{/foo}}", "{{#foo}}{{#bar}}<caret>{{/bar}}{{/foo}}");
  }

  @Test
  public void testInsertCloseTagForOpenInverseStache() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "{{^foo}<caret>", "{{^foo}}<caret>{{/foo}}");
    doCharTest(fixture.get(), '}', "{{^foo bar baz}<caret>", "{{^foo bar baz}}<caret>{{/foo}}");
    doCharTest(fixture.get(), '}', "{{^foo bar baz bat=\"bam\"}<caret>", "{{^foo bar baz bat=\"bam\"}}<caret>{{/foo}}");

    // test when caret is not at file boundary
    doCharTest(fixture.get(), '}', "{{^foo}<caret>some\nother content", "{{^foo}}<caret>{{/foo}}some\nother content");
    doCharTest(fixture.get(), '}', "{{^foo bar baz}<caret>some\nother content", "{{^foo bar baz}}<caret>{{/foo}}some\nother content");
    doCharTest(fixture.get(), '}', "{{^foo bar baz bat=\"bam\"}<caret>some\nother content",
               "{{^foo bar baz bat=\"bam\"}}<caret>{{/foo}}some\nother content");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest(fixture.get(), '}', "{{^foo}<caret>", "{{^foo}}<caret>");
    doCharTest(fixture.get(), '}', "{{^foo bar baz}<caret>", "{{^foo bar baz}}<caret>");
    doCharTest(fixture.get(), '}', "{{^foo bar baz bat=\"bam\"}<caret>", "{{^foo bar baz bat=\"bam\"}}<caret>");
  }

  @Test
  public void testInsertCloseTagWithWhitespace() {
    // ensure that we properly identify the "foo" even if there's whitespace between it and the open tag
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "{{#   foo   }<caret>", "{{#   foo   }}<caret>{{/foo}}");
  }

  @Test
  public void testInsertCloseTagForComplexIds() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "{{#foo.bar}<caret>", "{{#foo.bar}}<caret>{{/foo.bar}}");
    doCharTest(fixture.get(), '}', "{{#foo.bar.[baz bat]}<caret>", "{{#foo.bar.[baz bat]}}<caret>{{/foo.bar.[baz bat]}}");
  }

  @Test
  public void testNoInsertCloseTagForExtraStaches() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "{{#foo}}<caret>", "{{#foo}}}<caret>");
  }

  @Test
  public void testRegularStache() {
    // ensure that nothing special happens for regular 'staches, whether autoGenerateCloseTag is enabled or not

    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "{{foo}<caret>", "{{foo}}<caret>");
    doCharTest(fixture.get(), '}', "{{foo bar baz}<caret>", "{{foo bar baz}}<caret>");

    // test when caret is not at file boundary
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "{{foo}<caret>some\nother stuff", "{{foo}}<caret>some\nother stuff");
    doCharTest(fixture.get(), '}', "{{foo bar baz}<caret>some\nother stuff", "{{foo bar baz}}<caret>some\nother stuff");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest(fixture.get(), '}', "{{foo}<caret>", "{{foo}}<caret>");
    doCharTest(fixture.get(), '}', "{{foo bar baz}<caret>", "{{foo bar baz}}<caret>");
  }

  /**
   * Our typed handler relies on looking a couple of characters back
   * make sure we're well behaved when there are none.
   */
  @Test
  public void testFirstCharTyped() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '}', "<caret>", "}<caret>");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest(fixture.get(), '}', "<caret>", "}<caret>");
  }

  /**
   * Ensure that IDEA does not provide any automatic "}" insertion
   */
  @Test
  public void testSuppressNativeBracketInsert() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest(fixture.get(), '{', "<caret>", "{<caret>");
    doCharTest(fixture.get(), '{', "{<caret>", "{{<caret>");
    doCharTest(fixture.get(), '{', "{{<caret>", "{{{<caret>");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest(fixture.get(), '{', "<caret>", "{<caret>");
    doCharTest(fixture.get(), '{', "{<caret>", "{{<caret>");
    doCharTest(fixture.get(), '{', "{{<caret>", "{{{<caret>");
  }

  @Test
  public void testFormatOnCloseBlockCompleted1() {
    doCharTest(fixture.get(), '}',

               """
                 {{#foo}}
                     stuff
                     {{/foo}<caret>""",

               """
                 {{#foo}}
                     stuff
                 {{/foo}}<caret>""");
  }

  @Test
  public void testFormatOnCloseBlockCompleted2() {
    doCharTest(fixture.get(), '}',

               """
                 {{#foo}}
                     stuff
                     {{/foo}<caret> other stuff""",

               """
                 {{#foo}}
                     stuff
                 {{/foo}}<caret> other stuff""");
  }

  @Test
  public void testFormatOnCloseBlockCompleted3() {
    doCharTest(fixture.get(), '}',

               """
                 {{#foo}}
                     stuff
                     {{/foo}<caret>
                 other stuff""",

               """
                 {{#foo}}
                     stuff
                 {{/foo}}<caret>
                 other stuff""");
  }

  @Test
  public void testFormatDisabledCloseBlockCompleted() {
    boolean previousFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doCharTest(fixture.get(), '}',

               """
                 {{#foo}}
                     stuff
                     {{/foo}<caret>""",

               """
                 {{#foo}}
                     stuff
                     {{/foo}}<caret>""");

    HbConfig.setFormattingEnabled(previousFormatSetting);
  }

  @Test
  public void testFormatOnSimpleInverseCompleted1() {
    doCharTest(fixture.get(), '}',

               """
                 {{#if}}
                     if stuff
                     {{else}<caret>""",

               """
                 {{#if}}
                     if stuff
                 {{else}}<caret>""");
  }

  @Test
  public void testFormatOnSimpleInverseCompleted2() {
    doCharTest(fixture.get(), '}',

               """
                 {{#if}}
                     if stuff
                     {{else}<caret> other stuff""",

               """
                 {{#if}}
                     if stuff
                 {{else}}<caret> other stuff""");
  }

  @Test
  public void testFormatOnSimpleInverseCompleted3() {
    doCharTest(fixture.get(), '}',

               """
                 {{#if}}
                     if stuff
                     {{else}<caret>
                 other stuff""",

               """
                 {{#if}}
                     if stuff
                 {{else}}<caret>
                 other stuff""");
  }

  @Test
  public void testFormatDisabledSimpleInverseCompleted() {
    boolean previousFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doCharTest(fixture.get(), '}',

               """
                 {{#if}}
                     if stuff
                     {{else}<caret>""",

               """
                 {{#if}}
                     if stuff
                     {{else}}<caret>""");

    HbConfig.setFormattingEnabled(previousFormatSetting);
  }

  @Test
  public void testEnterBetweenBlockTags() {
    doEnterTest(fixture.get(),

      "{{#foo}}<caret>{{/foo}}",

      """
        {{#foo}}
            <caret>
        {{/foo}}"""
    );
  }

  @Test
  public void testFormatterDisabledEnterBetweenBlockTags() {
    boolean previousFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doEnterTest(fixture.get(),

      "{{#foo}}<caret>{{/foo}}",

      """
        {{#foo}}
        <caret>
        {{/foo}}"""
    );

    HbConfig.setFormattingEnabled(previousFormatSetting);
  }

  @Test
  public void testEnterNotBetweenBlockTags() {
    doEnterTest(fixture.get(),

      "{{foo}}<caret>{{foo}}",

      "{{foo}}\n" +
      "<caret>{{foo}}"
    );
  }

  @Test
  public void testFinishingClosingTag() {
    doCharTest(fixture.get(),
      '/',
      """
        <div class="entry">
            {{#if}}test{{<caret>
        </div>""",

      """
        <div class="entry">
            {{#if}}test{{/if}}<caret>
        </div>"""
    );

    doCharTest(fixture.get(),
      '/',
      """
        <div class="entry">
            {{#if}}test{<caret>
        </div>""",

      """
        <div class="entry">
            {{#if}}test{{/if}}<caret>
        </div>"""
    );
  }
}
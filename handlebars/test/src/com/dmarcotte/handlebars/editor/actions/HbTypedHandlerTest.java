// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.format.FormatterTestSettings;
import com.intellij.application.options.CodeStyle;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * These tests are based on other children of {@link BasePlatformTestCase},
 * in particular {@code com.intellij.application.options.codeInsight.editor.quotes.SelectionQuotingTypedHandlerTest}
 */
public class HbTypedHandlerTest extends HbActionHandlerTest {
  private boolean myPrevAutoCloseSetting;
  private FormatterTestSettings formatterTestSettings;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myPrevAutoCloseSetting = HbConfig.isAutoGenerateCloseTagEnabled();
    HbConfig.setAutoGenerateCloseTagEnabled(true);

    formatterTestSettings = new FormatterTestSettings(CodeStyle.getSettings(getProject()));
    formatterTestSettings.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      HbConfig.setAutoGenerateCloseTagEnabled(myPrevAutoCloseSetting);
      if (formatterTestSettings != null) {
        formatterTestSettings.tearDown();
      }
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  /**
   * Sanity check that we do nothing when something other than "}" completes a stache
   */
  public void testNonStacheClosingCharacter() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('X', "{{#foo}<caret>", "{{#foo}X<caret>");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest('X', "{{#foo}<caret>", "{{#foo}X<caret>");
  }

  public void testCloseDoubleBraces() {
    HbConfig.setAutocompleteMustachesEnabled(false);
    doCharTest('}', "foo {{bar<caret>", "foo {{bar}<caret>");
    doCharTest('}', "foo {{&bar<caret>", "foo {{&bar}<caret>");
    doCharTest('}', "foo {{/bar<caret>", "foo {{/bar}<caret>");

    HbConfig.setAutocompleteMustachesEnabled(true);
    doCharTest('}', "foo {{bar<caret>", "foo {{bar}}<caret>");
    doCharTest('}', "foo {{&bar<caret>", "foo {{&bar}}<caret>");
    doCharTest('}', "foo {{/bar<caret>", "foo {{/bar}}<caret>");

    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "foo {{#bar<caret>", "foo {{#bar}}<caret>{{/bar}}");
    doCharTest('}', "foo {{^bar<caret>", "foo {{^bar}}<caret>{{/bar}}");
    doCharTest('}', "foo {{#bar}}{{#baz<caret>{{/bar}}", "foo {{#bar}}{{#baz}}<caret>{{/baz}}{{/bar}}");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest('}', "foo {{#bar<caret>", "foo {{#bar}}<caret>");
    doCharTest('}', "foo {{^bar<caret>", "foo {{^bar}}<caret>");
    doCharTest('}', "foo {{#bar}}{{#baz<caret>{{/bar}}", "foo {{#bar}}{{#baz}}<caret>{{/bar}}");
  }

  public void testCloseTripleBraces() {
    HbConfig.setAutocompleteMustachesEnabled(false);
    doCharTest('}', "foo {{{bar<caret>", "foo {{{bar}<caret>");

    HbConfig.setAutocompleteMustachesEnabled(true);
    doCharTest('}', "foo {{{bar<caret>", "foo {{{bar}}}<caret>");
  }

  public void testInsertCloseTagForOpenBlockStache() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "{{#foo}<caret>", "{{#foo}}<caret>{{/foo}}");
    doCharTest('}', "{{#foo bar baz}<caret>", "{{#foo bar baz}}<caret>{{/foo}}");
    doCharTest('}', "{{#foo bar baz bat=\"bam\"}<caret>", "{{#foo bar baz bat=\"bam\"}}<caret>{{/foo}}");

    // test when caret is not at file boundary
    doCharTest('}', "{{#foo}<caret>some\nother content", "{{#foo}}<caret>{{/foo}}some\nother content");
    doCharTest('}', "{{#foo bar baz}<caret>some\nother content", "{{#foo bar baz}}<caret>{{/foo}}some\nother content");
    doCharTest('}', "{{#foo bar baz bat=\"bam\"}<caret>some\nother content",
               "{{#foo bar baz bat=\"bam\"}}<caret>{{/foo}}some\nother content");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest('}', "{{#foo}<caret>", "{{#foo}}<caret>");
    doCharTest('}', "{{#foo bar baz}<caret>", "{{#foo bar baz}}<caret>");
    doCharTest('}', "{{#foo bar baz bat=\"bam\"}<caret>", "{{#foo bar baz bat=\"bam\"}}<caret>");
  }

  public void testInsertCloseTagForNestedBlocks() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "{{#foo}}{{#bar}<caret>{{/foo}}", "{{#foo}}{{#bar}}<caret>{{/bar}}{{/foo}}");
  }

  public void testInsertCloseTagForOpenInverseStache() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "{{^foo}<caret>", "{{^foo}}<caret>{{/foo}}");
    doCharTest('}', "{{^foo bar baz}<caret>", "{{^foo bar baz}}<caret>{{/foo}}");
    doCharTest('}', "{{^foo bar baz bat=\"bam\"}<caret>", "{{^foo bar baz bat=\"bam\"}}<caret>{{/foo}}");

    // test when caret is not at file boundary
    doCharTest('}', "{{^foo}<caret>some\nother content", "{{^foo}}<caret>{{/foo}}some\nother content");
    doCharTest('}', "{{^foo bar baz}<caret>some\nother content", "{{^foo bar baz}}<caret>{{/foo}}some\nother content");
    doCharTest('}', "{{^foo bar baz bat=\"bam\"}<caret>some\nother content",
               "{{^foo bar baz bat=\"bam\"}}<caret>{{/foo}}some\nother content");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest('}', "{{^foo}<caret>", "{{^foo}}<caret>");
    doCharTest('}', "{{^foo bar baz}<caret>", "{{^foo bar baz}}<caret>");
    doCharTest('}', "{{^foo bar baz bat=\"bam\"}<caret>", "{{^foo bar baz bat=\"bam\"}}<caret>");
  }

  public void testInsertCloseTagWithWhitespace() {
    // ensure that we properly identify the "foo" even if there's whitespace between it and the open tag
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "{{#   foo   }<caret>", "{{#   foo   }}<caret>{{/foo}}");
  }

  public void testInsertCloseTagForComplexIds() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "{{#foo.bar}<caret>", "{{#foo.bar}}<caret>{{/foo.bar}}");
    doCharTest('}', "{{#foo.bar.[baz bat]}<caret>", "{{#foo.bar.[baz bat]}}<caret>{{/foo.bar.[baz bat]}}");
  }

  public void testNoInsertCloseTagForExtraStaches() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "{{#foo}}<caret>", "{{#foo}}}<caret>");
  }

  public void testRegularStache() {
    // ensure that nothing special happens for regular 'staches, whether autoGenerateCloseTag is enabled or not

    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "{{foo}<caret>", "{{foo}}<caret>");
    doCharTest('}', "{{foo bar baz}<caret>", "{{foo bar baz}}<caret>");

    // test when caret is not at file boundary
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "{{foo}<caret>some\nother stuff", "{{foo}}<caret>some\nother stuff");
    doCharTest('}', "{{foo bar baz}<caret>some\nother stuff", "{{foo bar baz}}<caret>some\nother stuff");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest('}', "{{foo}<caret>", "{{foo}}<caret>");
    doCharTest('}', "{{foo bar baz}<caret>", "{{foo bar baz}}<caret>");
  }

  /**
   * Our typed handler relies on looking a couple of characters back
   * make sure we're well behaved when there are none.
   */
  public void testFirstCharTyped() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('}', "<caret>", "}<caret>");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest('}', "<caret>", "}<caret>");
  }

  /**
   * Ensure that IDEA does not provide any automatic "}" insertion
   */
  public void testSuppressNativeBracketInsert() {
    HbConfig.setAutoGenerateCloseTagEnabled(true);
    doCharTest('{', "<caret>", "{<caret>");
    doCharTest('{', "{<caret>", "{{<caret>");
    doCharTest('{', "{{<caret>", "{{{<caret>");

    HbConfig.setAutoGenerateCloseTagEnabled(false);
    doCharTest('{', "<caret>", "{<caret>");
    doCharTest('{', "{<caret>", "{{<caret>");
    doCharTest('{', "{{<caret>", "{{{<caret>");
  }

  public void testFormatOnCloseBlockCompleted1() {
    doCharTest('}',

               """
                 {{#foo}}
                     stuff
                     {{/foo}<caret>""",

               """
                 {{#foo}}
                     stuff
                 {{/foo}}<caret>""");
  }

  public void testFormatOnCloseBlockCompleted2() {
    doCharTest('}',

               """
                 {{#foo}}
                     stuff
                     {{/foo}<caret> other stuff""",

               """
                 {{#foo}}
                     stuff
                 {{/foo}}<caret> other stuff""");
  }

  public void testFormatOnCloseBlockCompleted3() {
    doCharTest('}',

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

  public void testFormatDisabledCloseBlockCompleted() {
    boolean previousFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doCharTest('}',

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

  public void testFormatOnSimpleInverseCompleted1() {
    doCharTest('}',

               """
                 {{#if}}
                     if stuff
                     {{else}<caret>""",

               """
                 {{#if}}
                     if stuff
                 {{else}}<caret>""");
  }

  public void testFormatOnSimpleInverseCompleted2() {
    doCharTest('}',

               """
                 {{#if}}
                     if stuff
                     {{else}<caret> other stuff""",

               """
                 {{#if}}
                     if stuff
                 {{else}}<caret> other stuff""");
  }

  public void testFormatOnSimpleInverseCompleted3() {
    doCharTest('}',

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

  public void testFormatDisabledSimpleInverseCompleted() {
    boolean previousFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doCharTest('}',

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

  public void testEnterBetweenBlockTags() {
    doEnterTest(

      "{{#foo}}<caret>{{/foo}}",

      """
        {{#foo}}
            <caret>
        {{/foo}}"""
    );
  }

  public void testFormatterDisabledEnterBetweenBlockTags() {
    boolean previousFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doEnterTest(

      "{{#foo}}<caret>{{/foo}}",

      """
        {{#foo}}
        <caret>
        {{/foo}}"""
    );

    HbConfig.setFormattingEnabled(previousFormatSetting);
  }

  public void testEnterNotBetweenBlockTags() {
    doEnterTest(

      "{{foo}}<caret>{{foo}}",

      "{{foo}}\n" +
      "<caret>{{foo}}"
    );
  }

  public void testFinishingClosingTag() {
    doCharTest(
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

    doCharTest(
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
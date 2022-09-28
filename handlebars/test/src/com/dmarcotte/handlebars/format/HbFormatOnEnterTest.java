package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.editor.actions.HbActionHandlerTest;

public class HbFormatOnEnterTest extends HbActionHandlerTest implements HbFormattingModelBuilderTest {

  private boolean myPrevFormatSetting;

  @Override
  protected void setUp()
    throws Exception {
    super.setUp();

    myPrevFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(true);
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      HbConfig.setFormattingEnabled(myPrevFormatSetting);
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  /**
   * This sanity check should be enough to ensure that we don't format on Enter
   * when the formatter is disabled
   */
  public void testEnterWithFormatterDisabled() {
    boolean previousFormatterSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doEnterTest(

      "{{#foo}}<caret>",

      "{{#foo}}\n" +
      "<caret>"
    );

    HbConfig.setFormattingEnabled(previousFormatterSetting);
  }

  public void testSimpleStache() {
    doEnterTest(

      "{{foo}}<caret>",

      "{{foo}}\n" +
      "<caret>"
    );
  }

  public void testSimpleBlock1() {
    doEnterTest(

      "{{#foo}}<caret>",

      "{{#foo}}\n" +
      "    <caret>"
    );
  }

  public void testSimpleBlock2() {
    doEnterTest(

      "{{#foo}}\n" +
      "    {{bar}}<caret>htmlPadding",

      """
        {{#foo}}
            {{bar}}
            <caret>htmlPadding"""
    );
  }

  public void testSimpleBlock3() {
    doEnterTest(
      """
        {{#foo}}
            {{bar}}<caret>
        {{/foo}}
        """,

      """
        {{#foo}}
            {{bar}}
            <caret>
        {{/foo}}
        """);
  }

  public void testNestedBlocks1() {
    doEnterTest(

      """
        {{#foo}}
        {{#bar}}
        {{#bat}}<caret>
        {{baz}}
        {{/bat}}
        {{/bar}}
        {{/foo}}""",

      """
        {{#foo}}
        {{#bar}}
        {{#bat}}
            <caret>
        {{baz}}
        {{/bat}}
        {{/bar}}
        {{/foo}}"""
    );
  }

  public void testNestedBlocks2() {
    doEnterTest(

      """
        {{#foo}}
            {{#bar}}
                {{#bat}}<caret>
                    {{baz}}
                {{/bat}}
            {{/bar}}
        {{/foo}}""",

      """
        {{#foo}}
            {{#bar}}
                {{#bat}}
                    <caret>
                    {{baz}}
                {{/bat}}
            {{/bar}}
        {{/foo}}"""
    );
  }

  public void testNestedBlocks3() {
    doEnterTest(

      """
        {{#foo}}
            {{#bar}}
                {{#bat}}
                    {{baz}}<caret>
                {{/bat}}
            {{/bar}}
        {{/foo}}""",

      """
        {{#foo}}
            {{#bar}}
                {{#bat}}
                    {{baz}}
                    <caret>
                {{/bat}}
            {{/bar}}
        {{/foo}}"""
    );
  }

  public void testSimpleStacheInDiv1() {
    doEnterTest(

      """
        <div><caret>
            {{foo}}
        </div>""",

      """
        <div>
            <caret>
            {{foo}}
        </div>"""
    );
  }

  public void testSimpleStacheInDiv2() {
    doEnterTest(

      """
        <div>
            {{foo}}<caret>
        </div>""",

      """
        <div>
            {{foo}}
            <caret>
        </div>"""
    );
  }

  public void testSimpleStacheInDiv3() {
    doEnterTest(

      "<div>\n" +
      "    {{foo}}<caret>",

      """
        <div>
            {{foo}}
            <caret>"""
    );
  }

  public void testMarkupInBlockStache1() {
    doEnterTest(

      """
        {{#foo}}
            <span></span><caret>
        {{/foo}}""",

      """
        {{#foo}}
            <span></span>
            <caret>
        {{/foo}}"""
    );
  }

  public void testMarkupInBlockStache2() {
    doEnterTest(

      """
        {{#foo}}<caret>
            <span></span>
        {{/foo}}""",

      """
        {{#foo}}
            <caret>
            <span></span>
        {{/foo}}"""
    );
  }

  public void testMarkupInBlockStache3() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <span></span><caret>",

      """
        {{#foo}}
            <span></span>
            <caret>"""
    );
  }

  public void testEmptyBlockInDiv1() {
    doEnterTest(

      """
        <div>
            {{#foo}}<caret>
            {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                <caret>
            {{/foo}}
        </div>"""
    );
  }

  public void testEmptyBlockInDiv2() {
    doEnterTest(

      """
        <div>
            {{#foo}}<caret>{{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                <caret>
            {{/foo}}
        </div>"""
    );
  }

  public void testEmptyBlockInDiv3() {
    doEnterTest(

      """
        <div>
            {{#foo}}<caret>
        htmlPadding""",

      """
        <div>
            {{#foo}}
                <caret>
        htmlPadding"""
    );
  }

  public void testEmptyBlockInDiv4() {
    doEnterTest(

      "<div>\n" +
      "{{#foo}}<caret>{{/foo}}",

      """
        <div>
        {{#foo}}
            <caret>
        {{/foo}}"""
    );
  }

  public void testSimpleBlockInDiv1() {
    doEnterTest(

      """
        <div>
        {{#foo}}
        {{bar}}<caret>
        {{/foo}}
        </div>""",

      """
        <div>
        {{#foo}}
        {{bar}}
            <caret>
        {{/foo}}
        </div>"""
    );
  }

  public void testSimpleBlockInDiv2() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                {{bar}}<caret>
            {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                {{bar}}
                <caret>
            {{/foo}}
        </div>"""
    );
  }

  public void testSimpleBlockInDiv3() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                {{bar}}
            {{/foo}}<caret>
        </div>""",

      """
        <div>
            {{#foo}}
                {{bar}}
            {{/foo}}
            <caret>
        </div>"""
    );
  }

  public void testSimpleBlockInDiv4() {
    doEnterTest(

      """
        <div>
        {{#foo}}
        {{bar}}<caret>""",

      """
        <div>
        {{#foo}}
        {{bar}}
            <caret>"""
    );
  }

  public void testSimpleBlockInDiv5() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                {{bar}}<caret>
        htmlPadding""",

      """
        <div>
            {{#foo}}
                {{bar}}
                <caret>
        htmlPadding"""
    );
  }

  public void testSimpleBlockInDiv6() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                {{bar}}
            {{/foo}}<caret>""",

      """
        <div>
            {{#foo}}
                {{bar}}
            {{/foo}}
            <caret>"""
    );
  }

  public void testSimpleBlockInDiv7() {
    doEnterTest(

      """
        <div>
            {{#foo}}<caret>
                {{bar}}
            {{/foo}}""",

      "<div>\n" +
      "    {{#foo}}\n" +
      "    <caret>\n" + // NOTE: this is not ideal, but it's tough to get the formatting right when there's unclosed html elements
      "        {{bar}}\n" +
      "    {{/foo}}"
    );
  }

  public void testSimpleBlockInDiv8() {
    doEnterTest(

      """
        <div>
            {{#foo}}<caret>
                {{bar}}
            {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                <caret>
                {{bar}}
            {{/foo}}
        </div>"""
    );
  }

  public void testAttributeStaches1() {
    doEnterTest(

      """
        <div {{foo}}><caret>
            <div class="{{bar}}">
                sweeet
            </div>
        </div>""",

      """
        <div {{foo}}>
            <caret>
            <div class="{{bar}}">
                sweeet
            </div>
        </div>"""
    );
  }

  public void testAttributeStaches2() {
    doEnterTest(

      """
        <div {{foo}}>
            <div class="{{bar}}"><caret>
                sweeet
            </div>
        </div>""",

      """
        <div {{foo}}>
            <div class="{{bar}}">
                <caret>
                sweeet
            </div>
        </div>"""
    );
  }

  public void testAttributeStaches3() {
    doEnterTest(

      """
        <div {{foo}}>
            <div class="{{bar}}">
                sweeet<caret>
            </div>
        </div>""",

      """
        <div {{foo}}>
            <div class="{{bar}}">
                sweeet
                <caret>
            </div>
        </div>"""
    );
  }

  public void testAttributeStaches4() {
    doEnterTest(

      "<div {{foo}}><caret>",

      "<div {{foo}}>\n" +
      "    <caret>"
    );
  }

  public void testAttributeStaches5() {
    doEnterTest(

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\"><caret>",

      """
        <div {{foo}}>
            <div class="{{bar}}">
                <caret>"""
    );
  }

  public void testAttributeStaches6() {
    doEnterTest(

      """
        <div {{foo}}>
            <div class="{{bar}}">
                sweeet<caret>""",

      """
        <div {{foo}}>
            <div class="{{bar}}">
                sweeet
                <caret>"""
    );
  }

  public void testMixedContentInDiv1() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span><caret>
            {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>
                <caret>
            {{/foo}}
        </div>"""
    );
  }

  public void testMixedContentInDiv2() {
    doEnterTest(

      """
        <div>
            {{#foo}}<caret>
                <span class="{{bat}}">{{bar}}</span>
            {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                <caret>
                <span class="{{bat}}">{{bar}}</span>
            {{/foo}}
        </div>"""
    );
  }

  public void testMixedContentInDiv3() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span><caret>
            {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>
                <caret>
            {{/foo}}
        </div>"""
    );
  }

  public void testMixedContentInDiv4() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>
            {{/foo}}<caret>
        </div>""",

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>
            {{/foo}}
            <caret>
        </div>"""
    );
  }

  public void testMixedContentInDiv5() {
    doEnterTest(

      """
        <div><caret>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>""",

      """
        <div>
            <caret>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>"""
    );
  }

  public void testMixedContentInDiv6() {
    doEnterTest(

      """
        <div>
            {{#foo}}<caret>
                <span class="{{bat}}">{{bar}}</span>""",

      """
        <div>
            {{#foo}}
                <caret>
                <span class="{{bat}}">{{bar}}</span>"""
    );
  }

  public void testMixedContentInDiv7() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span><caret>""",

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>
                <caret>"""
    );
  }

  public void testMixedContentInDiv8() {
    doEnterTest(

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>
            {{/foo}}<caret>""",

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>
            {{/foo}}
            <caret>"""
    );
  }

  public void testEmptyLinesAfterOpenBlock1() {
    doEnterTest(

      """
        {{#foo}}
           \s
           \s
           \s
            <caret>
           \s
        """,

      """
        {{#foo}}
           \s
           \s
           \s
           \s
            <caret>
           \s
        """
    );
  }

  public void testEmptyLinesAfterOpenBlock2() {
    doEnterTest(

      """
        {{#if}}
           \s
           \s
        {{else}}
           \s
           \s
            <caret>
           \s
           \s
        """,

      """
        {{#if}}
           \s
           \s
        {{else}}
           \s
           \s
           \s
            <caret>
           \s
           \s
        """
    );
  }

  public void testSimpleStacheInNestedDiv1() {
    doEnterTest(

      """
        {{#foo}}
            <div><caret>
                {{bar}}
            </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                <caret>
                {{bar}}
            </div>
        {{/foo}}"""
    );
  }

  public void testSimpleStacheInNestedDiv2() {
    doEnterTest(

      """
        {{#foo}}
            <div>
                {{bar}}<caret>
            </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                {{bar}}
                <caret>
            </div>
        {{/foo}}"""
    );
  }

  public void testBlockStacheInNestedDiv1() {
    doEnterTest(

      """
        {{#foo}}
            <div><caret>
                {{#bar}}
                    stuff
                {{/bar}}
            </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                <caret>
                {{#bar}}
                    stuff
                {{/bar}}
            </div>
        {{/foo}}"""
    );
  }

  public void testBlockStacheInNestedDiv2() {
    doEnterTest(

      """
        {{#foo}}
            <div>
                {{#bar}}<caret>
                    stuff
                {{/bar}}
            </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                {{#bar}}
                    <caret>
                    stuff
                {{/bar}}
            </div>
        {{/foo}}"""
    );
  }

  public void testBlockStacheInNestedDiv3() {
    doEnterTest(

      """
        {{#foo}}
            <div>
                {{#bar}}
                    stuff<caret>
                {{/bar}}
            </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                {{#bar}}
                    stuff
                    <caret>
                {{/bar}}
            </div>
        {{/foo}}"""
    );
  }

  public void testBlockStacheInNestedDiv4() {
    doEnterTest(

      """
        {{#foo}}
            <div>
                {{#bar}}
                    stuff
                {{/bar}}<caret>
            </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                {{#bar}}
                    stuff
                {{/bar}}
                <caret>
            </div>
        {{/foo}}"""
    );
  }
}

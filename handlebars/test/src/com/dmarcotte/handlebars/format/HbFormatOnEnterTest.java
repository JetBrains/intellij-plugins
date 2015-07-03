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
  protected void tearDown()
    throws Exception {
    HbConfig.setFormattingEnabled(myPrevFormatSetting);

    super.tearDown();
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

      "{{#foo}}\n" +
      "    {{bar}}\n" +
      "    <caret>htmlPadding"
    );
  }

  public void testSimpleBlock3() {
    doEnterTest(
      "{{#foo}}\n" +
      "    {{bar}}<caret>\n" +
      "{{/foo}}\n",

      "{{#foo}}\n" +
      "    {{bar}}\n" +
      "    <caret>\n" +
      "{{/foo}}\n");
  }

  public void testNestedBlocks1() {
    doEnterTest(

      "{{#foo}}\n" +
      "{{#bar}}\n" +
      "{{#bat}}<caret>\n" +
      "{{baz}}\n" +
      "{{/bat}}\n" +
      "{{/bar}}\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "{{#bar}}\n" +
      "{{#bat}}\n" +
      "    <caret>\n" +
      "{{baz}}\n" +
      "{{/bat}}\n" +
      "{{/bar}}\n" +
      "{{/foo}}"
    );
  }

  public void testNestedBlocks2() {
    doEnterTest(

      "{{#foo}}\n" +
      "    {{#bar}}\n" +
      "        {{#bat}}<caret>\n" +
      "            {{baz}}\n" +
      "        {{/bat}}\n" +
      "    {{/bar}}\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    {{#bar}}\n" +
      "        {{#bat}}\n" +
      "            <caret>\n" +
      "            {{baz}}\n" +
      "        {{/bat}}\n" +
      "    {{/bar}}\n" +
      "{{/foo}}"
    );
  }

  public void testNestedBlocks3() {
    doEnterTest(

      "{{#foo}}\n" +
      "    {{#bar}}\n" +
      "        {{#bat}}\n" +
      "            {{baz}}<caret>\n" +
      "        {{/bat}}\n" +
      "    {{/bar}}\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    {{#bar}}\n" +
      "        {{#bat}}\n" +
      "            {{baz}}\n" +
      "            <caret>\n" +
      "        {{/bat}}\n" +
      "    {{/bar}}\n" +
      "{{/foo}}"
    );
  }

  public void testSimpleStacheInDiv1() {
    doEnterTest(

      "<div><caret>\n" +
      "    {{foo}}\n" +
      "</div>",

      "<div>\n" +
      "    <caret>\n" +
      "    {{foo}}\n" +
      "</div>"
    );
  }

  public void testSimpleStacheInDiv2() {
    doEnterTest(

      "<div>\n" +
      "    {{foo}}<caret>\n" +
      "</div>",

      "<div>\n" +
      "    {{foo}}\n" +
      "    <caret>\n" +
      "</div>"
    );
  }

  public void testSimpleStacheInDiv3() {
    doEnterTest(

      "<div>\n" +
      "    {{foo}}<caret>",

      "<div>\n" +
      "    {{foo}}\n" +
      "    <caret>"
    );
  }

  public void testMarkupInBlockStache1() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <span></span><caret>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <span></span>\n" +
      "    <caret>\n" +
      "{{/foo}}"
    );
  }

  public void testMarkupInBlockStache2() {
    doEnterTest(

      "{{#foo}}<caret>\n" +
      "    <span></span>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <caret>\n" +
      "    <span></span>\n" +
      "{{/foo}}"
    );
  }

  public void testMarkupInBlockStache3() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <span></span><caret>",

      "{{#foo}}\n" +
      "    <span></span>\n" +
      "    <caret>"
    );
  }

  public void testEmptyBlockInDiv1() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}<caret>\n" +
      "    {{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <caret>\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testEmptyBlockInDiv2() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}<caret>{{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <caret>\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testEmptyBlockInDiv3() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}<caret>\n" +
      "htmlPadding",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <caret>\n" +
      "htmlPadding"
    );
  }

  public void testEmptyBlockInDiv4() {
    doEnterTest(

      "<div>\n" +
      "{{#foo}}<caret>{{/foo}}",

      "<div>\n" +
      "{{#foo}}\n" +
      "    <caret>\n" +
      "{{/foo}}"
    );
  }

  public void testSimpleBlockInDiv1() {
    doEnterTest(

      "<div>\n" +
      "{{#foo}}\n" +
      "{{bar}}<caret>\n" +
      "{{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "{{#foo}}\n" +
      "{{bar}}\n" +
      "    <caret>\n" +
      "{{/foo}}\n" +
      "</div>"
    );
  }

  public void testSimpleBlockInDiv2() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}<caret>\n" +
      "    {{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}\n" +
      "        <caret>\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testSimpleBlockInDiv3() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}\n" +
      "    {{/foo}}<caret>\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}\n" +
      "    {{/foo}}\n" +
      "    <caret>\n" +
      "</div>"
    );
  }

  public void testSimpleBlockInDiv4() {
    doEnterTest(

      "<div>\n" +
      "{{#foo}}\n" +
      "{{bar}}<caret>",

      "<div>\n" +
      "{{#foo}}\n" +
      "{{bar}}\n" +
      "    <caret>"
    );
  }

  public void testSimpleBlockInDiv5() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}<caret>\n" +
      "htmlPadding",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}\n" +
      "        <caret>\n" +
      "htmlPadding"
    );
  }

  public void testSimpleBlockInDiv6() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}\n" +
      "    {{/foo}}<caret>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}\n" +
      "    {{/foo}}\n" +
      "    <caret>"
    );
  }

  public void testSimpleBlockInDiv7() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}<caret>\n" +
      "        {{bar}}\n" +
      "    {{/foo}}",

      "<div>\n" +
      "    {{#foo}}\n" +
      "    <caret>\n" + // NOTE: this is not ideal, but it's tough to get the formatting right when there's unclosed html elements
      "        {{bar}}\n" +
      "    {{/foo}}"
    );
  }

  public void testSimpleBlockInDiv8() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}<caret>\n" +
      "        {{bar}}\n" +
      "    {{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <caret>\n" +
      "        {{bar}}\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testAttributeStaches1() {
    doEnterTest(

      "<div {{foo}}><caret>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        sweeet\n" +
      "    </div>\n" +
      "</div>",

      "<div {{foo}}>\n" +
      "    <caret>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        sweeet\n" +
      "    </div>\n" +
      "</div>"
    );
  }

  public void testAttributeStaches2() {
    doEnterTest(

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\"><caret>\n" +
      "        sweeet\n" +
      "    </div>\n" +
      "</div>",

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        <caret>\n" +
      "        sweeet\n" +
      "    </div>\n" +
      "</div>"
    );
  }

  public void testAttributeStaches3() {
    doEnterTest(

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        sweeet<caret>\n" +
      "    </div>\n" +
      "</div>",

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        sweeet\n" +
      "        <caret>\n" +
      "    </div>\n" +
      "</div>"
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

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        <caret>"
    );
  }

  public void testAttributeStaches6() {
    doEnterTest(

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        sweeet<caret>",

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        sweeet\n" +
      "        <caret>"
    );
  }

  public void testMixedContentInDiv1() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span><caret>\n" +
      "    {{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "        <caret>\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testMixedContentInDiv2() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}<caret>\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "    {{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <caret>\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testMixedContentInDiv3() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span><caret>\n" +
      "    {{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "        <caret>\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testMixedContentInDiv4() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "    {{/foo}}<caret>\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "    {{/foo}}\n" +
      "    <caret>\n" +
      "</div>"
    );
  }

  public void testMixedContentInDiv5() {
    doEnterTest(

      "<div><caret>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>",

      "<div>\n" +
      "    <caret>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>"
    );
  }

  public void testMixedContentInDiv6() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}<caret>\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <caret>\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>"
    );
  }

  public void testMixedContentInDiv7() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span><caret>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "        <caret>"
    );
  }

  public void testMixedContentInDiv8() {
    doEnterTest(

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "    {{/foo}}<caret>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "    {{/foo}}\n" +
      "    <caret>"
    );
  }

  public void testEmptyLinesAfterOpenBlock1() {
    doEnterTest(

      "{{#foo}}\n" +
      "    \n" +
      "    \n" +
      "    \n" +
      "    <caret>\n" +
      "    \n",

      "{{#foo}}\n" +
      "    \n" +
      "    \n" +
      "    \n" +
      "    \n" +
      "    <caret>\n" +
      "    \n"
    );
  }

  public void testEmptyLinesAfterOpenBlock2() {
    doEnterTest(

      "{{#if}}\n" +
      "    \n" +
      "    \n" +
      "{{else}}\n" +
      "    \n" +
      "    \n" +
      "    <caret>\n" +
      "    \n" +
      "    \n",

      "{{#if}}\n" +
      "    \n" +
      "    \n" +
      "{{else}}\n" +
      "    \n" +
      "    \n" +
      "    \n" +
      "    <caret>\n" +
      "    \n" +
      "    \n"
    );
  }

  public void testSimpleStacheInNestedDiv1() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <div><caret>\n" +
      "        {{bar}}\n" +
      "    </div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        <caret>\n" +
      "        {{bar}}\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }

  public void testSimpleStacheInNestedDiv2() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{bar}}<caret>\n" +
      "    </div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{bar}}\n" +
      "        <caret>\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }

  public void testBlockStacheInNestedDiv1() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <div><caret>\n" +
      "        {{#bar}}\n" +
      "            stuff\n" +
      "        {{/bar}}\n" +
      "    </div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        <caret>\n" +
      "        {{#bar}}\n" +
      "            stuff\n" +
      "        {{/bar}}\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }

  public void testBlockStacheInNestedDiv2() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{#bar}}<caret>\n" +
      "            stuff\n" +
      "        {{/bar}}\n" +
      "    </div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{#bar}}\n" +
      "            <caret>\n" +
      "            stuff\n" +
      "        {{/bar}}\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }

  public void testBlockStacheInNestedDiv3() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{#bar}}\n" +
      "            stuff<caret>\n" +
      "        {{/bar}}\n" +
      "    </div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{#bar}}\n" +
      "            stuff\n" +
      "            <caret>\n" +
      "        {{/bar}}\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }

  public void testBlockStacheInNestedDiv4() {
    doEnterTest(

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{#bar}}\n" +
      "            stuff\n" +
      "        {{/bar}}<caret>\n" +
      "    </div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{#bar}}\n" +
      "            stuff\n" +
      "        {{/bar}}\n" +
      "        <caret>\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }
}

package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.application.options.CodeStyle;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class HbFormatterIndentTest extends HbFormatterTest {

  /**
   * This sanity check should be enough to ensure that we don't format
   * when the formatter is disabled
   */
  public void testFormatterDisabled() {
    boolean previousFormatterSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doStringBasedTest(

      "{{#foo}}\n" +
      "{{bar}}\n" +
      "{{/foo}}\n",

      "{{#foo}}\n" +
      "{{bar}}\n" +
      "{{/foo}}\n"
    );

    HbConfig.setFormattingEnabled(previousFormatterSetting);
  }

  /**
   * Sanity check that we respect non-default (i.e. 4) indent sizes
   */
  public void testNonDefaultIndentSize() {
    int previousHtmlIndent = CodeStyle.getSettings(getProject()).getIndentOptions(HtmlFileType.INSTANCE).INDENT_SIZE;
    CodeStyle.getSettings(getProject()).getIndentOptions(HtmlFileType.INSTANCE).INDENT_SIZE = 2;

    doStringBasedTest(

      "{{#foo}}\n" +
      "    <div>\n" +
      "{{bar}}\n" +
      "    </div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "  <div>\n" +
      "    {{bar}}\n" +
      "  </div>\n" +
      "{{/foo}}"
    );

    CodeStyle.getSettings(getProject()).getIndentOptions(HtmlFileType.INSTANCE).INDENT_SIZE = previousHtmlIndent;
  }

  public void testSimpleStache() {
    doStringBasedTest(

      "{{foo}}",

      "{{foo}}"
    );
  }

  public void testSimpleBlock() {
    doStringBasedTest(

      "{{#foo}}\n" +
      "{{bar}}\n" +
      "{{/foo}}\n",

      "{{#foo}}\n" +
      "    {{bar}}\n" +
      "{{/foo}}\n"
    );
  }

  public void testNestedBlocks() {
    doStringBasedTest(

      "{{#foo}}\n" +
      "{{#bar}}\n" +
      "{{#bat}}\n" +
      "{{baz}}\n" +
      "{{/bat}}\n" +
      "{{/bar}}\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    {{#bar}}\n" +
      "        {{#bat}}\n" +
      "            {{baz}}\n" +
      "        {{/bat}}\n" +
      "    {{/bar}}\n" +
      "{{/foo}}"
    );
  }

  public void testSimpleStacheInDiv() {
    doStringBasedTest(

      "<div>\n" +
      "{{foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{foo}}\n" +
      "</div>"
    );
  }

  public void testMarkupInBlockStache() {
    doStringBasedTest(

      "{{#foo}}\n" +
      "<span></span>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <span></span>\n" +
      "{{/foo}}"
    );
  }

  public void testSimpleBlockInDiv() {
    doStringBasedTest(

      "<div>\n" +
      "{{#foo}}\n" +
      "{{bar}}\n" +
      "{{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        {{bar}}\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testAttributeStaches() {
    doStringBasedTest(

      "<div {{foo}}>\n" +
      "<div class=\"{{bar}}\">\n" +
      "sweeet\n" +
      "</div>\n" +
      "</div>",

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\">\n" +
      "        sweeet\n" +
      "    </div>\n" +
      "</div>"
    );
  }

  public void testMixedContentInDiv1() {
    doStringBasedTest(

      "<div>\n" +
      "{{#foo}}\n" +
      "<span class=\"{{bat}}\">{{bar}}</span>\n" +
      "{{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        <span class=\"{{bat}}\">{{bar}}</span>\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testMixedContentInDiv2() {
    doStringBasedTest(

      "<div>\n" +
      "{{#foo}}\n" +
      "bar {{baz}}\n" +
      "{{/foo}}\n" +
      "</div>",

      "<div>\n" +
      "    {{#foo}}\n" +
      "        bar {{baz}}\n" +
      "    {{/foo}}\n" +
      "</div>"
    );
  }

  public void testSimpleStacheInNestedDiv() {
    doStringBasedTest(

      "{{#foo}}\n" +
      "    <div>\n" +
      "{{bar}}\n" +
      "    </div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{bar}}\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }

  public void testBlockStacheInNestedDiv() {
    doStringBasedTest(

      "{{#foo}}\n" +
      "<div>\n" +
      "{{#bar}}\n" +
      "stuff\n" +
      "{{/bar}}\n" +
      "</div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        {{#bar}}\n" +
      "            stuff\n" +
      "        {{/bar}}\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }

  public void testNestedDivsInBlock() {
    doStringBasedTest(

      "{{#foo}}\n" +
      "<div>\n" +
      "<div>\n" +
      "<div>\n" +
      "{{bar}}\n" +
      "{{#foo}}\n" +
      "{{/foo}}\n" +
      "</div>\n" +
      "</div>\n" +
      "</div>\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <div>\n" +
      "        <div>\n" +
      "            <div>\n" +
      "                {{bar}}\n" +
      "                {{#foo}}\n" +
      "                {{/foo}}\n" +
      "            </div>\n" +
      "        </div>\n" +
      "    </div>\n" +
      "{{/foo}}"
    );
  }

  public void testFormattingInsideDoNotIndentElems1() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "body";

    doStringBasedTest(

      "<body>\n" +
      "{{#foo}}\n" +
      "<div></div>\n" +
      "{{/foo}}\n" +
      "</body>",

      "<body>\n" +
      "{{#foo}}\n" +
      "    <div></div>\n" +
      "{{/foo}}\n" +
      "</body>"
    );
  }

  public void testFormattingInsideDoNotIndentElems2() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "body";

    doStringBasedTest(

      "<body>\n" +
      "{{foo}}\n" +
      "<div></div>\n" +
      "</body>",

      "<body>\n" +
      "{{foo}}\n" +
      "<div></div>\n" +
      "</body>"
    );
  }

  public void testFormattingInsideNestedDoNotIndentElems() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "span";

    doStringBasedTest(

      "<span>\n" +
      "{{#foo}}\n" +
      "<span>\n" +
      "{{^bar}}\n" +
      "<span></span>\n" +
      "{{/bar}}\n" +
      "</span>\n" +
      "{{/foo}}\n" +
      "</span>",

      "<span>\n" +
      "{{#foo}}\n" +
      "    <span>\n" +
      "    {{^bar}}\n" +
      "        <span></span>\n" +
      "    {{/bar}}\n" +
      "    </span>\n" +
      "{{/foo}}\n" +
      "</span>"
    );
  }

  @NotNull
  private HtmlCodeStyleSettings getHtmlSettings() {
    return CodeStyle.getSettings(getProject()).getCustomSettings(HtmlCodeStyleSettings.class);
  }
}

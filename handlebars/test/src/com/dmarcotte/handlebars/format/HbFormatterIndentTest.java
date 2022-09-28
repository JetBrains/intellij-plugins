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

      """
        {{#foo}}
        {{bar}}
        {{/foo}}
        """,

      """
        {{#foo}}
        {{bar}}
        {{/foo}}
        """
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

      """
        {{#foo}}
            <div>
        {{bar}}
            </div>
        {{/foo}}""",

      """
        {{#foo}}
          <div>
            {{bar}}
          </div>
        {{/foo}}"""
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

      """
        {{#foo}}
        {{bar}}
        {{/foo}}
        """,

      """
        {{#foo}}
            {{bar}}
        {{/foo}}
        """
    );
  }

  public void testNestedBlocks() {
    doStringBasedTest(

      """
        {{#foo}}
        {{#bar}}
        {{#bat}}
        {{baz}}
        {{/bat}}
        {{/bar}}
        {{/foo}}""",

      """
        {{#foo}}
            {{#bar}}
                {{#bat}}
                    {{baz}}
                {{/bat}}
            {{/bar}}
        {{/foo}}"""
    );
  }

  public void testSimpleStacheInDiv() {
    doStringBasedTest(

      """
        <div>
        {{foo}}
        </div>""",

      """
        <div>
            {{foo}}
        </div>"""
    );
  }

  public void testMarkupInBlockStache() {
    doStringBasedTest(

      """
        {{#foo}}
        <span></span>
        {{/foo}}""",

      """
        {{#foo}}
            <span></span>
        {{/foo}}"""
    );
  }

  public void testSimpleBlockInDiv() {
    doStringBasedTest(

      """
        <div>
        {{#foo}}
        {{bar}}
        {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                {{bar}}
            {{/foo}}
        </div>"""
    );
  }

  public void testAttributeStaches() {
    doStringBasedTest(

      """
        <div {{foo}}>
        <div class="{{bar}}">
        sweeet
        </div>
        </div>""",

      """
        <div {{foo}}>
            <div class="{{bar}}">
                sweeet
            </div>
        </div>"""
    );
  }

  public void testMixedContentInDiv1() {
    doStringBasedTest(

      """
        <div>
        {{#foo}}
        <span class="{{bat}}">{{bar}}</span>
        {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                <span class="{{bat}}">{{bar}}</span>
            {{/foo}}
        </div>"""
    );
  }

  public void testMixedContentInDiv2() {
    doStringBasedTest(

      """
        <div>
        {{#foo}}
        bar {{baz}}
        {{/foo}}
        </div>""",

      """
        <div>
            {{#foo}}
                bar {{baz}}
            {{/foo}}
        </div>"""
    );
  }

  public void testSimpleStacheInNestedDiv() {
    doStringBasedTest(

      """
        {{#foo}}
            <div>
        {{bar}}
            </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                {{bar}}
            </div>
        {{/foo}}"""
    );
  }

  public void testBlockStacheInNestedDiv() {
    doStringBasedTest(

      """
        {{#foo}}
        <div>
        {{#bar}}
        stuff
        {{/bar}}
        </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                {{#bar}}
                    stuff
                {{/bar}}
            </div>
        {{/foo}}"""
    );
  }

  public void testNestedDivsInBlock() {
    doStringBasedTest(

      """
        {{#foo}}
        <div>
        <div>
        <div>
        {{bar}}
        {{#foo}}
        {{/foo}}
        </div>
        </div>
        </div>
        {{/foo}}""",

      """
        {{#foo}}
            <div>
                <div>
                    <div>
                        {{bar}}
                        {{#foo}}
                        {{/foo}}
                    </div>
                </div>
            </div>
        {{/foo}}"""
    );
  }

  public void testFormattingInsideDoNotIndentElems1() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "body";

    doStringBasedTest(

      """
        <body>
        {{#foo}}
        <div></div>
        {{/foo}}
        </body>""",

      """
        <body>
        {{#foo}}
            <div></div>
        {{/foo}}
        </body>"""
    );
  }

  public void testFormattingInsideDoNotIndentElems2() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "body";

    doStringBasedTest(

      """
        <body>
        {{foo}}
        <div></div>
        </body>""",

      """
        <body>
        {{foo}}
        <div></div>
        </body>"""
    );
  }

  public void testFormattingInsideNestedDoNotIndentElems() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "span";

    doStringBasedTest(

      """
        <span>
        {{#foo}}
        <span>
        {{^bar}}
        <span></span>
        {{/bar}}
        </span>
        {{/foo}}
        </span>""",

      """
        <span>
        {{#foo}}
            <span>
            {{^bar}}
                <span></span>
            {{/bar}}
            </span>
        {{/foo}}
        </span>"""
    );
  }

  @NotNull
  private HtmlCodeStyleSettings getHtmlSettings() {
    return CodeStyle.getSettings(getProject()).getCustomSettings(HtmlCodeStyleSettings.class);
  }
}

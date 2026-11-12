package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.application.options.CodeStyle;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.dmarcotte.handlebars.format.HbFormatterTestUtil.doStringBasedTest;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class HbFormatterIndentTest implements HbFormattingModelBuilderTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private FormatterTestSettings formatterTestSettings;

  @BeforeEach
  void setUp() {
    formatterTestSettings = new FormatterTestSettings(CodeStyle.getSettings(fixture.get().getProject()));
    formatterTestSettings.setUp();
  }

  @AfterEach
  void tearDown() {
    if (formatterTestSettings != null) {
      formatterTestSettings.tearDown();
    }
  }

  /**
   * This sanity check should be enough to ensure that we don't format
   * when the formatter is disabled
   */
  @Test
  public void testFormatterDisabled() {
    boolean previousFormatterSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doStringBasedTest(fixture.get(),

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
  @Test
  public void testNonDefaultIndentSize() {
    int previousHtmlIndent = CodeStyle.getSettings(fixture.get().getProject()).getIndentOptions(HtmlFileType.INSTANCE).INDENT_SIZE;
    CodeStyle.getSettings(fixture.get().getProject()).getIndentOptions(HtmlFileType.INSTANCE).INDENT_SIZE = 2;

    doStringBasedTest(fixture.get(),

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

    CodeStyle.getSettings(fixture.get().getProject()).getIndentOptions(HtmlFileType.INSTANCE).INDENT_SIZE = previousHtmlIndent;
  }

  @Test
  public void testSimpleStache() {
    doStringBasedTest(fixture.get(),

      "{{foo}}",

      "{{foo}}"
    );
  }

  @Test
  public void testSimpleBlock() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testNestedBlocks() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testSimpleStacheInDiv() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testMarkupInBlockStache() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testSimpleBlockInDiv() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testAttributeStaches() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv1() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv2() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testSimpleStacheInNestedDiv() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testBlockStacheInNestedDiv() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testNestedDivsInBlock() {
    doStringBasedTest(fixture.get(),

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

  @Test
  public void testFormattingInsideDoNotIndentElems1() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "body";

    doStringBasedTest(fixture.get(),

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

  @Test
  public void testFormattingInsideDoNotIndentElems2() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "body";

    doStringBasedTest(fixture.get(),

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

  @Test
  public void testFormattingInsideNestedDoNotIndentElems() {

    HtmlCodeStyleSettings settings = getHtmlSettings();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = "span";

    doStringBasedTest(fixture.get(),

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
    return CodeStyle.getSettings(fixture.get().getProject()).getCustomSettings(HtmlCodeStyleSettings.class);
  }
}

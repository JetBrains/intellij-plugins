package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.idea.IJIgnore;
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
public class HbFormatOnEnterTest implements HbFormattingModelBuilderTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  private boolean myPrevFormatSetting;

  @BeforeEach
  void setUp() {
    myPrevFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(true);
  }

  @AfterEach
  void tearDown() {
    HbConfig.setFormattingEnabled(myPrevFormatSetting);
  }

  /**
   * This sanity check should be enough to ensure that we don't format on Enter
   * when the formatter is disabled
   */
  @IJIgnore(issue = "WEB-77789")
  @Test
  public void testEnterWithFormatterDisabled() {
    boolean previousFormatterSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);

    doEnterTest(fixture.get(),

      "{{#foo}}<caret>",

      "{{#foo}}\n" +
      "<caret>"
    );

    HbConfig.setFormattingEnabled(previousFormatterSetting);
  }

  @Test
  public void testSimpleStache() {
    doEnterTest(fixture.get(),

      "{{foo}}<caret>",

      "{{foo}}\n" +
      "<caret>"
    );
  }

  @Test
  public void testSimpleBlock1() {
    doEnterTest(fixture.get(),

      "{{#foo}}<caret>",

      "{{#foo}}\n" +
      "    <caret>"
    );
  }

  @Test
  public void testSimpleBlock2() {
    doEnterTest(fixture.get(),

      "{{#foo}}\n" +
      "    {{bar}}<caret>htmlPadding",

      """
        {{#foo}}
            {{bar}}
            <caret>htmlPadding"""
    );
  }

  @Test
  public void testSimpleBlock3() {
    doEnterTest(fixture.get(),
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

  @Test
  public void testNestedBlocks1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testNestedBlocks2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testNestedBlocks3() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleStacheInDiv1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleStacheInDiv2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleStacheInDiv3() {
    doEnterTest(fixture.get(),

      "<div>\n" +
      "    {{foo}}<caret>",

      """
        <div>
            {{foo}}
            <caret>"""
    );
  }

  @Test
  public void testMarkupInBlockStache1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMarkupInBlockStache2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMarkupInBlockStache3() {
    doEnterTest(fixture.get(),

      "{{#foo}}\n" +
      "    <span></span><caret>",

      """
        {{#foo}}
            <span></span>
            <caret>"""
    );
  }

  @Test
  public void testEmptyBlockInDiv1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testEmptyBlockInDiv2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testEmptyBlockInDiv3() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testEmptyBlockInDiv4() {
    doEnterTest(fixture.get(),

      "<div>\n" +
      "{{#foo}}<caret>{{/foo}}",

      """
        <div>
        {{#foo}}
            <caret>
        {{/foo}}"""
    );
  }

  @Test
  public void testSimpleBlockInDiv1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleBlockInDiv2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleBlockInDiv3() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleBlockInDiv4() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleBlockInDiv5() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleBlockInDiv6() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleBlockInDiv7() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleBlockInDiv8() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testAttributeStaches1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testAttributeStaches2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testAttributeStaches3() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testAttributeStaches4() {
    doEnterTest(fixture.get(),

      "<div {{foo}}><caret>",

      "<div {{foo}}>\n" +
      "    <caret>"
    );
  }

  @Test
  public void testAttributeStaches5() {
    doEnterTest(fixture.get(),

      "<div {{foo}}>\n" +
      "    <div class=\"{{bar}}\"><caret>",

      """
        <div {{foo}}>
            <div class="{{bar}}">
                <caret>"""
    );
  }

  @Test
  public void testAttributeStaches6() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv3() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv4() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv5() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv6() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv7() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testMixedContentInDiv8() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testEmptyLinesAfterOpenBlock1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testEmptyLinesAfterOpenBlock2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleStacheInNestedDiv1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testSimpleStacheInNestedDiv2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testBlockStacheInNestedDiv1() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testBlockStacheInNestedDiv2() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testBlockStacheInNestedDiv3() {
    doEnterTest(fixture.get(),

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

  @Test
  public void testBlockStacheInNestedDiv4() {
    doEnterTest(fixture.get(),

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

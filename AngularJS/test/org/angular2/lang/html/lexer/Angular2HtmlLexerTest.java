// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.LexerTestCase;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NonNls;

public class Angular2HtmlLexerTest extends LexerTestCase {
  private IdeaProjectTestFixture myFixture;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // needed for various XML extension points registration
    myFixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR).getFixture();
    myFixture.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myFixture.tearDown();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  public void testNoNewline() {
    doTest("<t>a</t>");
  }

  public void testNewlines() {
    doTest("<t\n>\r\na\r</t>");
  }

  public void testComments() {
    doTest("<!-- {{ v }} -->");
  }

  public void testInterpolation1() {
    doTest("<t a=\"{{v}}\" b=\"s{{m}}e\" c='s{{m//c}}e'>");
  }

  public void testInterpolation2() {
    doTest("{{ a }}b{{ c // comment }}");
  }

  public void testBoundAttributes() {
    doTest("<a [src]=bla() (click)='event()'></a>");
  }

  public void testMultipleInterpolations() {
    doTest("{{test}} !=bbb {{foo() - bar()}}");
  }

  public void testInterpolationIgnored() {
    doTest("<div> this is ignored {{<interpolation> }}");
  }

  public void testInterpolationIgnored2() {
    doTest("this {{ is {{ <ignored/> interpolation }}");
  }

  public void testInterpolationIgnored3() {
    doTest("<div foo=\"This {{ is {{ ignored interpolation\"> }}<a foo=\"{{\">");
  }

  public void testInterpolationIgnored4() {
    doTest("<div foo='This {{ is {{ ignored interpolation'> }}<a foo='{{'>");
  }

  public void testInterpolationEmpty() {
    doTest("{{}}<div foo='{{}}' foo='a{{}}b' bar=\"{{}}\" bar=\"a{{}}b\">{{}}</div>a{{}}b<div>a{{}}b</div>");
  }

  public void testInterpolationCharEntityRefs() {
    doTest("&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}<div foo='&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}' bar=\"&nbsp;{{foo&nbsp;bar}}{{&nbsp;}}\">");
  }

  public void testInterpolationEntityRefs() {
    doTest("&foo;{{foo&foo;bar}}{{&foo;}}<div foo='&foo;{{foo&foo;bar}}{{&foo;}}' bar=\"&foo;{{foo&foo;bar}}{{&foo;}}\">");
  }

  public void testComplex() {
    doTest("<div *ngFor=\"let contact of value; index as i\"\n" +
           "  (click)=\"contact\"\n" +
           "</div>\n" +
           "\n" +
           "<li *ngFor=\"let user of userObservable | async as users; index as i; first as isFirst\">\n" +
           "  {{i}}/{{users.length}}. {{user}} <span *ngIf=\"isFirst\">default</span>\n" +
           "</li>\n" +
           "\n" +
           "<tr [style]=\"{'visible': con}\" *ngFor=\"let contact of contacts; index as i\">\n" +
           "  <td>{{i + 1}}</td>\n" +
           "</tr>\n");
  }

  public void testExpansionForm() {
    doTest("{one.two, three, =4 {four} =5 {five} foo {bar} }");
  }

  public void testExpansionFormWithTextElementsAround() {
    doTest("before{one.two, three, =4 {four}}after");
  }

  public void testExpansionFormTagSingleChild() {
    doTest("<div><span>{a, b, =4 {c}}</span></div>");
  }

  public void testExpansionFormWithTagsInIt() {
    doTest("{one.two, three, =4 {four <b>a</b>}}");
  }

  public void testExpansionFormWithInterpolation() {
    doTest("{one.two, three, =4 {four {{a}}}}");
  }

  public void testExpansionFormNested() {
    doTest("{one.two, three, =4 {{xx, yy, =x {one}} }}");
  }

  public void testExpansionFormComplex() {
    doTest("<div>Text{ form, open, =23 {{{{foo: 12} }} is {inner, open, =34{{{\"test\"}} cool } =12{<tag test='12'></tag>}}}}}} {}");
  }

  public void testScriptSrc() {
    doTest("<body>\n" +
           "<script src=\"\">var i</script>\n" +
           "foo\n" +
           "</body>");
  }

  public void testScript() {
    doTest("<body>\n" +
           "<script>var i</script>\n" +
           "foo\n" +
           "</body>");
  }

  public void testScriptAngularAttr() {
    doTest("<body>\n" +
           "<script (foo)=\"\">var i</script>\n" +
           "foo\n" +
           "</body>");
  }

  @Override
  protected void doTest(@NonNls String text) {
    super.doTest(text);
    checkCorrectRestart(text);
  }

  @Override
  protected Lexer createLexer() {
    return new Angular2HtmlLexer(true, null);
  }

  @Override
  protected String getDirPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()).substring(PathManager.getHomePath().length());
  }
}

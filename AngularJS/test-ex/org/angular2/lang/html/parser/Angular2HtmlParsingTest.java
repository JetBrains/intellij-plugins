// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.html.HtmlParsingTest;
import com.intellij.html.embedding.HtmlEmbeddedContentSupport;
import com.intellij.javascript.JSHtmlEmbeddedContentSupport;
import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.tree.events.TreeChangeEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssEmbeddedTokenTypesProvider;
import com.intellij.psi.css.CssHtmlEmbeddedContentSupport;
import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorProviderImpl;
import com.intellij.psi.impl.BlockSupportImpl;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.impl.DiffLog;
import org.angular2.lang.expr.parser.Angular2ParserDefinition;
import org.angular2.lang.html.lexer.Angular2HtmlEmbeddedContentSupport;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;

public class Angular2HtmlParsingTest extends HtmlParsingTest {

  public Angular2HtmlParsingTest() {
    super("", "html",
          new Angular2HtmlParserDefinition(),
          new Angular2ParserDefinition(),
          new JavascriptParserDefinition(),
          new HTMLParserDefinition(),
          new CSSParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    registerExtensions(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, EmbeddedTokenTypesProvider.class,
                       Collections.singletonList(new CssEmbeddedTokenTypesProvider()));
    HtmlEmbeddedContentSupport.register(getApplication(), getTestRootDisposable(),
                                        CssHtmlEmbeddedContentSupport.class, JSHtmlEmbeddedContentSupport.class,
                                        Angular2HtmlEmbeddedContentSupport.class);

    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, new CssTreeElementFactory());
    registerExtensionPoint(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider.class);
    registerExtension(CssElementDescriptorProvider.EP_NAME, new CssElementDescriptorProviderImpl());
    getApplication().registerService(CssElementDescriptorFactory2.class,
                                     new CssElementDescriptorFactory2("css-parsing-tests.xml"));

    // Update parser definition if version is changed
    assert JSLanguageLevel.DEFAULT == JSLanguageLevel.ES6;
    registerParserDefinition(new ECMA6ParserDefinition());
  }

  @Override
  protected void checkResult(@NotNull String targetDataName, @NotNull PsiFile file) throws IOException {
    super.checkResult(targetDataName, file);
    ensureReparsingConsistent(file);
  }

  private static void ensureReparsingConsistent(@NotNull PsiFile file) {
    DebugUtil.performPsiModification("ensureReparsingConsistent", () -> {
      final String fileText = file.getText();
      final DiffLog diffLog = new BlockSupportImpl().reparseRange(
        file, file.getNode(), TextRange.allOf(fileText), fileText, new EmptyProgressIndicator(), fileText);
      TreeChangeEvent event = diffLog.performActualPsiChange(file);
      assertEmpty(event.getChangedElements());
    });
  }

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestExDataPath(Angular2HtmlParsingTest.class);
  }

  public void testNgParseElementsInsideNgTemplate() throws Exception {
    doTestHtml("<ng-template><span></span></ng-template>");
  }

  public void testNgSupportVoidElements() throws Exception {
    doTestHtml("<link rel=\"author license\" href=\"/about\">");
  }

  public void _testNgNotErrorOnVoidHtml5Elements() throws Exception {
    doTestHtml("<map><area></map><div><br></div><colgroup><col></colgroup>" +
               "<div><embed></div><div><hr></div><div><img></div><div><input></div>" +
               "<object><param>/<object><audio><source></audio><audio><track></audio>" +
               "<p><wbr></p>");
  }

  public void _testNgReportClosingTagForVoidElement() throws Exception {
    doTestHtml("<input></input>");
  }

  public void _testNgReportSelfClosingHtmlElement() throws Exception {
    doTestHtml("<p />");
  }

  public void testNgCloseVoidElementsOnTextNodes() throws Exception {
    doTestHtml("<p>before<br>after</p>");
  }

  public void testNgSupportOptionalEndTags() throws Exception {
    doTestHtml("<div><p>1<p>2</div>");
  }

  public void testNgSupportNestedElements() throws Exception {
    doTestHtml("<ul><li><ul><li></li></ul></li></ul>");
  }

  public void testNgSupportSelfClosingVoidElements() throws Exception {
    doTestHtml("<input />");
  }

  public void testNgParseExpansionForms1() throws Exception {
    doTestHtml("<div>before{messages.length, plural, =0 {You have <b>no</b> messages} =1 {One {{message}}}}after</div>");
  }

  public void testNgParseExpansionForms2() throws Exception {
    doTestHtml("<div><span>{a, plural, =0 {b}}</span></div>");
  }

  public void testNgParseExpansionForms3() throws Exception {
    doTestHtml("{messages.length, plural, =0 { {p.gender, select, male {m}} }}");
  }

  public void testNgErrorOnUnterminatedExpansionForm() throws Exception {
    doTestHtml("{messages.length, plural, =0 {one}");
  }

  public void testNgICUWithNumbers() throws Exception {
    doTestHtml("{sex, select, male {m} female {f} 0 {other}}");
  }

  public void testNgErrorOnUnterminatedExpansionCase() throws Exception {
    doTestHtml("{messages.length, plural, =0 {one");
  }

  public void testNgErrorOnInvalidHTMLInExpansionCase() throws Exception {
    doTestHtml("{messages.length, plural, =0 {<div>}}");
  }

  public void testNgWhitespacesInExpansionCase() throws Exception {
    doTestHtml("{ messages . length,  plural ,  =0   {  <div> } }");
  }

  public void testExpansionFormComplex() throws Exception {
    doTestHtml("<div>Text{ form, open, =23 {{{{foo: 12} }} is {inner, open, =34{{{\"test\"}} cool } =12{<tag test='12'></tag>}}}}}} {}");
  }

  public void testNgReportUnexpectedClosingTag() throws Exception {
    doTestHtml("<div></p></div>");
  }

  public void testNgReportSubsequentOpenTagWithoutCloseTag() throws Exception {
    doTestHtml("<div</div>");
  }

  public void testNgParseBoundProperties() throws Exception {
    doTestHtml("<div [someProp]='v'></div>" +
               "<div [some-prop]='v'></div>" +
               "<div [dot.name]='v'></div>" +
               "<div [attr.someAttr]='v'></div>" +
               "<div [class.some-class]='v'></div>" +
               "<div [style.someStyle]='v'></div>" +
               "<div data-[style.someStyle]='v'></div>" +
               "<div bind-prop='v'></div>" +
               "<div prop='{{v}}'></div>" +
               "<div bind-animate-someAnimation='val'></div>" +
               "<div [@someAnimation]='v'></div>" +
               "<div @someAnimation='v'></div>");
  }

  public void testNgParseEvents() throws Exception {
    doTestHtml("<div (window:event)='v'></div>" +
               "<div (event)='v'></div>" +
               "<div data-(event)='v'></div>" +
               "<div (some-event)='v'></div>" +
               "<div (someEvent)='v'></div>" +
               "<div on-event='v'></div>");
  }

  public void testNgParseAnimationEvents() throws Exception {
    doTestHtml("<a (@click)='doStuff()'></a>" +
               "<b on-animate-click='doStuff()'></b>" +
               "<a (@click.done)='doStuff()'></a>" +
               "<b on-animate-click.start='doStuff()'></b>");
  }

  public void testNgParseReferences() throws Exception {
    doTestHtml("<div #a></div>" +
               "<div ref-a></div>" +
               "<div a #a='dirA'></div>" +
               "<div #a-b></div>" +
               "<div #></div>" +
               "<div ref- ></div>");
  }

  public void testNgParseVariables() throws Exception {
    doTestHtml("<div let-a></div>" +
               "<ng-template let-a='b'></ng-template>");
  }

  public void testNgParseInlineTemplates() throws Exception {
    doTestHtml("<div *ngIf></div>" +
               "<div *ngIf='condition'></div>" +
               "<div *ngIf='#a=b'>Report error on vars with #</div>" +
               "<div *ngIf='let a=b'></div>" +
               "<div data-*ngIf='let a=b'></div>" +
               "<div *ngIf='expr as local'></div>");
  }

  public void testNgReportErrorsInExpressions() throws Exception {
    doTestHtml("<div [prop]='a b'></div>");
  }

  public void testNgBindingAttributeComplex() throws Exception {
    doTestHtml("<div (lang)=\"{'current':i == (wordIndex | async)}\"></div>");
  }

  public void testNgCss() throws Exception {
    doTestHtml("<div *ngFor=\"let something of items\" class=\"inDaClass foo\" style=\"color: #fff\"></div>");
  }

  public void testNgBindingElvis() throws Exception {
    doTestHtml("<div lang=\"{{interpolation?.here}}\"></div>");
  }

  public void testNgBindingNullishCoalescing() throws Exception {
    doTestHtml("<div lang=\"{{interpolation ?? 'fallback'}}\"></div>");
  }

  public void testNgEntity() throws Exception {
    doTestHtml("<div>{{foo ? ' &mdash;' + bar : \"\"}}</div>");
  }

  public void testNgStringWithEntity() throws Exception {
    doTestHtml("{{ &quot;fo&#123;o\" }}");
  }

  public void testNgStringWithEntity2() throws Exception {
    doTestHtml("<div [input]='&apos;foo&quot;&dash;&apos;'");
  }

  public void testNgStringWithEntity3() throws Exception {
    doTestHtml("<div [input]='&apos;foo&quot;&dash;&apos;\"second\"'");
  }

  public void testNgWeb20713() throws Exception {
    doTestHtml("<h5>Last Updated: {{(viewModel.lastUpdated$ | async) | date:'mediumTime'}}</h5>");
  }

  public void testNgWeb24804() throws Exception {
    doTestHtml("<div *myStructuralDirective style=\"z-index: 10;\"></div>");
  }

  public void testNgTextInterpolation() throws Exception {
    doTestHtml("<div>my {{interpolated}} text</div>\n" +
               "<div>my{{interpolated}}text</div>\n" +
               "<div>my{{double}}{{interpolated}}text</div>\n" +
               "<div>my{{double}}double{{interpolated}}text</div>");
  }

  public void testNgTextInterpolationWithLineBreaks() throws Exception {
    doTestHtml("{{todo\n" +
               "            | started : status\n" +
               "            | search : term\n" +
               "            }}");
  }

  public void testNgIgnoredInterpolation() throws Exception {
    doTestHtml("this {{ is {{ <ignored/> interpolation }}");
  }

  public void testNgIgnoredInterpolationInTag() throws Exception {
    doTestHtml("<div>this{{is{{<ignored/> interpolation}}</div>another{{ignored{{<interpolation/>");
  }

  public void testNgInterpolationEmpty() throws Exception {
    doTestHtml("empty {{}} interpolation");
  }


  public void testNgScriptWithEventAndAngularAttr() throws Exception {
    doTestHtml("<script src=\"//example.com\" onerror=\"console.log(1)\" (error)='console.log(1)'" +
               "onload=\"console.log(1)\" (load)='console.log(1)'>\n" +
               "  console.log(2)\n" +
               "</script>\n" +
               "<div></div>");
  }

  public void testNgStyleTag() throws Exception {
    doTestHtml("<style>\n" +
               "  div {\n" +
               "  }\n" +
               "</style>\n" +
               "<div></div>");
  }

  public void testNgStyleAngularAttr() throws Exception {
    doTestHtml("<style (load)='disabled=true'>\n" +
               "  div {\n" +
               "  }\n" +
               "</style>\n" +
               "<div></div>");
  }

  public void testNgStyleWithEventAndAngularAttr() throws Exception {
    doTestHtml("<style (load)='disabled=true' onload=\"this.disabled=true\" (load)='disabled=true'>\n" +
               "  div {\n" +
               "  }\n" +
               "</style>\n" +
               "<div></div>");
  }

  public void testNgContentSelect() throws Exception {
    doTestHtml("<div><ng-content select='foo,bar'></ng-content></div>");
  }

  public void testNgNonBindable() throws Exception {
    doTestHtml("<div><span ngNonBindable>f{{bar}}a</span>f{{foo}}a</div>");
  }

  public void testNgNonBindable2() throws Exception {
    doTestHtml("<div><span ngNonBindable ngNonBindable>s{{bar}}e</span>s{{foo}}e</div>");
  }

  public void testNgNonBindable3() throws Exception {
    doTestHtml("<div><span ngNonBindable ngNonBindable>{{bar}}<b ngNonBindable>{{boo}}</b></span>{{foo}}</div>");
  }

  public void testNgNonBindable4() throws Exception {
    doTestHtml("<p ngNonBindable>{{foo}}<p>{{bar}}");
  }

  public void testNgNonQuotedAttrs() throws Exception {
    doTestHtml("<div (click)=doIt()></div>\n" +
               "<div [id]=foo></div>\n" +
               "<div #foo=bar></div>\n" +
               "<ng-content select=[header-content]></ng-content>\n");
  }

  public void testEmptyLetAndRef() throws Exception {
    doTestHtml("<ng-template let-/><div let-/><div #/><div ref-/>");
  }
}

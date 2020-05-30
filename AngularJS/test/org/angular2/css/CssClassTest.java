// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;
import static org.angular2.modules.Angular2TestModule.ANGULAR_COMMON_4_0_0;
import static org.angular2.modules.Angular2TestModule.configureLink;

public class CssClassTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "clazz";
  }

  @NotNull
  private PsiElement resolveReference(@NotNull String signature) {
    return AngularTestUtil.resolveReference(signature, myFixture);
  }


  public void testExternalReference() {
    myFixture.configureByFiles("cssExtRef.ts", "package.json", "css.css");
    PsiElement resolve = resolveReference("inDa<caret>Class");
    assertEquals("css.css", resolve.getContainingFile().getName());
  }

  public void testInternalReference() {
    myFixture.configureByFiles("cssIntRef.ts", "package.json");
    resolveReference("inDa<caret>Class");
  }

  public void testInternalReferenceWithHtmlTag() {
    myFixture.configureByFiles("cssIntRefHtmlTag.ts", "package.json");
    resolveReference("inDa<caret>Class");
  }

  public void testInternalReferenceExternalTemplate() {
    myFixture.configureByFiles("extTemplateRef.html", "extTemplateRef.ts", "package.json");
    resolveReference("inDa<caret>Class");
  }

  public void testInternalReferenceExternalTemplateHtmlTag() {
    myFixture.configureByFiles("extTemplateRefHtmlTag.html", "extTemplateRefHtmlTag.ts", "package.json");
    resolveReference("inDa<caret>Class");
  }

  public void testNonCliComplexScopeCodeCompletion() {
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css", "package.json");
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture);
    myFixture.completeBasic();
    assertSameElements(myFixture.getLookupElementStrings(),
                       "global-class",
                       "inline-class",
                       "simpleNameClass",
                       "internal-class",
                       "ext-html-class");
  }

  public void testNonCliComplexScopeCodeCompletionInline() {
    myFixture.configureByFiles("complex.ts", "complex-global.css", "complex-internal.css", "package.json");
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture);
    myFixture.completeBasic();
    assertSameElements(myFixture.getLookupElementStrings(),
                       "global-class",
                       "inline-class",
                       "internal-class",
                       "simpleNameClass",
                       "inline-html-class");
  }

  public void testCliComplexScopeCodeCompletion() {
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json", "package.json");
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture);
    myFixture.completeBasic();
    assertSameElements(myFixture.getLookupElementStrings(),
                       "cli-class",
                       "inline-class",
                       "internal-class",
                       "ext-html-class",
                       "simpleNameClass",
                       //"index-html-inline-class", - not supported yet
                       "index-html-link-class");
  }

  public void testCliComplexScopeCodeCompletionInline() {
    myFixture.configureByFiles("complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json", "package.json");
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture);
    myFixture.completeBasic();
    assertEquals(ContainerUtil.newArrayList("cli-class",
                                            "index-html-link-class",
                                            "inline-class",
                                            "inline-html-class",
                                            "internal-class",
                                            //"index-html-inline-class", - not supported yet
                                            "simpleNameClass"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()));
  }

  public void testNgClassCodeCompletion() {
    myFixture.configureByFiles("ngClass.html", "ngClass.css", "package.json");
    for (String prefix : asList("{", "[", "")) {
      AngularTestUtil.moveToOffsetBySignature("=\"" + prefix + "'foo1 b<caret>'", myFixture);
      myFixture.completeBasic();
      assertSameElements(myFixture.getLookupElementStrings(), "bar", "boo");
    }
    AngularTestUtil.moveToOffsetBySignature(", foo1: true<caret>}\"", myFixture);
    myFixture.type(",");
    myFixture.completeBasic();

    assertSameElements(myFixture.getLookupElementStrings(), "bar", "boo", "foo");
  }

  public void testNgClassReferences() {
    myFixture.configureByFiles("ngClass.html", "ngClass.css", "package.json");
    for (String prefix : asList("{", "[", "")) {
      AngularTestUtil.resolveReference("=\"" + prefix + "'fo<caret>o b", myFixture);
      AngularTestUtil.resolveReference("=\"" + prefix + "'foo b<caret>ar", myFixture);
      AngularTestUtil.assertUnresolvedReference("=\"" + prefix + "'f<caret>oo1 ", myFixture);
      AngularTestUtil.assertUnresolvedReference("=\"" + prefix + "'foo1 b<caret>", myFixture);
    }
    AngularTestUtil.resolveReference(", b<caret>ar: true}\"", myFixture);
    AngularTestUtil.assertUnresolvedReference(", f<caret>oo1: true}\"", myFixture);
  }

  public void testBoundClassCodeCompletion() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json");
    AngularTestUtil.moveToOffsetBySignature("<div class=\"\"<caret>></div>", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "[class.");
    myFixture.type("[class.");
    assertEquals(ContainerUtil.newArrayList(
      "cli-class",
      "ext-html-class",
      "index-html-link-class",
      "inline-class",
      "internal-class",
      "simpleNameClass"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()));
    myFixture.type("cli\n");
    assertEquals(AngularTestUtil.findOffsetBySignature("[class.cli-class]=\"<caret>\"", myFixture.getFile()),
                 myFixture.getCaretOffset());
  }

  public void testBoundClassCodeCompletionCanonical() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json");
    AngularTestUtil.moveToOffsetBySignature("<div class=\"\"<caret>></div>", myFixture);
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "[class.");
    myFixture.type("bind-");
    assertContainsElements(myFixture.getLookupElementStrings(), "class.");
    myFixture.type("class.");
    assertEquals(ContainerUtil.newArrayList(
      "cli-class",
      "ext-html-class",
      "index-html-link-class",
      "inline-class",
      "internal-class",
      "simpleNameClass"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()));
    myFixture.type("cli\n");
    assertEquals(AngularTestUtil.findOffsetBySignature("bind-class.cli-class=\"<caret>\"", myFixture.getFile()),
                 myFixture.getCaretOffset());
  }

  public void testClassCodeCompletionRun() {
    configureLink(myFixture, ANGULAR_COMMON_4_0_0);
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json");
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture);
    myFixture.completeBasic();
    myFixture.type("indexlc\n");
    AngularTestUtil.moveToOffsetBySignature("<div class=\"index-html-link-class\"<caret>>", myFixture);
    myFixture.type(" ");
    myFixture.completeBasic();
    myFixture.type("cla.\nintecl\ntrue");
    AngularTestUtil.moveToOffsetBySignature("[class.internal-class]=\"true\"<caret>>", myFixture);
    myFixture.type(" ");
    myFixture.completeBasic();
    assertContainsElements(myFixture.getLookupElementStrings(), "[ngClass]");
    myFixture.type("ngCl\n");
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    myFixture.type("{");
    myFixture.completeBasic();
    assertEquals(ContainerUtil.newArrayList(
      "cli-class",
      "ext-html-class",
      "index-html-link-class",
      "inline-class",
      "internal-class",
      "simpleNameClass"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()));
    myFixture.type("cli\n");
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    assertEquals(AngularTestUtil.findOffsetBySignature("'cli-class'<caret>", myFixture.getFile()),
                 myFixture.getCaretOffset());
    myFixture.type(": true, ");
    myFixture.completeBasic();
    assertSameElements(myFixture.getLookupElementStrings(),
                       "inline-class",
                       "internal-class",
                       "ext-html-class",
                       "index-html-link-class",
                       "simpleNameClass");
    myFixture.type("simpl\n");
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    assertEquals(AngularTestUtil.findOffsetBySignature(" simpleNameClass<caret>", myFixture.getFile()),
                 myFixture.getCaretOffset());
  }
}

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.containers.ContainerUtil
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.modules.Angular2TestModule
import org.angular2.modules.Angular2TestModule.Companion.configureLink
import org.angularjs.AngularTestUtil

class CssClassTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "clazz"
  }

  private fun resolveReference(signature: String): PsiElement {
    return AngularTestUtil.resolveReference(signature, myFixture)
  }

  fun testExternalReference() {
    myFixture.configureByFiles("cssExtRef.ts", "package.json", "css.css")
    val resolve = resolveReference("inDa<caret>Class")
    assertEquals("css.css", resolve.getContainingFile().getName())
  }

  fun testInternalReference() {
    myFixture.configureByFiles("cssIntRef.ts", "package.json")
    resolveReference("inDa<caret>Class")
  }

  fun testInternalReferenceWithHtmlTag() {
    myFixture.configureByFiles("cssIntRefHtmlTag.ts", "package.json")
    resolveReference("inDa<caret>Class")
  }

  fun testInternalReferenceExternalTemplate() {
    myFixture.configureByFiles("extTemplateRef.html", "extTemplateRef.ts", "package.json")
    resolveReference("inDa<caret>Class")
  }

  fun testInternalReferenceExternalTemplateHtmlTag() {
    myFixture.configureByFiles("extTemplateRefHtmlTag.html", "extTemplateRefHtmlTag.ts", "package.json")
    resolveReference("inDa<caret>Class")
  }

  fun testNonCliComplexScopeCodeCompletion() {
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css", "package.json")
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture)
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!,
                                      "global-class",
                                      "inline-class",
                                      "simpleNameClass",
                                      "internal-class",
                                      "ext-html-class")
  }

  fun testNonCliComplexScopeCodeCompletionInline() {
    myFixture.configureByFiles("complex.ts", "complex-global.css", "complex-internal.css", "package.json")
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture)
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!,
                                      "global-class",
                                      "inline-class",
                                      "internal-class",
                                      "simpleNameClass",
                                      "inline-html-class")
  }

  fun testCliComplexScopeCodeCompletion() {
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json", "package.json")
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture)
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!,
                                      "cli-class",
                                      "inline-class",
                                      "internal-class",
                                      "ext-html-class",
                                      "simpleNameClass",  //"index-html-inline-class", - not supported yet
                                      "index-html-link-class")
  }

  fun testCliComplexScopeCodeCompletionInline() {
    myFixture.configureByFiles("complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json", "package.json")
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture)
    myFixture.completeBasic()
    assertEquals(listOf("cli-class",
                        "index-html-link-class",
                        "inline-class",
                        "inline-html-class",
                        "internal-class",  //"index-html-inline-class", - not supported yet
                        "simpleNameClass"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
  }

  fun testNgClassCodeCompletion() {
    myFixture.configureByFiles("ngClass.html", "ngClass.css", "package.json")
    for (prefix in mutableListOf("{", "[", "")) {
      AngularTestUtil.moveToOffsetBySignature("=\"$prefix'foo1 b<caret>'", myFixture)
      myFixture.completeBasic()
      UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!, "bar", "boo")
    }
    AngularTestUtil.moveToOffsetBySignature(", foo1: true<caret>}\"", myFixture)
    myFixture.type(",")
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!, "bar", "boo", "foo")
  }

  fun testNgClassReferences() {
    myFixture.configureByFiles("ngClass.html", "ngClass.css", "package.json")
    for (prefix in mutableListOf("{", "[", "")) {
      AngularTestUtil.resolveReference("=\"$prefix'fo<caret>o b", myFixture)
      AngularTestUtil.resolveReference("=\"$prefix'foo b<caret>ar", myFixture)
      AngularTestUtil.assertUnresolvedReference("=\"$prefix'f<caret>oo1 ", myFixture)
      AngularTestUtil.assertUnresolvedReference("=\"$prefix'foo1 b<caret>", myFixture)
    }
    AngularTestUtil.resolveReference(", b<caret>ar: true}\"", myFixture)
    AngularTestUtil.assertUnresolvedReference(", f<caret>oo1: true}\"", myFixture)
  }

  fun testBoundClassCodeCompletion() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json")
    AngularTestUtil.moveToOffsetBySignature("<div class=\"\"<caret>></div>", myFixture)
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "[class.")
    myFixture.type("[class.")
    assertEquals(listOf("cli-class]",
                        "ext-html-class]",
                        "index-html-link-class]",
                        "inline-class]",
                        "internal-class]",
                        "simpleNameClass]"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
    myFixture.type("cli\n")
    assertEquals(AngularTestUtil.findOffsetBySignature("[class.cli-class]=\"<caret>\"", myFixture.getFile()),
                 myFixture.getCaretOffset())
  }

  fun testBoundClassCodeCompletionCanonical() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_4_0_0)
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json")
    AngularTestUtil.moveToOffsetBySignature("<div class=\"\"<caret>></div>", myFixture)
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "[class.")
    myFixture.type("bind-")
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "class.")
    myFixture.type("class.")
    assertEquals(listOf("cli-class",
                        "ext-html-class",
                        "index-html-link-class",
                        "inline-class",
                        "internal-class",
                        "simpleNameClass"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
    myFixture.type("cli\n")
    assertEquals(AngularTestUtil.findOffsetBySignature("bind-class.cli-class=\"<caret>\"", myFixture.getFile()),
                 myFixture.getCaretOffset())
  }

  fun testClassCodeCompletionRun() {
    configureLink(myFixture, Angular2TestModule.ANGULAR_COMMON_15_1_5)
    myFixture.configureByFiles("complex.html", "complex.ts", "complex-global.css", "complex-internal.css",
                               "complex-cli.css", "complex-cli-index.html", "complex-cli-index.css",
                               "angular.json")
    AngularTestUtil.moveToOffsetBySignature("<div class=\"<caret>\">", myFixture)
    myFixture.completeBasic()
    myFixture.type("indexlc\n")
    AngularTestUtil.moveToOffsetBySignature("<div class=\"index-html-link-class\"<caret>>", myFixture)
    myFixture.type(" ")
    myFixture.completeBasic()
    myFixture.type("cla.\nintecl\ntrue")
    AngularTestUtil.moveToOffsetBySignature("[class.internal-class]=\"true\"<caret>>", myFixture)
    myFixture.type(" ")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "[ngClass]")
    UsefulTestCase.assertDoesntContain(myFixture.getLookupElementStrings()!!, "ngClass")
    myFixture.type("ngCl\n")
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    myFixture.type("{")
    myFixture.completeBasic()
    assertEquals(listOf("cli-class",
                        "ext-html-class",
                        "index-html-link-class",
                        "inline-class",
                        "internal-class",
                        "simpleNameClass"),
                 ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
    myFixture.type("cli\n")
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    assertEquals(AngularTestUtil.findOffsetBySignature("'cli-class'<caret>", myFixture.getFile()),
                 myFixture.getCaretOffset())
    myFixture.type(": true, ")
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(myFixture.getLookupElementStrings()!!,
                                      "cli-class",  //TODO: remove - CSS reference actually does not filter already present class names, though it should
                                      "inline-class",
                                      "internal-class",
                                      "ext-html-class",
                                      "index-html-link-class",
                                      "simpleNameClass")
    myFixture.type("simpl\n")
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    assertEquals(AngularTestUtil.findOffsetBySignature(" simpleNameClass<caret>", myFixture.getFile()),
                 myFixture.getCaretOffset())
  }
}

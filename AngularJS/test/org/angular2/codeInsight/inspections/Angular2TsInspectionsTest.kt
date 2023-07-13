// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.inspections.*
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.SyntaxTraverser
import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angularjs.AngularTestUtil
import java.util.function.Consumer

/**
 * @see Angular2DecoratorInspectionsTest
 * @see Angular2TemplateInspectionsTest
 */
class Angular2TsInspectionsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "inspections/ts"
  }

  fun testUnusedSymbol() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java,
                                JSUnusedLocalSymbolsInspection::class.java)
    myFixture.configureByFiles("unused.ts", "unused.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testUnusedSetter() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java,
                                JSUnusedLocalSymbolsInspection::class.java)
    myFixture.configureByFiles("unusedSetter.ts", "unusedSetter.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testMethodCanBeStatic() {
    val canBeStaticInspection = JSMethodCanBeStaticInspection()
    JSTestUtils.setInspectionHighlightLevel(project, canBeStaticInspection, HighlightDisplayLevel.WARNING, testRootDisposable)
    myFixture.enableInspections(canBeStaticInspection)
    myFixture.configureByFiles("methodCanBeStatic.ts", "methodCanBeStatic.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testUnterminated() {
    myFixture.enableInspections(UnterminatedStatementJSInspection::class.java)
    myFixture.configureByFiles("unterminated.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testUnusedReference() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java,
                                JSUnusedLocalSymbolsInspection::class.java)
    myFixture.configureByFiles("unusedReference.html", "unusedReference.ts", "package.json")
    myFixture.checkHighlighting()
    for (attrToRemove in mutableListOf("notUsedRef", "anotherNotUsedRef", "notUsedRefWithAttr", "anotherNotUsedRefWithAttr")) {
      myFixture.moveToOffsetBySignature("<caret>$attrToRemove")
      myFixture.launchAction(myFixture.findSingleIntention("Remove unused variable '$attrToRemove'"))
    }
    myFixture.checkResultByFile("unusedReference.after.html")
  }

  fun testId() {
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection::class.java,
                                JSUnusedGlobalSymbolsInspection::class.java)
    myFixture.configureByFiles("object.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testPipeAndArgResolution() {
    myFixture.enableInspections(TypeScriptUnresolvedReferenceInspection::class.java)
    myFixture.configureByFiles("pipeAndArgResolution.html", "lowercase_pipe.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testHtmlTargetWithInterpolation() {
    myFixture.enableInspections(HtmlUnknownTargetInspection::class.java)
    myFixture.configureByFiles("htmlTargetWithInterpolation.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testGlobalThisInspection() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("top-level-this.html", "top-level-this.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testComplexGenerics() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("complex-generics.html", "complex-generics.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testDuplicateDeclarationOff() {
    myFixture.enableInspections(JSDuplicatedDeclarationInspection())
    myFixture.configureByFiles("duplicateDeclarationOff.html", "duplicateDeclarationOff.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testDuplicateDeclarationOffTemplate() {
    myFixture.enableInspections(JSDuplicatedDeclarationInspection())
    myFixture.configureByFiles("duplicateDeclarationOffLocalTemplate.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testNestedComponentClasses() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("nested-classes.html", "nested-classes.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testEmptyVarDefinition() {
    myFixture.enableInspections(JSUnusedLocalSymbolsInspection())
    myFixture.configureByFiles("package.json")
    myFixture.configureByText("template.html", "<ng-template ngFor let- ></ng-template>")
    myFixture.checkHighlighting()
  }

  fun testMissingLabelSuppressed() {
    myFixture.enableInspections(HtmlFormInputWithoutLabelInspection())
    myFixture.configureByFiles("missingLabelSuppressed.html", "missingLabelSuppressed.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testDeprecated() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("deprecated.html", "deprecated.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testDeprecatedInline() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("deprecated.ts", "package.json")
    loadInjectionsAndCheckHighlighting()
  }

  fun testNgAcceptInputType() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("ngAcceptInputType.ts", "package.json")
    myFixture.checkHighlighting()
  }

  private fun loadInjectionsAndCheckHighlighting() {
    val data = ExpectedHighlightingData(
      myFixture.getEditor().getDocument(), true, true, false, true)
    data.init()
    EdtTestUtil.runInEdtAndWait<RuntimeException> { PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments() }
    val injectedLanguageManager = InjectedLanguageManager.getInstance(myFixture.getProject())
    // We need to ensure that injections are cached before we check deprecated highlighting
    SyntaxTraverser.psiTraverser(myFixture.getFile())
      .forEach(
        Consumer { it: PsiElement? -> if (it is PsiLanguageInjectionHost) injectedLanguageManager.getInjectedPsiFiles(it) })
    (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
  }
}

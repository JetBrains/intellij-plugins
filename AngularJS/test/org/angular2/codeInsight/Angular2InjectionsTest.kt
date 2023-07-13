// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.UIUtil
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.context.WebSymbolsContextProvider
import com.intellij.webSymbols.context.impl.WebSymbolsContextProviderExtensionPoint
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.resolveReference
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureCopy
import org.angular2.lang.Angular2LangUtil.isAngular2Context
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings
import org.angularjs.AngularTestUtil

class Angular2InjectionsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "codeInsight/injections"
  }

  fun testAngular2EmptyInterpolation() {
    myFixture.testCompletion("emptyInterpolation.html", "emptyInterpolation.after.html",
                             "package.json", "emptyInterpolation.ts")
  }

  fun testAngular2NonEmptyInterpolation() {
    myFixture.testCompletion("nonEmptyInterpolation.html", "nonEmptyInterpolation.after.html",
                             "package.json", "nonEmptyInterpolation.ts")
  }

  fun testEventHandler2Resolve() {
    myFixture.configureByFiles("event.html", "package.json", "event.ts")
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
  }

  fun testEventHandlerPrivate2Resolve() {
    myFixture.configureByFiles("event_private.html", "package.json", "event_private.ts")
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
  }

  fun testNgIfResolve() {
    myFixture.configureByFiles("ngIf.ts", "ng_if.ts", "package.json")
    checkVariableResolve("my_use<caret>r.last", "my_user", JSVariable::class.java)
  }

  private fun <T : JSElement?> checkVariableResolve(signature: String, varName: String, varClass: Class<T>) {
    AngularTestUtil.checkVariableResolve(signature, varName, varClass, myFixture)
  }

  fun testStyles2() {
    myFixture.configureByFiles("custom.ts", "package.json")
    val offset = AngularTestUtil.findOffsetBySignature("Helvetica <caret>Neue", myFixture.getFile())
    val element = InjectedLanguageManager.getInstance(project).findInjectedElementAt(myFixture.getFile(), offset)
    assertEquals(CSSLanguage.INSTANCE, element!!.getLanguage())
  }

  fun testHost() {
    myFixture.configureByFiles("host.ts", "package.json")
    for (signature in listOf(Pair("eve<caret>nt", Angular2Language.INSTANCE),
                             Pair("bind<caret>ing", Angular2Language.INSTANCE),
                             Pair("at<caret>tribute", JavaScriptSupportLoader.TYPESCRIPT))) {
      val offset = AngularTestUtil.findOffsetBySignature(signature.first, myFixture.getFile())
      var element = InjectedLanguageManager.getInstance(
        project).findInjectedElementAt(myFixture.getFile(), offset)
      if (element == null) {
        element = myFixture.getFile().findElementAt(offset)
      }
      assertEquals(signature.first, signature.second, element!!.getContainingFile().getLanguage())
    }
  }

  fun testNonAngular() {
    myFixture.configureByFiles("nonAngularComponent.ts", "package.json")
    for (signature in listOf(Pair("<foo><caret></foo>", HTMLLanguage.INSTANCE),
                             Pair("eve<caret>nt", JavaScriptSupportLoader.TYPESCRIPT),
                             Pair("bind<caret>ing", JavaScriptSupportLoader.TYPESCRIPT),
                             Pair("at<caret>tribute", JavaScriptSupportLoader.TYPESCRIPT))) {
      val offset = AngularTestUtil.findOffsetBySignature(signature.first, myFixture.getFile())
      var element = InjectedLanguageManager.getInstance(
        project).findInjectedElementAt(myFixture.getFile(), offset)
      if (element == null) {
        element = myFixture.getFile().findElementAt(offset)
      }
      assertEquals(signature.first, signature.second, element!!.getContainingFile().getLanguage())
    }
  }

  fun testNgForExternalCompletion() {
    myFixture.testCompletion("ngFor.html", "ngFor.after.html", "package.json")
  }

  fun testNgForExternalResolve() {
    myFixture.configureByFiles("ngFor.after.html", "ngFor.ts", "ng_for_of.ts", "package.json")
    checkVariableResolve("\"myTo<caret>do\"", "myTodo", JSVariable::class.java)
  }

  fun testNgForInlineCompletion() {
    myFixture.testCompletion("ngFor.ts", "ngFor.after.ts", "package.json")
  }

  fun testNgForInlineResolve() {
    myFixture.configureByFiles("ngFor.after.ts", "ng_for_of.ts", "package.json")
    checkVariableResolve("\"myTo<caret>do\"", "myTodo", JSVariable::class.java)
  }

  fun `test$EventExternalCompletion`() {
    myFixture.testCompletion("\$event.html", "\$event.after.html", "package.json")
  }

  fun `test$EventInlineCompletion`() {
    myFixture.testCompletion("\$event.ts", "\$event.after.ts", "package.json")
  }

  fun testUserSpecifiedInjection() {
    myFixture.configureByFiles("userSpecifiedLang.ts", "package.json")
    for (signature in listOf(Pair("<div><caret></div>", Angular2HtmlLanguage.INSTANCE.id),
                             Pair("\$text<caret>-color", "SCSS"),  //fails if correct order of injectors is not ensured
                             Pair("color: <caret>#00aa00", CSSLanguage.INSTANCE.id))) {
      val offset = AngularTestUtil.findOffsetBySignature(signature.first, myFixture.getFile())
      val element = InjectedLanguageManager.getInstance(
        project).findInjectedElementAt(myFixture.getFile(), offset)
      assertEquals(signature.first, signature.second, element!!.getContainingFile().getLanguage().id)
    }
  }

  fun testNoInjectionInHTMLTemplateLiteral() {
    myFixture.configureByFiles("noInjection.html", "package.json")
    val offset = AngularTestUtil.findOffsetBySignature("b<caret>ar", myFixture.getFile())
    assert(offset > 0)
    val injection = InjectedLanguageManager.getInstance(project).findInjectedElementAt(myFixture.getFile(), offset)
    assertNull("There should be no injection", injection)
  }

  fun testPrivateMembersOrder() {
    myFixture.configureByFiles("event_private.html", "event_private.ts", "package.json")
    myFixture.completeBasic()
    assertEquals("Private members should be sorted after public ones",
                 listOf("_callApi", "callSecuredApi", "callZ", "callA", "callAnonymousApi"),
                 AngularTestUtil.renderLookupItems(myFixture, false, false, true))
  }

  fun testResolutionWithDifferentTemplateName() {
    myFixture.configureByFiles("event_different_name2.html", "package.json", "event_different_name.ts")
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
  }

  fun testIntermediateFoldersWithPackageJson1() {
    myFixture.configureByFiles("inner/event.html", "package.json", "inner/package.json", "inner/event.ts")
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
  }

  fun testIntermediateFoldersWithPackageJson2() {
    val files = myFixture.configureByFiles("inner/event.html", "inner/package.json", "inner/event.ts",
                                           "inner2/event.html", "inner2/package.json", "inner2/event.ts")
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("callAnonymous<caret>Api()", myFixture.getFile())
    assertNull(myFixture.getFile().findReferenceAt(offsetBySignature))
    myFixture.openFileInEditor(files[3].getViewProvider().getVirtualFile())
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
  }

  fun testNodeModulesBasedInclusionCheck1() {
    myFixture.copyDirectoryToProject("node-modules-check", ".")
    myFixture.configureFromTempProjectFile("inner/event.html")
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
    myFixture.configureFromTempProjectFile("inner2/event.html")
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
  }

  fun testAngularCliLibrary() {
    myFixture.copyDirectoryToProject("angular-cli-lib", ".")
    configureCopy(myFixture, Angular2TestModule.ANGULAR_L10N_4_2_0)

    // Add "dist" folder to excludes
    ModuleRootModificationUtil.updateModel(module) { model: ModifiableRootModel ->
      val folder = model.getContentEntries()[0].addExcludeFolder(
        myFixture.getTempDirFixture().getFile("dist")!!)
      Disposer.register(myFixture.projectDisposable) {
        ModuleRootModificationUtil.updateModel(
          module) { model2: ModifiableRootModel -> model2.getContentEntries()[0].removeExcludeFolder(folder) }
      }
    }

    // This should trigger immediate library update
    TypeScriptTestUtil.waitForLibraryUpdate(myFixture)

    // Ensure we are set up correctly
    ReadAction.run<RuntimeException> {
      assertTrue(ProjectFileIndex.getInstance(project).isExcluded(
        myFixture.getTempDirFixture().getFile("dist/check")!!))
      assertTrue(ProjectFileIndex.getInstance(project).isInLibrary(
        myFixture.getTempDirFixture().getFile("dist/my-common-ui-lib/package.json")!!))
    }
    myFixture.configureFromTempProjectFile("src/app/home.component.html")
    val transform = myFixture.resolveReference("| trans<caret>late")
    assertEquals("translate.pipe.d.ts", transform.getContainingFile().getName())
    UsefulTestCase.assertInstanceOf(transform, TypeScriptFunction::class.java)
  }

  fun testCustomContextProvider() {
    val disposable = Disposer.newDisposable()
    WebSymbolsContext.WEB_SYMBOLS_CONTEXT_EP
      .point!!
      .registerExtension(
        WebSymbolsContextProviderExtensionPoint(WebSymbolsContext.KIND_FRAMEWORK, "angular", object : WebSymbolsContextProvider {
          override fun isEnabled(project: Project, directory: VirtualFile): CachedValueProvider.Result<Int?> {
            return CachedValueProvider.Result.create(1, ModificationTracker.EVER_CHANGED)
          }
        }),
        disposable)
    myFixture.configureByFiles("inner/event.html", "inner/package.json", "inner/event.ts")
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
    Disposer.dispose(disposable)

    // Force reload of roots
    isAngular2Context(myFixture.getFile())
    UIUtil.dispatchAllInvocationEvents()
    val offsetBySignature = AngularTestUtil.findOffsetBySignature("callAnonymous<caret>Api()", myFixture.getFile())
    assertNull(myFixture.getFile().findReferenceAt(offsetBySignature))
  }

  fun testTemplateReferencedThroughImportStatement() {
    myFixture.configureByFiles("event_private.html", "package.json", "event_private.import.ts")
    checkVariableResolve("callAnonymous<caret>Api()", "callAnonymousApi", TypeScriptFunction::class.java)
  }

  fun testUnclosedTemplateAttribute() {
    myFixture.configureByFiles("unclosedTemplate.html", "package.json")
    UsefulTestCase.assertInstanceOf(
      myFixture.getFile().findElementAt(myFixture.getCaretOffset())!!.getParent(),
      Angular2HtmlTemplateBindings::class.java)
  }

  fun testMultiPartTemplateString() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java,
                                JSUnusedLocalSymbolsInspection::class.java)
    myFixture.configureByFiles("multipart-template-string.ts", "package.json")
    myFixture.checkHighlighting(true, false, true)
  }

  fun testCompletionOnTemplateReferenceVariable() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_4_0_0)
    myFixture.configureByFiles("ref-var.html", "ref-var.ts")
    val defaultProps: List<String> = mutableListOf("constructor", "hasOwnProperty", "isPrototypeOf", "propertyIsEnumerable",
                                                   "toLocaleString", "toString", "valueOf")
    for (check in listOf(
      Pair("comp", ContainerUtil.prepend(defaultProps, "myCompProp")),
      Pair("dir", ContainerUtil.prepend(defaultProps, "myDirectiveProp")),
      Pair("template", ContainerUtil.prepend(defaultProps, "createEmbeddedView", "elementRef"))
    )) {
      myFixture.moveToOffsetBySignature(check.first + "Ref.<caret>")
      myFixture.completeBasic()
      assertEquals("Issue with " + check.first,
                   ContainerUtil.sorted(check.second),
                   ContainerUtil.sorted(myFixture.getLookupElementStrings()!!))
    }
  }

  fun testInterpolationTyping() {
    myFixture.configureByFiles("interpolation.html", "package.json")
    myFixture.type("{{foo")
    val doc = myFixture.getDocument(myFixture.getFile())
    assertEquals("<div>{{foo}}</div>", doc.text)
    WriteAction.runAndWait<RuntimeException> {
      PsiDocumentManager.getInstance(
        project).commitDocument(doc)
    }
    myFixture.type("}}bar")
    assertEquals("<div>{{foo}}bar</div>", doc.text)
  }

  fun testTopLevelThisCompletion() {
    myFixture.configureByFiles("top-level-this.ts", "package.json")
    myFixture.completeBasic()
    UsefulTestCase.assertSameElements(ContainerUtil.sorted(myFixture.getLookupElementStrings()!!), ContainerUtil.sorted(
      mutableListOf("getSomething", "\$any", "ref1", "title")))
  }
}

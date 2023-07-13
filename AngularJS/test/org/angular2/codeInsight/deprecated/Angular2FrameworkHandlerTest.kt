// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.containers.ContainerUtil
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2ComponentLocator.findComponentClasses
import org.angular2.entities.Angular2ComponentLocator.findComponentClassesInFile
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2FrameworkHandler
import org.angular2.entities.Angular2Module
import org.angular2.inspections.AngularMissingOrInvalidDeclarationInModuleInspection
import org.angularjs.AngularTestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2FrameworkHandlerTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "codeInsight/deprecated/frameworkHandler"
  }

  fun testAdditionalComponentClasses() {
    val disposable = Disposer.newDisposable()
    Disposer.register(myFixture.testRootDisposable, disposable)
    Angular2FrameworkHandler.EP_NAME.point.registerExtension(object : Angular2FrameworkHandler {
      override fun findAdditionalComponentClasses(context: PsiFile): List<TypeScriptClass> {
        val componentsFile = PsiManager.getInstance(project).findFile(myFixture.findFileInTempDir("components.ts"))
        return findComponentClassesInFile(
          componentsFile!!) { cls: TypeScriptClass, dec: ES6Decorator? -> "FooComponent" == cls.getName() }
      }
    }, disposable)
    myFixture.configureByFiles("components.ts", "package.json")
    myFixture.configureByText("test.html", "{{ <caret> }}")
    UsefulTestCase.assertSize(1, findComponentClasses(myFixture.getFile()))
    myFixture.completeBasic()
    UsefulTestCase.assertOrderedEquals(ContainerUtil.sorted(AngularTestUtil.renderLookupItems(myFixture, false, false, true)),
                                       "\$any", "fooField1", "fooField2")
    Disposer.dispose(disposable)
    UsefulTestCase.assertSize(0, findComponentClasses(myFixture.getFile()))
    myFixture.completeBasic()
    UsefulTestCase.assertOrderedEquals(ContainerUtil.sorted(AngularTestUtil.renderLookupItems(myFixture, false, false, true)),
                                       "\$any")
  }

  fun testSelectModuleForDeclarationsScope() {
    val disposable = Disposer.newDisposable()
    Disposer.register(myFixture.testRootDisposable, disposable)
    Angular2FrameworkHandler.EP_NAME.point.registerExtension(object : Angular2FrameworkHandler {
      override fun selectModuleForDeclarationsScope(modules: Collection<Angular2Module>,
                                                    component: Angular2Component,
                                                    context: PsiFile): Angular2Module {
        return modules.first { it.getName() == "FooModule" }
      }
    }, disposable)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("multi-module.ts", "package.json")
    myFixture.configureByText("template.html",
                              """
                                <app-foo></app-foo>
                                <<error descr="Component or directive matching app-bar element is out of scope of the current template">app-bar</error>></app-bar>
                                <<warning descr="Unknown html tag foobar">foobar</warning>></<warning descr="Unknown html tag foobar">foobar</warning>>
                                """.trimIndent())
    myFixture.checkHighlighting()
    Disposer.dispose(disposable)
    myFixture.configureByText("template.html",
                              """
                                <<error descr="Component or directive matching app-foo element is out of scope of the current template">app-foo</error>></app-foo>
                                <app-bar></app-bar>
                                <<warning descr="Unknown html tag foobar">foobar</warning>></<warning descr="Unknown html tag foobar">foobar</warning>>
                                """.trimIndent())
    myFixture.checkHighlighting()
  }

  fun testSuppressModuleInspectionErrors() {
    Angular2FrameworkHandler.EP_NAME.point.registerExtension(object : Angular2FrameworkHandler {
      override fun suppressModuleInspectionErrors(modules: Collection<Angular2Module>,
                                                  declaration: Angular2Declaration): Boolean {
        return "FooComponent" == declaration.getName()
      }
    }, myFixture.testRootDisposable)
    myFixture.enableInspections(AngularMissingOrInvalidDeclarationInModuleInspection())
    myFixture.configureByFiles("multi-module-errors.ts", "package.json")
    myFixture.checkHighlighting()
  }
}

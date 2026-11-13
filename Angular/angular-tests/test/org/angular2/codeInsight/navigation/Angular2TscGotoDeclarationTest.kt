// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.typescript.getNavigationFromService
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import com.intellij.testFramework.PlatformTestUtil
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule.ANGULAR_CORE_20_1_4
import org.angular2.Angular2TestModule.NGRX_SIGNALS_20_1_0
import org.angular2.Angular2TsConfigFile
import org.angular2.SkipTsGoProxy
import org.angular2.SkipTsKotlin
import org.angular2.SkipTsNode
import org.angular2.TestTsGoProxy
import org.angular2.TestTsKotlin
import org.angular2.TestTsKotlinLs
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoProxy
@TestTsKotlin
@TestTsKotlinLs
class Angular2TscGotoDeclarationTest : Angular2TestCase("navigation/declaration") {

  @Test
  fun testSignalStore() = doGotoDeclarationTest("<caret>books: Book[];",
                                                ANGULAR_CORE_20_1_4, NGRX_SIGNALS_20_1_0,
                                                configurators = listOf(Angular2TsConfigFile()))

  /** Only the TSKotlin language service gives the navigation for an element of a template. */
  @Test
  @SkipTsNode
  @SkipTsGoProxy
  @SkipTsKotlin
  fun testLetDeclarationFromService() = doConfiguredTest(ANGULAR_CORE_20_1_4, configurators = listOf(Angular2TsConfigFile())) {
    val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
    val hostEditor = InjectedLanguageEditorUtil.getTopLevelEditor(editor)
    val reference = injectedLanguageManager.findInjectedElementAt(injectedLanguageManager.getTopLevelFile(file), hostEditor.caretModel.offset)!!
    val expected = (reference.parent as JSReferenceExpression).resolve()!!
    val targets = PlatformTestUtil.callOnBgtSynchronously({ runBlockingMaybeCancellable { readAction { getNavigationFromService(project, reference, hostEditor) } } }, 10)
    assertEquals(listOf(expected).map(::describe), targets.orEmpty().map(::describe))
  }

  private fun describe(element: PsiElement): String =
    "${element.javaClass.simpleName} '${element.text}' at ${InjectedLanguageManager.getInstance(element.project).injectedToHost(element, element.textRange)}"

}
// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.ide.IdeView
import com.intellij.ide.actions.TestDialogBuilder.TestAnswers
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.polySymbols.testFramework.enableIdempotenceChecksOnEveryCache
import com.intellij.psi.PsiDirectory
import org.jetbrains.vuejs.VueCreateFromTemplateHandler.Companion.VUE_COMPOSITION_API_TEMPLATE_NAME
import org.jetbrains.vuejs.VueTestCase

class VueNewComponentTest : VueTestCase("new_component") {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get PolySymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  fun testCompositionComponentWithJsLang() {
    doNewComponentCheck(componentName = "MyJsLabel")
  }

  fun testCompositionComponentWithTsLang() {
    doNewComponentCheck(componentName = "MyTsLabel")
  }

  fun testVaporComponentWithJsLang() {
    doNewComponentCheck(componentName = "MyVaporJsLabel")
  }

  fun testVaporComponentWithTsLang() {
    doNewComponentCheck(componentName = "MyVaporTsLabel")
  }

  private fun doNewComponentCheck(
    componentName: String,
  ) {
    doConfiguredTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      checkResult = true,
      configureFileName = "App.vue",
    ) {
      createNewComponent(
        name = componentName,
        directory = psiManager.findDirectory(findFileInTempDir("core/components"))!!,
      )
    }
  }

  private fun createNewComponent(
    name: String,
    directory: PsiDirectory,
  ) {
    val action = ActionManager.getInstance()
      .getAction("CreateVueSingleFileComp")

    val event = AnActionEvent.createEvent(
      action,
      createDataContext(name, directory),
      null,
      ActionPlaces.PROJECT_VIEW_POPUP,
      ActionUiKind.POPUP,
      null,
    )

    action.actionPerformed(event)
  }

  private fun createDataContext(
    name: String,
    directory: PsiDirectory,
  ): DataContext {
    return SimpleDataContext.builder()
      .add(CommonDataKeys.PROJECT, project)
      .add(TestAnswers.KEY, TestAnswers(name, VUE_COMPOSITION_API_TEMPLATE_NAME))
      .add(LangDataKeys.IDE_VIEW, object : IdeView {
        override fun getDirectories(): Array<out PsiDirectory> =
          arrayOf(directory)

        override fun getOrChooseDirectory(): PsiDirectory =
          directory
      })
      .build()
  }
}


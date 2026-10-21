// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.ide.IdeView
import com.intellij.ide.actions.TestDialogBuilder.TestAnswers
import com.intellij.lang.typescript.compiler.preventTypeScriptServiceRestartOnContextChange
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.VueCreateFromTemplateHandler.Companion.VUE_COMPOSITION_API_TEMPLATE_NAME
import org.jetbrains.vuejs.VueFixture
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.vueFixture
import org.junit.jupiter.api.Test

@TestApplication
class VueNewComponentTest :
  VueNewComponentTestBase() {

  @TestApplication
  class WithTsGoProxyTest :
    VueNewComponentTestBase(testMode = VueTestMode.TS_GO_PROXY)

  @TestApplication
  class WithoutServiceTest :
    VueNewComponentTestBase(testMode = VueTestMode.NO_PLUGIN)
}

abstract class VueNewComponentTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) {

  protected val vue: VueFixture by vueFixture(testMode) { VueNewComponentFixture(it, testMode) }

  @Test
  fun testCompositionComponentWithJsLang() {
    doNewComponentCheck(componentName = "MyJsLabel")
  }

  @Test
  fun testCompositionComponentWithTsLang() {
    doNewComponentCheck(componentName = "MyTsLabel")
  }

  @Test
  fun testVaporComponentWithJsLang() {
    doNewComponentCheck(componentName = "MyVaporJsLabel")
  }

  @Test
  fun testVaporComponentWithTsLang() {
    doNewComponentCheck(componentName = "MyVaporTsLabel")
  }

  private fun doNewComponentCheck(
    componentName: String,
  ) {
    // The new TypeScript component changes the PolyContext, and the change restarts the TypeScript service on the EDT.
    // The test runs off the EDT, so the restart can run during the test.
    preventTypeScriptServiceRestartOnContextChange {
      vue.doConfiguredTest(
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
      .add(CommonDataKeys.PROJECT, vue.project)
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

/**
 * The state of [VueNewComponentTestBase]: a [VueFixture] that checks the idempotence of each cache and adds no `tsconfig.json` file.
 */
private class VueNewComponentFixture(
  context: HybridFixtureContext,
  testMode: VueTestMode,
) : VueFixture(context, "new_component", testMode) {

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get PolySymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  // TEMP WA
  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    super.adjustConfigurators(configurators)
      .filterNot { it is VueTsConfigFile }
}

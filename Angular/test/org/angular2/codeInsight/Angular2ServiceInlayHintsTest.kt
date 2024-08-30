package org.angular2.codeInsight

import com.intellij.codeInsight.hints.InlineInlayRenderer
import com.intellij.codeInsight.hints.declarative.PsiPointerInlayActionPayload
import com.intellij.codeInsight.hints.declarative.impl.DeclarativeInlayRenderer
import com.intellij.codeInsight.hints.declarative.impl.TextInlayPresentationEntry
import com.intellij.lang.typescript.editing.TypeScriptServiceInlayHintsService
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile

class Angular2ServiceInlayHintsTest : Angular2TestCase("inlayHints", true) {

  fun testBasic() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    configurators = listOf(Angular2TsConfigFile())
  ) {
    checkInlayHints()
  }

  fun testExternalTemplate() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    dir = true,
    extension = "html",
    configurators = listOf(Angular2TsConfigFile())
  ) {
    checkInlayHints()
  }

  private fun checkInlayHints() {
    TypeScriptServiceInlayHintsService.testInlayHints(testRootDisposable)
    myFixture.testInlays(
      {
        when (val renderer = it.renderer) {
          is DeclarativeInlayRenderer -> renderer.presentationList.getEntries()
            .joinToString("") { entry ->
              val text = (entry as TextInlayPresentationEntry).text
              val pointer = (entry.clickArea?.actionData?.payload as? PsiPointerInlayActionPayload)?.pointer
              val isLib = pointer?.virtualFile?.path?.contains("/lib.") == true

              // Don't include nav to libs to avoid test failure on bundle update
              "$text${if (!isLib) pointer?.range ?: "" else ""}"
            }
          is InlineInlayRenderer -> renderer.toString()
          else -> renderer.toString()
        }

      }, { hint ->
        hint.renderer.let { it is DeclarativeInlayRenderer || it is InlineInlayRenderer }
      })
  }
}
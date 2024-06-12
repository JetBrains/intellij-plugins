package org.angular2.codeInsight

import com.google.gson.GsonBuilder
import com.intellij.javascript.web.WebFrameworkTestModule
import com.intellij.webSymbols.checkTextByFile
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder

class Angular2TemplateTranspilerTest : Angular2TestCase("templateTranspiler") {

  fun testBasic() =
    checkTranspilation(Angular2TestModule.ANGULAR_CORE_16_2_8,
                       Angular2TestModule.ANGULAR_COMMON_16_2_8,
                       Angular2TestModule.ANGULAR_ROUTER_16_2_8, dir = true)

  fun testCssClassBinding() =
    checkTranspilation(Angular2TestModule.ANGULAR_CORE_16_2_8,
                       Angular2TestModule.ANGULAR_COMMON_16_2_8)

  private fun checkTranspilation(
    vararg modules: WebFrameworkTestModule,
    dir: Boolean = false,
  ) {
    doConfiguredTest(*modules, dir = dir, configurators = listOf(
      Angular2TsConfigFile(strictTemplates = true)
    )) {
      val transpiledFile = Angular2TranspiledComponentFileBuilder.buildTranspiledComponentFile(myFixture.file)
                           ?: throw IllegalStateException("Cannot build transpiled file")

      val fileText = myFixture.file.text
      assert(transpiledFile.generatedCode.startsWith(fileText)) {
        "Generated code does not start with the original file contents"
      }

      checkTextByFile(
        transpiledFile.generatedCode.substring(fileText.length),
        if (dir) "${testName}/tcb._ts" else "$testName.tcb._ts"
      )

      checkTextByFile(
        GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(transpiledFile.mappings.map { fileInfo ->
          mapOf("file-name" to fileInfo.sourceFile.name,
                "mappings" to fileInfo.sourceMappings
                  .map { mapping ->
                    "${mapping.sourceOffset}:${mapping.sourceOffset + mapping.sourceLength} => " +
                    "${mapping.generatedOffset}:${mapping.generatedOffset + mapping.generatedLength}" +
                    (if (mapping.ignoreDiagnostics) " (ignoreDiagnostics)" else "")
                  })
        }),
        if (dir) "${testName}/mappings.json" else "$testName.mappings.json"
      )
    }
  }
}
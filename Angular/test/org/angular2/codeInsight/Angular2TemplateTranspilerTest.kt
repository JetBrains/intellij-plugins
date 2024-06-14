package org.angular2.codeInsight

import com.google.gson.GsonBuilder
import com.intellij.javascript.web.WebFrameworkTestModule
import com.intellij.openapi.util.text.StringUtil
import com.intellij.webSymbols.checkTextByFile
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.lang.html.tcb.Angular2TranspiledComponentFileBuilder

class Angular2TemplateTranspilerTest : Angular2TestCase("templateTranspiler") {

  fun testBasic() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_16_2_8,
    Angular2TestModule.ANGULAR_COMMON_16_2_8,
    Angular2TestModule.ANGULAR_ROUTER_16_2_8,
    dir = true,
  )

  fun testTemplateColorsHtml() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_16_2_8,
    Angular2TestModule.ANGULAR_COMMON_16_2_8,
    Angular2TestModule.ANGULAR_FORMS_16_2_8,
    dir = true,
    configureFileName = "colors.ts"
  )

  fun testNgAcceptInputTypeOverride() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_16_2_8,
  )

  fun testHostDirectives() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_16_2_8,
    dir = true,
  )

  private fun checkTranspilation(
    vararg modules: WebFrameworkTestModule,
    dir: Boolean = false,
    configureFileName: String = "$testName.ts",
  ) {
    doConfiguredTest(*modules, dir = dir, configureFileName = configureFileName, configurators = listOf(
      Angular2TsConfigFile(strictTemplates = true)
    )) {
      val componentFile = myFixture.file
      val transpiledFile = Angular2TranspiledComponentFileBuilder.buildTranspiledComponentFile(componentFile)
                           ?: throw IllegalStateException("Cannot build transpiled file")

      val fileText = componentFile.text
      assert(transpiledFile.generatedCode.startsWith(fileText)) {
        "Generated code does not start with the original file contents"
      }

      val prefixLength = StringUtil.skipWhitespaceOrNewLineForward(transpiledFile.generatedCode, fileText.length)
      checkTextByFile(
        transpiledFile.generatedCode.substring(prefixLength),
        if (dir) "${testName}/tcb._ts" else "$testName.tcb._ts"
      )

      checkTextByFile(
        GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(transpiledFile.mappings.map { fileInfo ->
          val sourceFileText = fileInfo.sourceFile.text
          mapOf("file-name" to fileInfo.sourceFile.name,
                "mappings" to fileInfo.sourceMappings
                  .map { mapping ->
                    (if (mapping.generatedOffset >= prefixLength)
                      "«${sourceFileText.substring(mapping.sourceOffset, mapping.sourceOffset + mapping.sourceLength)}» [${mapping.sourceOffset}] => " +
                      "«${transpiledFile.generatedCode.substring(mapping.generatedOffset, mapping.generatedOffset + mapping.generatedLength)}» [${mapping.generatedOffset - prefixLength}]}"
                    else {
                      "${mapping.sourceOffset}:${mapping.sourceOffset + mapping.sourceLength} => " +
                      "${mapping.generatedOffset}:${mapping.generatedOffset + mapping.generatedLength} (source)"
                    }) + (if (mapping.ignoreDiagnostics) " (ignoreDiagnostics)" else "")
                  })
        }),
        if (dir) "${testName}/mappings.json" else "$testName.mappings.json"
      )
    }
  }
}
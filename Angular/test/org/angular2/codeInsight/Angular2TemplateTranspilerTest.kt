package org.angular2.codeInsight

import com.google.gson.GsonBuilder
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.openapi.util.text.StringUtil
import com.intellij.webSymbols.testFramework.checkTextByFile
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMappingFlag
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder

class Angular2TemplateTranspilerTest : Angular2TestCase("templateTranspiler", true) {

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

  fun testSafeAccess() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  fun testModelSignals() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  fun testBlockDefer() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  fun testBlockFor() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  fun testBlockIf() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  fun testBlockSwitch() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  fun testEscapedString() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  fun testDirectiveReferences() = checkTranspilation(
    Angular2TestModule.ANGULAR_MATERIAL_17_3_0,
    Angular2TestModule.ANGULAR_CORE_17_3_0,
    Angular2TestModule.ANGULAR_COMMON_17_3_0,
    Angular2TestModule.ANGULAR_FORMS_17_3_0,
  )

  fun testBlockLet() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_18_2_1,
    Angular2TestModule.ANGULAR_COMMON_18_2_1,
  )

  fun testInputSignal() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
    Angular2TestModule.ANGULAR_COMMON_17_3_0,
  )

  fun testTypeof() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4
  )

  fun testObjectInitializer() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_18_2_1,
    Angular2TestModule.ANGULAR_COMMON_18_2_1,
    Angular2TestModule.RXJS_7_8_1,
    dir = true
  )

  fun testNumberValueAccessor() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
    Angular2TestModule.ANGULAR_FORMS_17_3_0,
  )

  fun testHostBindings() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  fun testEs6ShorthandProperty() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4,
  )

  fun testStructuralDirective() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_18_2_1,
    Angular2TestModule.ANGULAR_COMMON_18_2_1,
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
      val transpiledFile = Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveFile(componentFile)
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
        GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(transpiledFile.fileMappings.map { (_, fileInfo) ->
          val sourceFileText = fileInfo.sourceFile.text

          fun rangeToText(text: String, offset: Int, length: Int, offsetPrefix: Int = 0) =
            "«${text.substring(offset, offset + length)}» [${offset - offsetPrefix}]"

          mapOf("file-name" to fileInfo.sourceFile.name,
                "mappings" to fileInfo.sourceMappings
                  .map { mapping ->
                    val result = StringBuilder()
                    if (mapping.generatedOffset >= prefixLength)
                      result.append(rangeToText(sourceFileText, mapping.sourceOffset, mapping.sourceLength))
                        .append(" => ")
                        .append(rangeToText(transpiledFile.generatedCode, mapping.generatedOffset, mapping.generatedLength, prefixLength))
                        .append("}")
                    else
                      result.append(mapping.sourceOffset).append(":").append(mapping.sourceOffset + mapping.sourceLength).append(" => ")
                        .append(mapping.generatedOffset).append(":").append(mapping.generatedOffset + mapping.generatedLength).append(" (source)")

                    if (mapping.flags.contains(SourceMappingFlag.REVERSE_TYPES) && mapping.flags.size == 1) {
                      result.append(" (only reverse types)")
                    }
                    else {
                      result.append(
                        when {
                          mapping.diagnosticsOffset == mapping.sourceOffset && mapping.diagnosticsLength == mapping.diagnosticsLength -> ""
                          mapping.ignored -> " (ignored)"
                          mapping.diagnosticsOffset == null -> " (no diagnostics)"
                          else -> " (diagnostics: " + rangeToText(sourceFileText, mapping.diagnosticsOffset!!, mapping.diagnosticsLength!!) + ")"
                        })
                      if (!mapping.flags.contains(SourceMappingFlag.TYPES) && !mapping.ignored)
                        result.append(" (no types)")
                      if (!mapping.flags.contains(SourceMappingFlag.SEMANTIC) && !mapping.ignored)
                        result.append(" (no semantic)")
                      if (mapping.flags.contains(SourceMappingFlag.REVERSE_TYPES) && !mapping.flags.contains(SourceMappingFlag.TYPES))
                        result.append(" (reverse types)")
                    }
                    result.toString()
                  })
        }),
        if (dir) "${testName}/mappings.json" else "$testName.mappings.json"
      )
    }
  }
}
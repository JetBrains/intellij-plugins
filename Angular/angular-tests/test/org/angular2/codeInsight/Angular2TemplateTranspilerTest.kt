package org.angular2.codeInsight

import com.google.gson.GsonBuilder
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.openapi.util.text.StringUtil
import com.intellij.polySymbols.testFramework.checkTextByFile
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.ANGULAR_CORE_20_1_4
import org.angular2.Angular2TsConfigFile
import org.angular2.TestTsGoFork
import org.angular2.TestTsNode
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMappingFlag
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import org.junit.Test

@TestTsNode
@TestTsGoFork
class Angular2TemplateTranspilerTest : Angular2TestCase("templateTranspiler") {

  @Test
  fun testBasic() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_16_2_8,
    Angular2TestModule.ANGULAR_COMMON_16_2_8,
    Angular2TestModule.ANGULAR_ROUTER_16_2_8,
    dir = true,
  )

  @Test
  fun testTemplateColorsHtml() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_16_2_8,
    Angular2TestModule.ANGULAR_COMMON_16_2_8,
    Angular2TestModule.ANGULAR_FORMS_16_2_8,
    dir = true,
    configureFileName = "colors.ts"
  )

  @Test
  fun testNgAcceptInputTypeOverride() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_16_2_8,
  )

  @Test
  fun testNgAcceptInputTypeOverrideGenericDirectives() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_20_1_4,
    dir = true,
    configureFileName = "app.component.ts"
  )

  @Test
  fun testHostDirectives() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_16_2_8,
    dir = true,
  )

  @Test
  fun testSafeAccess() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  @Test
  fun testModelSignals() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  @Test
  fun testBlockDefer() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  @Test
  fun testBlockFor() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  @Test
  fun testBlockIf() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  @Test
  fun testBlockSwitch() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  @Test
  fun testEscapedString() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  @Test
  fun testDirectiveReferences() = checkTranspilation(
    Angular2TestModule.ANGULAR_MATERIAL_17_3_0,
    Angular2TestModule.ANGULAR_CORE_17_3_0,
    Angular2TestModule.ANGULAR_COMMON_17_3_0,
    Angular2TestModule.ANGULAR_FORMS_17_3_0,
  )

  @Test
  fun testBlockLet() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_18_2_1,
    Angular2TestModule.ANGULAR_COMMON_18_2_1,
  )

  @Test
  fun testInputSignal() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
    Angular2TestModule.ANGULAR_COMMON_17_3_0,
  )

  @Test
  fun testTypeof() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_2_0
  )

  @Test
  fun testObjectInitializer() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_18_2_1,
    Angular2TestModule.ANGULAR_COMMON_18_2_1,
    Angular2TestModule.RXJS_7_8_1,
    dir = true
  )

  @Test
  fun testNumberValueAccessor() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
    Angular2TestModule.ANGULAR_FORMS_17_3_0,
  )

  @Test
  fun testHostBindings() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_17_3_0,
  )

  @Test
  fun testEs6ShorthandProperty() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_2_0,
  )

  @Test
  fun testStructuralDirective() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_18_2_1,
    Angular2TestModule.ANGULAR_COMMON_18_2_1,
  )

  @Test
  fun testTemplateLiteralExternal() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_2_0,
    Angular2TestModule.ANGULAR_COMMON_19_2_0,
    dir = true,
    configureFileName = "templateLiteral.ts"
  )

  @Test
  fun testTemplateLiteralUnclosed() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_2_0,
    Angular2TestModule.ANGULAR_COMMON_19_2_0,
    dir = true,
    configureFileName = "templateLiteral.ts"
  )

  @Test
  fun testTemplateLiteralInline() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_2_0,
    Angular2TestModule.ANGULAR_COMMON_19_2_0,
    configureFileName = "templateLiteralInline.ts"
  )

  @Test
  fun testCreateComponentBindings() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_20_0_0_NEXT_3
  )

  @Test
  fun testNgTemplateWithStructuralDirective() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_2_0,
    Angular2TestModule.ANGULAR_COMMON_19_2_0,
  )

  @Test
  fun testPower() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_20_0_0_NEXT_3
  )

  @Test
  fun testVoidKeyword() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_20_0_0_NEXT_3
  )

  @Test
  fun testVoidKeywordNg19() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_19_2_0
  )

  @Test
  fun testAssignmentOperators() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_20_1_4
  )

  @Test
  fun testGenericDirectiveWithExtendsNonNullable() =
    checkTranspilation(
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      dir = true,
    )

  @Test
  fun testListenerInNestedIfBlocks() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_20_1_4
  )

  @Test
  fun testNewAnimateEvents() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_20_2_2
  )

  @Test
  fun testAnyInCallArgs() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_20_2_2
  )

  @Test
  fun testLibraryWithSignals() =
    checkTranspilation(ANGULAR_CORE_20_1_4, configureFileName = "my-component.ts", dir = true)

  @Test
  fun testSpreadSyntax() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_1_3
  )

  @Test
  fun testArrowFunctions() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_2_0, dir = true
  )

  @Test
  fun testArrowFunctionDefinedLet() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_2_0
  )

  @Test
  fun testArrowFunctionDollarEvents() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_2_0
  )

  @Test
  fun testArrowFunctionHostBinding() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_2_0
  )

  @Test
  fun testArrowFunctionLoopVariables() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_2_0
  )

  @Test
  fun testArrowFunctionSafeAccess() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_2_0
  )

  @Test
  fun testRegexes() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_0_9
  )

  @Test
  fun testRegexesExt() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_0_9, dir = true
  )

  @Test
  fun testRelativeImports() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_0_9, Angular2TestModule.IONIC_ANGULAR_7_7_3
  )

  @Test
  fun testExtendedKeyEvents() = checkTranspilation(
    Angular2TestModule.ANGULAR_CORE_21_0_9
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
      val prefixLength = if (transpiledFile.generatedCode.startsWith(fileText))
        StringUtil.skipWhitespaceOrNewLineForward(transpiledFile.generatedCode, fileText.length)
      else
        0

      checkTextByFile(
        transpiledFile.generatedCode.substring(prefixLength),
        if (dir) "${testName}/tcb._ts" else "$testName.tcb._ts"
      )

      checkTextByFile(
        GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(transpiledFile.fileMappings.map { (_, fileInfo) ->
          val sourceFileText = fileInfo.sourceFile.text

          fun rangeToText(text: String, offset: Int, length: Int, offsetPrefix: Int = 0) =
            "«${text.substring(offset, offset + length)}» [${offset - offsetPrefix}]"

          val result = mutableMapOf<String, Any>()
          result["file-name"] = fileInfo.sourceFile.name
          if (fileInfo.externalFile)
            result["external-file"] = true
          result["mappings"] = fileInfo.sourceMappings
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
            }
          result
        }),
        if (dir) "${testName}/mappings.json" else "$testName.mappings.json"
      )
    }
  }
}
package org.angular2.codeInsight

import com.intellij.webSymbols.checkTextByFile
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.codeInsight.config.Angular2Compiler
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2Pipe
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.lang.html.tcb.*

class Angular2TemplateTranspilerTest : Angular2TestCase("templateTranspiler") {

  fun testBasic() =
    doConfiguredTest(Angular2TestModule.ANGULAR_COMMON_16_2_8, Angular2TestModule.ANGULAR_ROUTER_16_2_8, dir = true, configurators = listOf(
      Angular2TsConfigFile(strictTemplates = true)
    )) {
      val transpiledTemplate = Angular2TemplateTranspiler.transpileTemplate(Angular2EntitiesProvider.getComponent(myFixture.elementAtCaret)!!)

      checkTextByFile(
        transpiledTemplate.generatedCode,
        "${testName}/result.ts"
      )
    }

}
package org.intellij.plugin.mdx

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection
import com.sixrr.inspectjs.validity.ThisExpressionReferencesGlobalObjectJSInspection


abstract class MdxTestBase : BasePlatformTestCase() {
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        myFixture.enableInspections(HtmlUnknownTagInspection(),
                HtmlUnknownAttributeInspection(),
                BadExpressionStatementJSInspection(),
                ThisExpressionReferencesGlobalObjectJSInspection(),
                HtmlUnknownBooleanAttributeInspection()
        )
    }

    override fun getTestDataPath(): String {
        return "src/test/testData"
    }
}
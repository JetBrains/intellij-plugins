package org.intellij.plugin.mdx

import com.intellij.codeInsight.actions.OptimizeImportsAction
import com.intellij.ide.DataManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import junit.framework.TestCase
import org.junit.Test

class MdxTest : MdxTestBase() {

    @Test
    fun testResolve() {
        myFixture.configureByText("my.mdx", "export const hello = \"hello\"")
        myFixture.configureByText("test.mdx", "import {hello} from \'my.mdx\'\n<div>{h<caret>ello}</div>")
        val ref = myFixture.getReferenceAtCaretPosition()
        TestCase.assertNotNull(ref?.resolve())
    }

    @Test
    fun testFindUsages() {
        val usageInfos = myFixture.testFindUsages("FindUsagesTestData.mdx", "FindUsagesTestData.kt")
        TestCase.assertEquals(1, usageInfos.size)
    }

    @Test
    fun testOptimizeImports() {
        myFixture.configureByFile("OptimizeImportsTestData.mdx")
        OptimizeImportsAction.actionPerformedImpl(DataManager.getInstance().getDataContext(myFixture.editor.contentComponent))
        FileDocumentManager.getInstance().saveAllDocuments()
        myFixture.checkResultByFile("DefaultTestData.mdx")
    }

    @Test
    fun testFoldingImports() {
        doTestFolding()
    }

    @Test
    fun testFoldingOneImport() {
        doTestFolding()
    }

    @Test
    fun testFoldingMultilineImport() {
        doTestFolding()
    }

    private fun doTestFolding() {
        myFixture.testFoldingWithCollapseStatus(testDataPath + "/" + getTestName(false) + ".mdx")
    }
}
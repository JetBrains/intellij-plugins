package org.intellij.plugin.mdx

import com.intellij.codeInsight.actions.OptimizeImportsAction
import com.intellij.ide.DataManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class MdxTest : MdxTestBase() {

    @Test
    fun testResolve() {
        myFixture.configureByText("my.mdx", "export const hello = \"hello\"")
        myFixture.configureByText("test.mdx", "import {hello} from \'my.mdx\'\n<div>{h<caret>ello}</div>")
        val ref = myFixture.getReferenceAtCaretPosition()
        assertNotNull(ref?.resolve())
    }

    @Test
    fun testFindUsages() {
        val usageInfos = myFixture.testFindUsages("FindUsagesTestData.mdx", "FindUsagesTestData.kt")
        assertEquals(1, usageInfos.size)
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
        val testFile = File(testDataPath, getTestName(false) + ".mdx")
        val expected = FileUtil.loadFile(testFile).replace("\r", "")
        myFixture.configureByText(testFile.name, CodeInsightTestFixtureImpl.removeFoldingMarkers(expected))
        val actual = (myFixture as CodeInsightTestFixtureImpl)
            .getFoldingDescription(true, false)
            .normalizeMdxFoldingDescription()
        assertEquals(expected, actual)
    }

    private fun String.normalizeMdxFoldingDescription(): String {
        // The folding fixture marker format cannot represent single quotes inside a placeholder attribute.
        return replace(MULTILINE_IMPORT_FROM_FOLD_TEXT, "text='...'")
    }

    private companion object {
        private val MULTILINE_IMPORT_FROM_FOLD_TEXT = Regex("text='from\\n'[^\\n]*''")
    }
}

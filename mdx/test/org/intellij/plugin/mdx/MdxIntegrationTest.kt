package org.intellij.plugin.mdx

import com.intellij.codeInsight.actions.OptimizeImportsAction
import com.intellij.ide.DataManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.junit.jupiter.api.Test

/** Integration tests for IDE features that span multiple subsystems: resolve, reformat, find-usages, folding. */
@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData")
class MdxIntegrationTest : MdxTestBase() {

    @Test
    fun testResolve() {
        myFixture.configureByText("my.mdx", "export const hello = \"hello\"")
        myFixture.configureByText("test.mdx", "import {hello} from 'my.mdx'\n<div>{h<caret>ello}</div>")
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
        myFixture.testFolding("$testDataPath/$testName.mdx")
    }

    @Test
    fun testFoldingOneImport() {
        myFixture.testFolding("$testDataPath/$testName.mdx")
    }

    @Test
    fun testFoldingMultilineImport() {
        val virtualFile = myFixture.copyFileToProject("$testName.mdx")
        val psiFile = myFixture.psiManager.findFile(virtualFile) ?: error("No PSI file for $virtualFile")
        val expected = psiFile.text.replace("\r", "")
        myFixture.configureByText("$testName.mdx", CodeInsightTestFixtureImpl.removeFoldingMarkers(expected))
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

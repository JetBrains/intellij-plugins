package org.intellij.prisma.completion

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.TestLookupElementPresentation
import com.intellij.webSymbols.testFramework.checkLookupItems
import com.intellij.webSymbols.testFramework.noAutoComplete
import org.intellij.prisma.PrismaTestCase
import org.intellij.prisma.ide.documentation.PrismaDocumentationProvider
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.PrismaFileType
import org.intellij.prisma.reformatDocumentation

abstract class PrismaCompletionTestBase(testCasePath: String) : PrismaTestCase(testCasePath) {
  protected fun completeSelected(
    source: String,
    expected: String,
    selected: String,
    additionalFile: String? = null,
  ): Array<LookupElement> {
    return completeAndGetLookupElements(source, expected, selected, additionalFile)
  }

  protected fun getLookupElements(source: String): Array<LookupElement> {
    return completeAndGetLookupElements(source, source)
  }

  private fun completeAndGetLookupElements(
    source: String,
    expected: String,
    selected: String? = null,
    additionalFile: String? = null,
  ): Array<LookupElement> {
    return noAutoComplete {
      myFixture.configureByText(PrismaFileType, source)
      if (!additionalFile.isNullOrEmpty()) {
        myFixture.addFileToProject("additionalSchema.prisma", additionalFile)
      }
      val lookupElements = myFixture.completeBasic()
      assertNotNull(lookupElements)
      if (selected != null) {
        val selectedItem = myFixture.lookupElements?.find { it.lookupString == selected }
        assertNotNull(selectedItem)
        myFixture.lookup.currentItem = selectedItem
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        myFixture.checkResult(expected)
      }
      lookupElements
    }
  }

  protected fun completeBasic(source: String, expected: String) {
    myFixture.configureByText(PrismaFileType, source)
    myFixture.completeBasic()
    myFixture.checkResult(expected)
  }

  protected fun noCompletion(source: String) {
    UsefulTestCase.assertSize(0, completeAndGetLookupElements(source, source))
  }

  protected fun checkLookupDocumentation(lookupElements: Array<LookupElement>, lookupString: String) {
    checkLookupDocumentation(lookupElements.find { it.lookupString == lookupString })
  }

  protected fun checkLookupDocumentation(lookupElement: LookupElement?) {
    val element = lookupElement?.psiElement
    assertNotNull(element)
    val doc = PrismaDocumentationProvider().generateDoc(element!!, null)?.let {
      assertNotNull(it)
      reformatDocumentation(project, it)
    }
    assertSameLinesWithFile("${testDataPath}/${getTestFileName("html")}", doc!!)
  }

  protected fun checkLookupElementsInSplitSchema(fileDir: String? = null, includePrimitive: Boolean = false) {
    doConfiguredTest(dir = true, configureFileName = "${if (fileDir != null) "$fileDir/" else ""}$testName.$defaultExtension") {
      checkLookupItems(
        renderPriority = true,
        renderTypeText = true,
        expectedDataLocation = testName,
        lookupItemFilter = {
          if (!includePrimitive) {
            it.lookupString !in PrismaConstants.PrimitiveTypes.ALL
          }
          else {
            true
          }
        }
      )
    }
  }

  protected val Array<LookupElement>?.strings: List<String>
    get() = this?.map { it.lookupString } ?: emptyList()

  protected fun Array<LookupElement>?.find(lookupString: String): LookupElement? =
    this?.find { it.lookupString == lookupString }

  protected val LookupElement?.presentation: LookupElementPresentation?
    get() = this?.let { TestLookupElementPresentation.renderReal(it) }
}
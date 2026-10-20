package org.intellij.prisma.completion

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.polySymbols.testFramework.checkLookupItems
import com.intellij.polySymbols.testFramework.noAutoComplete
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.TestLookupElementPresentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.PrismaTestCase
import org.intellij.prisma.ide.documentation.PrismaDocumentationProvider
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.PrismaFileType
import org.intellij.prisma.reformatDocumentation
import org.junit.jupiter.api.Assertions.assertNotNull

abstract class PrismaCompletionTestBase(testCasePath: String) : PrismaTestCase(testCasePath) {
  protected suspend fun completeSelected(
    source: String,
    expected: String,
    selected: String,
    additionalFile: String? = null,
  ): Array<LookupElement> {
    return completeAndGetLookupElements(source, expected, selected, additionalFile)
  }

  protected suspend fun getLookupElements(source: String): Array<LookupElement> {
    return completeAndGetLookupElements(source, source)
  }

  private suspend fun completeAndGetLookupElements(
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
        withContext(Dispatchers.EDT) {
          myFixture.lookup.currentItem = selectedItem
          myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        }
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

  protected suspend fun noCompletion(source: String) {
    assertThat(completeAndGetLookupElements(source, source)).isEmpty()
  }

  protected suspend fun checkLookupDocumentation(lookupElements: Array<LookupElement>, lookupString: String) {
    checkLookupDocumentation(lookupElements.find { it.lookupString == lookupString })
  }

  protected suspend fun checkLookupDocumentation(lookupElement: LookupElement?) {
    val generated = readAction {
      val element = lookupElement?.psiElement
      assertNotNull(element)
      PrismaDocumentationProvider().generateDoc(element!!, null)
    }
    val doc = generated?.let {
      assertNotNull(it)
      reformatDocumentation(project, it)
    }
    PlatformTestUtil.assertSameLinesWithFile("${testDataPath}/${getTestFileName("html")}", doc!!)
  }

  protected fun checkLookupElementsInSplitSchema(fileDir: String? = null, includePrimitive: Boolean = false) {
    web.doConfiguredTest(dir = true, configureFileName = "${if (fileDir != null) "$fileDir/" else ""}${web.testName}.${web.defaultExtension}") {
      checkLookupItems(
        renderPriority = true,
        renderTypeText = true,
        expectedDataLocation = web.testName,
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
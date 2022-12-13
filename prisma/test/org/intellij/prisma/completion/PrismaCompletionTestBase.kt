package org.intellij.prisma.completion

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.TestLookupElementPresentation
import junit.framework.TestCase
import org.intellij.prisma.PrismaTestCase
import org.intellij.prisma.ide.documentation.PrismaDocumentationProvider
import org.intellij.prisma.lang.PrismaFileType

abstract class PrismaCompletionTestBase : PrismaTestCase() {

  protected fun completeSelected(
    source: String,
    expected: String,
    selected: String,
  ): Array<LookupElement> {
    return completeAndGetLookupElements(source, expected, selected)
  }

  protected fun getLookupElements(source: String): Array<LookupElement> {
    return completeAndGetLookupElements(source, source)
  }

  private fun completeAndGetLookupElements(
    source: String,
    expected: String,
    selected: String? = null
  ): Array<LookupElement> {
    return withoutAutoCompletion {
      myFixture.configureByText(PrismaFileType, source)
      val lookupElements = myFixture.completeBasic()
      TestCase.assertNotNull(lookupElements)
      if (selected != null) {
        val selectedItem = myFixture.lookupElements?.find { it.lookupString == selected }
        TestCase.assertNotNull(selectedItem)
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
    val doc = PrismaDocumentationProvider().generateDoc(element!!, null)
    assertNotNull(doc)
    assertSameLinesWithFile("${testDataPath}/${getTestName("html")}", doc!!)
  }

  protected fun <T> withoutAutoCompletion(block: () -> T): T {
    val before = CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION
    CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = false
    try {
      return block()
    }
    finally {
      CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = before
    }
  }

  protected val Array<LookupElement>?.strings: List<String>
    get() = this?.map { it.lookupString } ?: emptyList()

  protected fun Array<LookupElement>?.find(lookupString: String): LookupElement? =
    this?.find { it.lookupString == lookupString }

  protected val LookupElement?.presentation: LookupElementPresentation?
    get() = this?.let { TestLookupElementPresentation.renderReal(it) }
}
package org.intellij.prisma.completion

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.HybridTestMode
import com.intellij.polySymbols.testFramework.checkLookupItems
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.polySymbols.testFramework.noAutoComplete
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.TestLookupElementPresentation
import com.intellij.testFramework.junit5.fixture.TestFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.PrismaFixture
import org.intellij.prisma.ide.documentation.PrismaDocumentationProvider
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.PrismaFileType
import org.intellij.prisma.reformatDocumentation
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertNotNull

/**
 * Creates a [PrismaCompletionFixture] on the light project. [testCasePath] is the test data directory under the Prisma test data root.
 * Declare the result as an instance property.
 */
@TestOnly
fun prismaCompletionFixture(testCasePath: String): TestFixture<PrismaCompletionFixture> =
  hybridFixture({ HybridTestMode.BasePlatform }) { PrismaCompletionFixture(it, testCasePath) }

/**
 * A [PrismaFixture] with the completion helpers of the Prisma completion tests.
 * Call a member from a background thread.
 */
class PrismaCompletionFixture(context: HybridFixtureContext, testCasePath: String) : PrismaFixture(context, testCasePath) {
  suspend fun completeSelected(
    source: String,
    expected: String,
    selected: String,
    additionalFile: String? = null,
  ): Array<LookupElement> {
    return completeAndGetLookupElements(source, expected, selected, additionalFile)
  }

  suspend fun getLookupElements(source: String): Array<LookupElement> {
    return completeAndGetLookupElements(source, source)
  }

  private suspend fun completeAndGetLookupElements(
    source: String,
    expected: String,
    selected: String? = null,
    additionalFile: String? = null,
  ): Array<LookupElement> {
    return noAutoComplete {
      fixture.configureByText(PrismaFileType, source)
      if (!additionalFile.isNullOrEmpty()) {
        fixture.addFileToProject("additionalSchema.prisma", additionalFile)
      }
      val lookupElements = fixture.completeBasic()
      assertNotNull(lookupElements)
      if (selected != null) {
        val selectedItem = fixture.lookupElements?.find { it.lookupString == selected }
        assertNotNull(selectedItem)
        withContext(Dispatchers.EDT) {
          fixture.lookup.currentItem = selectedItem
          fixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        }
        fixture.checkResult(expected)
      }
      lookupElements
    }
  }

  fun completeBasic(source: String, expected: String) {
    fixture.configureByText(PrismaFileType, source)
    fixture.completeBasic()
    fixture.checkResult(expected)
  }

  suspend fun noCompletion(source: String) {
    assertThat(completeAndGetLookupElements(source, source)).isEmpty()
  }

  suspend fun checkLookupDocumentation(lookupElements: Array<LookupElement>, lookupString: String) {
    checkLookupDocumentation(lookupElements.find { it.lookupString == lookupString })
  }

  suspend fun checkLookupDocumentation(lookupElement: LookupElement?) {
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

  fun checkLookupElementsInSplitSchema(fileDir: String? = null, includePrimitive: Boolean = false) {
    doConfiguredTest(dir = true, configureFileName = "${if (fileDir != null) "$fileDir/" else ""}${testName}.${defaultExtension}") {
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
}

internal val Array<LookupElement>?.strings: List<String>
  get() = this?.map { it.lookupString } ?: emptyList()

internal fun Array<LookupElement>?.find(lookupString: String): LookupElement? =
  this?.find { it.lookupString == lookupString }

internal val LookupElement?.presentation: LookupElementPresentation?
  get() = this?.let { TestLookupElementPresentation.renderReal(it) }

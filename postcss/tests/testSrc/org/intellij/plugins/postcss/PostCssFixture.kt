package org.intellij.plugins.postcss

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightFixture
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.runInEdtAndGet
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.util.ArrayUtil
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail

/**
 * Creates a [PostCssFixture] on the module of [projectFixture].
 * [projectFixture] must come from `sharedCodeInsightProjectFixture()`.
 * Declare the result as an instance property, so that each test gets its own fixture.
 * The test data path comes from the `@TestDataPath` annotation of the test class.
 */
@TestOnly
fun postCssFixture(projectFixture: TestFixture<Project>): TestFixture<PostCssFixture> = testFixture("postCssFixture") { context ->
  val fixture = sharedCodeInsightFixture(projectFixture).init()
  initialized(PostCssFixture(fixture, PlatformTestUtil.getTestName(context.extensionContext.requiredTestMethod.name, true))) {}
}

/**
 * The helpers of the PostCSS tests.
 * Call a member from a background thread. A member takes the EDT calls that it needs.
 */
@TestOnly
class PostCssFixture internal constructor(
  val fixture: CodeInsightTestFixture,
  /** The name of the current test method without the `test` prefix, with a lowercase first letter. */
  val testName: String,
) {
  /** Completes at the caret of the file `<testName>.pcss`, and checks that the variants are the same set as [items]. */
  fun doTestCompletionVariants(vararg items: String) {
    runInEdtAndWait { fixture.testCompletionVariants("$testName.pcss", *items) }
  }

  /** Completes at the caret of the file `<testName>.pcss`, and checks that the lookup starts with [items]. */
  fun doTestPreferred(vararg items: String) {
    fixture.configureByFile("$testName.pcss")
    runInEdtAndWait {
      fixture.completeBasic()
      fixture.assertPreferredCompletionItems(0, *items)
    }
  }

  /** Completes at the caret of the file `<testName>.pcss`, and checks the item text and the type text of the first [items]. */
  fun doTestPreferred(vararg items: Pair<String, String>) {
    fixture.configureByFile("$testName.pcss")
    val lookupElements = runInEdtAndGet {
      fixture.completeBasic()
    }
    val presentation = LookupElementPresentation()
    for (i in items.indices) {
      ReadAction.computeBlocking<Unit, RuntimeException> { lookupElements[i].renderElement(presentation) }
      assertEquals(items[i].first, presentation.itemText)
      assertEquals(items[i].second, presentation.typeText)
    }
  }

  /**
   * Completes at the caret of the file `<testName>.pcss`.
   * Checks that the first items have the same priority, and that each of them is in [expectedItems].
   */
  fun doTestPreferredNotStrict(vararg expectedItems: Pair<String, String>) {
    fixture.configureByFile("$testName.pcss")
    val lookupElements = runInEdtAndGet {
      fixture.completeBasic()
    }
    val presentation = LookupElementPresentation()
    var priority = 0.0
    for (i in expectedItems.indices) {
      val lookupElement = lookupElements[i]
      if (lookupElement is PrioritizedLookupElement<*>) {
        val p = lookupElement.priority
        if (priority != 0.0 && priority != p) {
          fail<Unit>("There are lookup elements with different priorities among top ${expectedItems.size} completion items")
        }
        priority = p
      }

      ReadAction.computeBlocking<Unit, RuntimeException> { lookupElement.renderElement(presentation) }
      val actual = Pair.create(presentation.itemText, presentation.typeText)
      assertTrue(ArrayUtil.contains(actual, *expectedItems), "$actual is not expected among top ${expectedItems.size} completion items")
    }
  }

  /** Completes at the caret of the file `<testName>.pcss`, and compares the result with the file `<testName>_after.pcss`. */
  fun doTestCompletion() {
    runInEdtAndWait { fixture.testCompletion("$testName.pcss", "${testName}_after.pcss") }
  }

  /** Applies the single intention [message] in the file `<testName>.pcss`, and compares the result with `<testName>_after.pcss`. */
  fun doTestQuickFix(message: String) {
    fixture.configureByFile("$testName.pcss")
    val intentionAction = fixture.findSingleIntention(message)
    fixture.launchAction(intentionAction)
    fixture.checkResultByFile("${testName}_after.pcss")
  }

  /** Checks that the reference at the caret of the file `<testName>.<extension>` resolves to [count] valid results. */
  @JvmOverloads
  fun doTestResolve(count: Int = 1, extension: String = "pcss") {
    val reference = fixture.getReferenceAtCaretPosition("$testName.$extension")
    assertNotNull(reference)
    ReadAction.computeBlocking<Unit, RuntimeException> {
      val results = (reference as PsiPolyVariantReference).multiResolve(false)
      assertEquals(count, results.size)
      for (i in 0 until count) {
        assertTrue(results[i].isValidResult)
      }
    }
  }
}

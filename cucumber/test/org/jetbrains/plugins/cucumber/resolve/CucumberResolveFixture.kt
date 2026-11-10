// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.resolve

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestContext
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.function.Function

/**
 * Finds the references of the Cucumber steps in the test data, and checks the step definitions that they resolve to.
 * Each step definition language gives the name of a step definition with [stepDefinitionNameOf].
 * The helpers read the PSI in a read action, so a test can call them off the EDT.
 */
class CucumberResolveFixture(
  val fixture: CodeInsightTestFixture,
  private val stepDefinitionNameOf: Function<PsiElement, String?>,
  context: TestContext,
) {
  /** The name of the test method without the `test` prefix, with a lowercase first letter. */
  val testName: String = PlatformTestUtil.getTestName(context.extensionContext.requiredTestMethod.name, true)

  fun findOffsetBySignature(signature: String): Int = findOffsetBySignature(signature, fixture.file)

  fun findOffsetBySignature(signature: String, psiFile: PsiFile): Int {
    val caretOffset = signature.indexOf(CARET_STR)
    assert(caretOffset >= 0)
    val text = signature.substring(0, caretOffset) + signature.substring(caretOffset + CARET_STR.length)
    val pos = ReadAction.computeBlocking<Int, RuntimeException> {
      PsiDocumentManager.getInstance(fixture.project).getDocument(psiFile)!!.text.indexOf(text)
    }
    assertTrue(pos >= 0)
    return pos + caretOffset
  }

  fun findReferenceBySignature(signature: String): PsiReference? {
    val offset = findOffsetBySignature(signature)
    val file = fixture.file
    return ReadAction.computeBlocking<PsiReference?, RuntimeException> { file.findReferenceAt(offset) }
  }

  fun findPsiFileInTempDirBy(relPath: String): PsiFile? {
    val virtualFile = fixture.tempDirFixture.getFile(relPath)
    return ReadAction.computeBlocking<PsiFile?, RuntimeException> {
      if (virtualFile != null && !virtualFile.isDirectory) PsiManager.getInstance(fixture.project).findFile(virtualFile) else null
    }
  }

  fun checkReference(result: Array<ResolveResult>, stepDefinitionName: String?) {
    val ok = ReadAction.computeBlocking<Boolean, RuntimeException> {
      var ok = stepDefinitionName == null
      for (rr in result) {
        val resolvedElement = rr.element
        if (resolvedElement != null) {
          if (stepDefinitionName == null) {
            ok = false
          }
          else {
            val resolvedStepDefName = stepDefinitionNameOf.apply(resolvedElement)
            if (resolvedStepDefName != null && resolvedStepDefName == stepDefinitionName) {
              ok = true
              break
            }
          }
        }
      }
      ok
    }
    assertTrue(ok)
  }

  fun checkReference(element: String, stepDefinitionName: String?) {
    val result = getResolveResult(element)
    checkReference(result, stepDefinitionName)
  }

  fun getResolveResult(step: String): Array<ResolveResult> {
    val reference = findReferenceBySignature(step)
    assertNotNull(reference, "reference must not be null")
    return getResolveResult(reference!!)
  }

  fun getResolveResult(reference: PsiReference): Array<ResolveResult> =
    ReadAction.computeBlocking<Array<ResolveResult>, RuntimeException> {
      if (reference is PsiPolyVariantReference) {
        @Suppress("UNCHECKED_CAST")
        reference.multiResolve(true) as Array<ResolveResult>
      }
      else {
        arrayOf(PsiElementResolveResult(reference.resolve()!!))
      }
    }

  fun doTest(folder: String, step: String, stepDefinitionName: String?) {
    init(folder)

    checkReference(step, stepDefinitionName)
  }

  fun doTest(folder: String, fileName: String, step: String, stepDefinitionName: String?) {
    init(folder, fileName)

    checkReference(step, stepDefinitionName)
  }

  fun init(folder: String) {
    init(folder, "test.feature")
  }

  fun init(folder: String, fileName: String) {
    fixture.copyDirectoryToProject(folder, "")
    fixture.configureFromExistingVirtualFile(fixture.findFileInTempDir(fileName))
  }

  companion object {
    const val CARET_STR: String = "<caret>"

    /**
     * Creates a [CucumberResolveFixture] on the code insight fixture of [codeInsightFixture], with the test data in [testDataPath].
     * Declare it as an instance property.
     */
    @JvmStatic
    fun cucumberResolveFixture(
      codeInsightFixture: TestFixture<CodeInsightTestFixture>,
      testDataPath: String,
      stepDefinitionName: Function<PsiElement, String?>,
    ): TestFixture<CucumberResolveFixture> = testFixture("cucumberResolveFixture") { context ->
      val fixture = codeInsightFixture.init()
      fixture.testDataPath = testDataPath
      initialized(CucumberResolveFixture(fixture, stepDefinitionName, context)) {}
    }
  }
}

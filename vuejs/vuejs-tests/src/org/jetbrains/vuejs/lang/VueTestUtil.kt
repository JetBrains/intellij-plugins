package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.Condition
import com.intellij.psi.*
import com.intellij.testFramework.UsefulTestCase.assertEmpty
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.TestLookupElementPresentation
import com.intellij.util.containers.ContainerUtil
import junit.framework.TestCase
import junit.framework.TestCase.assertTrue

fun getVueTestDataPath() = PathManager.getHomePath() + vueRelativeTestDataPath()

fun vueRelativeTestDataPath() = "/contrib/vuejs/vuejs-tests/testData"

// TODO remove duplication with AngularTestUtil
fun CodeInsightTestFixture.renderLookupItems(renderPriority: Boolean, renderTypeText: Boolean): List<String> {
  return ContainerUtil.mapNotNull<LookupElement, String>(lookupElements!!) { el ->
    val result = StringBuilder()
    val presentation = TestLookupElementPresentation.renderReal(el)
    if (renderPriority && presentation.isItemTextBold) {
      result.append('!')
    }
    result.append(el.lookupString)
    if (renderTypeText) {
      result.append('#')
      result.append(presentation.typeText)
    }
    if (renderPriority) {
      result.append('#')
      var priority = 0.0
      if (el is PrioritizedLookupElement<*>) {
        priority = el.priority
      }
      result.append(priority.toInt())
    }
    result.toString()
  }
}

fun CodeInsightTestFixture.moveToOffsetBySignature(signature: String) {
  PsiDocumentManager.getInstance(project).commitAllDocuments()
  val offset = file.findOffsetBySignature(signature)
  editor.caretModel.moveToOffset(offset)
}

fun PsiFile.findOffsetBySignature(signature: String): Int {
  var str = signature
  val caretSignature = "<caret>"
  val caretOffset = str.indexOf(caretSignature)
  assert(caretOffset >= 0)
  str = str.substring(0, caretOffset) + str.substring(caretOffset + caretSignature.length)
  val pos = text.indexOf(str)
  assertTrue("Failed to locate '$str'", pos >= 0)
  return pos + caretOffset
}

fun CodeInsightTestFixture.resolveReference(signature: String): PsiElement {
  val offsetBySignature = file.findOffsetBySignature(signature)
  var ref = file.findReferenceAt(offsetBySignature)
  if (ref === null) {
    //possibly an injection
    ref = InjectedLanguageManager.getInstance(project)
      .findInjectedElementAt(file, offsetBySignature)
      ?.findReferenceAt(0)
  }

  TestCase.assertNotNull("No reference at '$signature'", ref)
  var resolve = ref!!.resolve()
  if (resolve == null && ref is PsiPolyVariantReference) {
    val results = ContainerUtil.filter(ref.multiResolve(false),
                                       Condition<ResolveResult> { it.isValidResult })
    if (results.size > 1) {
      throw AssertionError("Reference resolves to more than one element at '" + signature + "': "
                           + results)
    }
    else if (results.size == 1) {
      resolve = results[0].element
    }

  }
  TestCase.assertNotNull("Reference resolves to null at '$signature'", resolve)
  return resolve!!
}

fun CodeInsightTestFixture.assertUnresolvedReference(signature: String) {
  val offsetBySignature = file.findOffsetBySignature(signature)
  val ref = file.findReferenceAt(offsetBySignature)
  TestCase.assertNotNull(ref)
  TestCase.assertNull(ref!!.resolve())
  if (ref is PsiPolyVariantReference) {
    assertEmpty(ref.multiResolve(false))
  }
}

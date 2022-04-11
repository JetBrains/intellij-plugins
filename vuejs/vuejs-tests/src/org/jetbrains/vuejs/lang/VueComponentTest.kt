// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.util.descendantsOfType
import com.intellij.psi.xml.XmlComment
import com.intellij.rt.execution.junit.FileComparisonFailure
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.VueSourceComponent

/**
 * Checks highlighting, then checks the AST-based component model, then compares it with the Stub-based component model.
 * Runs the above both for TS default and strict modes.
 *
 * Reasons for this test to exist:
 * * in some cases we need to check how the Component Model contributes both file-internal & file-external references
 * * self-referenced component could be used, but it has some problems, e.g. it places warnings in a single line
 * * in some cases it makes sense to verify core JS/TS highlighting at the same time
 *
 * Loosely inspired by [Vue SFC Playground](https://sfc.vuejs.org/) that prepends analyzed bindings to output file.
 *
 * Example Vue script content:
 * ```ts
 * import { ref } from 'vue'
 * const msg = ref('Hello World!')
 * defineProps({a: String, b: Boolean});
 * ```
 *
 * Debug info of SFC compiler:
 * ```ts
 * /* Analyzed bindings: {
 *   "a": "props",
 *   "b": "props",
 *   "ref": "setup-const",
 *   "msg": "setup-ref"
 * } */
 * ```
 */
class VueComponentTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/component"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(VueInspectionsProvider())
  }

  fun testOptionsApiRuntimeDeclarationJS() = doTest()

  fun testOptionsApiRuntimeDeclarationPropTypeTS() = doTest()

  fun testOptionsApiRuntimeDeclarationArrayJS() = doTest()

  fun testDefinePropsRuntimeDeclarationTS() = doTest()

  fun testDefinePropsRuntimeDeclarationArrayTS() = doTest()

  fun testDefinePropsTypeDeclarationTS() = doTest()

  fun testBothScriptsJS() = doTest()

  /**
   * Runs `doTestInner` twice: once for default TS config, once for strict TS config
   */
  private fun doTest(addNodeModules: List<VueTestModule> = listOf(VueTestModule.VUE_3_2_2)) {
    var file = configureTestProject(addNodeModules) as PsiFileImpl

    val comment = file.descendantsOfType<XmlComment>().firstOrNull()
                  ?: throw AssertionError("HTML Comment with the expected model is missing.\n" +
                                          "Add `<!-- -->` to the beginning of the file if you want the test framework to fill it for you.")
    val expectedCommentContent = getExpectedText(comment)
    val textWithMarkers = file.text

    // first, check the component model with TSConfig(strictNullChecks=false)
    TypeScriptTestUtil.forceDefaultTsConfig(project, testRootDisposable)
    file = doTestInner(file, expectedCommentContent, false)

    // reset file contents
    runWriteAction {
      PsiDocumentManager.getInstance(project).getDocument(file)!!.setText(textWithMarkers)
    }

    // check another time, with TSConfig(strictNullChecks=true), to make sure that the result does not change
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    file = doTestInner(file, expectedCommentContent, true)
  }

  /**
   * Checks highlighting, then checks the AST-based component model, then compares it with the Stub-based component model.
   */
  private fun doTestInner(_file: PsiFileImpl, expectedCommentContent: String, strictNullChecks: Boolean): PsiFileImpl {
    var file = _file

    file.node // ensure that the AST is loaded
    assertNull(file.stub)

    val highlightingFailure = runCatching {
      myFixture.checkHighlighting()
    }.exceptionOrNull() as? FileComparisonFailure

    val astBasedCommentContent = serializeComponentModel(buildComponentModel(file))

    val modelFailed = astBasedCommentContent != expectedCommentContent
    if (highlightingFailure != null || modelFailed) {
      val combinedMessage = listOfNotNull(
        "Props mismatch (strictNullChecks=$strictNullChecks)".takeIf { modelFailed },
        highlightingFailure?.message
      ).joinToString("\n")

      val fileText = file.text
      val filePath = file.virtualFile.getUserData(VfsTestUtil.TEST_DATA_FILE_PATH)
      throw FileComparisonFailure(
        combinedMessage,
        highlightingFailure?.expected ?: fileText,
        replaceComment(highlightingFailure?.actual ?: fileText, astBasedCommentContent),
        filePath
      )
    }

    assertNull(file.stub)
    file = unloadAst(file)
    assertNotNull(file.stub)

    PsiManagerEx.getInstanceEx(project).setAssertOnFileLoadingFilter(VirtualFileFilter.ALL, testRootDisposable)
    val stubBasedComponentModel = buildComponentModel(file)
    assertNotNull(file.stub)
    PsiManagerEx.getInstanceEx(project).setAssertOnFileLoadingFilter(VirtualFileFilter.NONE, testRootDisposable)
    // serialization is not always stub safe, but that's fine: those operations don't run as often as component model building
    val stubBasedCommentContent = serializeComponentModel(stubBasedComponentModel)

    assertEquals(
      "The Stub-based model differs from the AST-based model (strictNullChecks=$strictNullChecks)",
      astBasedCommentContent,
      stubBasedCommentContent
    )

    return file
  }

  private fun buildComponentModel(file: PsiFile): ComponentModel {
    val component = VueModelManager.findEnclosingContainer(file) as? VueSourceComponent
                    ?: throw AssertionError("VueSourceComponent not found")

    val model = ComponentModel(
      props = component.props.sortedBy { it.name }.map {
        Prop(name = it.name, required = it.required, jsType = it.jsType) // Ensures that getters are executed eagerly
      }
    )

    return model
  }

  private fun serializeComponentModel(model: ComponentModel): String {
    return buildString {
      append("Expected Component Model:")
      append("\n")
      model.props.joinTo(this, "\n") {
        with(it) {
          val presentableType = jsType?.getTypeText(JSType.TypeTextFormat.PRESENTABLE)
          //"Prop(name=$name, required=$required, presentableType=${presentableType}, jsType=${jsType})"
          "Prop(name=$name, required=$required, presentableType=${presentableType})"
        }
      }
    }
  }

  private data class ComponentModel(val props: List<Prop>)
  private data class Prop(val name: String, val required: Boolean, val jsType: JSType?)

  private fun getExpectedText(comment: XmlComment): String {
    // we can't use comment.commentText since it stops on the [ character
    return comment.text.removeSurrounding("<!--", "-->").trimIndent()
  }

  private fun replaceComment(text: String, commentText: String): String {
    return text.replaceFirst(
      Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL),
      Regex.escapeReplacement("<!--\n$commentText\n-->")
    )
  }

  private fun configureTestProject(addNodeModules: List<VueTestModule> = emptyList(), extension: String = "vue"): PsiFile {
    if (addNodeModules.isNotEmpty()) {
      myFixture.configureVueDependencies(*addNodeModules.toTypedArray())
    }
    return myFixture.configureByFile(getTestName(false) + "." + extension)
  }

  /**
   * Inspired by SqlModelBuilderTest.unloadAst
   */
  private fun unloadAst(file: PsiFile): PsiFileImpl {
    val vFile = file.viewProvider.virtualFile
    (psiManager as PsiManagerImpl).cleanupForNextTest()
    val newFile = psiManager.findFile(vFile) as PsiFileImpl
    assertNull(newFile.treeElement)
    assertFalse(file.isValid)
    return newFile
  }
}


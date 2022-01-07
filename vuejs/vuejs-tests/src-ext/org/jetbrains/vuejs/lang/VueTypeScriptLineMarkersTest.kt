// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.lang.javascript.typescript.TypeScriptLineMarkersTest
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.Function
import org.jetbrains.vuejs.lang.html.VueFileType

class VueTypeScriptLineMarkersTest : TypeScriptLineMarkersTest() {

  private val localVarsMap = mapOf(
    Pair("propertyMarker", listOf("interfacePropertyMarker")),
    Pair("interfaceMethodCall", listOf("simpleObject")),
    Pair("namespaceImportColor", listOf("i")),
    Pair("semanticHighlighting", listOf("x", "s", "string", "i"))
  )

  private val localFunsMap = mapOf(
    Pair("typeGuardNarrowedHighlighting", listOf("assertNever", "q")),
    Pair("typeGuardHighlightingWithImplicit", listOf("foo")),
    Pair("typeGuardsHighlightingOptionals", listOf("foo", "zoo", "goo")),
    Pair("genericsMarker", listOf("funcGenerics"))
  )

  override fun doTestFor(checkWeakWarnings: Boolean,
                         function: Function<in MutableCollection<HighlightInfo>, Void>,
                         vararg fileNames: String?) {
    LOG.info("Running overridden code for vue")
    if (fileNames.getOrNull(0)?.endsWith(".d.ts") == true) {
      LOG.info("Skipping because only .d.ts file for test")
      return
    }
    super.doTestFor(checkWeakWarnings, function, *fileNames)
  }

  override fun configureEditorFile(name: String?) {
    val tsFile = LocalFileSystem.getInstance().findFileByPath("$testDataPath/$name")
    var text: String = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))

    val testName = getTestName(true)
    localVarsMap[testName]?.forEach { text = text.replace("<info descr=\"global variable\">$it", "<info descr=\"local variable\">$it") }
    localFunsMap[testName]?.forEach { text = text.replace("<info descr=\"global function\">$it", "<info descr=\"local function\">$it") }

    myFixture.configureByText(VueFileType.INSTANCE, surroundWithScriptTag(text))
  }

  override fun checkEditorText(ext: String?) {
    val tsFile = LocalFileSystem.getInstance().findFileByPath("$testDataPath/${getTestName(false)}_after.$ext")
    val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
    myFixture.checkResult(surroundWithScriptTag(text))
  }

  override fun checkHighlightingByRelativePath(relativePath: String?) {
    val text = surroundWithScriptTag(JSDaemonAnalyzerLightTestCase.loadText(relativePath))
    JSDaemonAnalyzerLightTestCase.checkHighlightByFile(myFixture, relativePath, text)
  }

  private fun surroundWithScriptTag(text: Any) = "<script lang=\"ts\">\n$text\n</script>"

}

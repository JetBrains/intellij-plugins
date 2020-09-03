// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.typescript.TypeScriptDuplicateTest
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import org.jetbrains.vuejs.lang.html.VueFileType

class VueTypeScriptDuplicateTest : TypeScriptDuplicateTest() {

  override fun configureEditorFile(name: String?) {
    val tsFile = LocalFileSystem.getInstance().findFileByPath("$testDataPath/$name")
    val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
    myFixture.configureByText(VueFileType.INSTANCE, surroundWithScriptTag(text))
  }

  override fun checkEditorText(ext: String?) {
    val tsFile = LocalFileSystem.getInstance().findFileByPath("$testDataPath/${getTestName(false)}_after.$ext")
    val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
    myFixture.checkResult(surroundWithScriptTag(text))
  }

  override fun checkHighlightingByRelativePath(relativePath: String?) {
    val text = surroundWithScriptTag(loadText(relativePath))
    checkHighlightByFile(myFixture, relativePath, text)
  }

  private fun surroundWithScriptTag(text: Any) = "<script lang=\"ts\">\n$text\n</script>"
}
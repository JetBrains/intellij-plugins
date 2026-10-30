// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSDaemonAnalyzerFixture
import com.intellij.lang.javascript.jsFixture
import com.intellij.lang.javascript.typescript.TypeScriptDuplicateTest
import com.intellij.lang.javascript.typescript.TypeScriptHighlightingFixture
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.jetbrains.vuejs.lang.html.VueFileType

class VueTypeScriptDuplicateTest : TypeScriptDuplicateTest() {
  override fun createFixture(projectFixture: TestFixture<Project>): TestFixture<TypeScriptHighlightingFixture> =
    jsFixture("vueTypeScriptDuplicateFixture", projectFixture, TypeScriptHighlightingFixture.defaultOptions()) { fixture, testClass, testMethod, options ->
      object : TypeScriptHighlightingFixture(fixture, testClass, testMethod, options, true) {
        override fun configureEditorFile(name: String) {
          val tsFile = StandardFileSystems.local().findFileByPath("$testDataPath/$name")
          val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
          this.fixture.configureByText(VueFileType, surroundWithScriptTag(text))
        }

        override fun checkEditorText(ext: String) {
          val tsFile = StandardFileSystems.local().findFileByPath("$testDataPath/${getTestName(false)}_after.$ext")
          val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
          this.fixture.checkResult(surroundWithScriptTag(text))
        }

        override fun checkHighlightingByRelativePath(relativePath: String) {
          val text = surroundWithScriptTag(JSDaemonAnalyzerFixture.loadText(relativePath))
          JSDaemonAnalyzerFixture.checkHighlightByFile(this.fixture, relativePath, text)
        }
      }
    }
}

private fun surroundWithScriptTag(text: Any) = "<script lang=\"ts\">\n$text\n</script>"

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper

import com.intellij.lang.javascript.resharper.TypeScriptReSharperCompletionTestBase
import com.intellij.lang.resharper.ReSharperTestUtil
import com.intellij.testFramework.TestDataPath
import org.angular2.Angular2TestUtil

@Suppress("ACCIDENTAL_OVERRIDE")
abstract class Angular2ReSharperCompletionTestBase : TypeScriptReSharperCompletionTestBase() {

  override fun setUp() {
    super.setUp()
    Angular2TestUtil.enableAstLoadingFilter(this)
  }

  protected open fun doGetExtraFiles(): List<String> {
    val extraFiles = mutableListOf<String>()
    val basePath = "/$name."
    for (ext in arrayOf("ts", "html")) {
      if (ReSharperTestUtil.fetchVirtualFile(testDataPath, "$basePath$ext.gold", testRootDisposable, false) == null
          && ReSharperTestUtil.fetchVirtualFile(testDataPath, basePath + ext, testRootDisposable, false) != null) {
        extraFiles.add("$name.$ext")
      }
    }
    return extraFiles
  }

  override val extraFiles: Map<String, Array<String>>
    get() = mapOf(name to doGetExtraFiles().toTypedArray())

  override fun skipTestForData(expectedNames: Set<String>, realNames: Set<String>, diff: MutableSet<String>): Boolean {
    if (!super.skipTestForData(expectedNames, realNames, diff)) {
      diff.remove("\$any")
      if (diff.isEmpty()) {
        return true
      }
    }
    return false
  }

  companion object {
    @JvmStatic
    @Suppress("unused")
    fun findTestData(klass: Class<*>): String {
      return (Angular2TestUtil.getBaseTestDataPath()
              + "resharper/CodeCompletion/"
              + klass.getAnnotation(TestDataPath::class.java).value.removePrefix("\$R#_COMPLETION_TEST_ROOT"))
    }
  }
}

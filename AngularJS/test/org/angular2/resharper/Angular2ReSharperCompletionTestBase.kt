// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper

import com.intellij.lang.javascript.resharper.TypeScriptReSharperCompletionTestBase
import com.intellij.lang.resharper.ReSharperTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.util.ArrayUtilRt
import org.angularjs.AngularTestUtil
import java.util.*

@Suppress("ACCIDENTAL_OVERRIDE")
abstract class Angular2ReSharperCompletionTestBase : TypeScriptReSharperCompletionTestBase() {

  override fun setUp() {
    super.setUp()
    AngularTestUtil.enableAstLoadingFilter(this)
  }

  protected open fun doGetExtraFiles(): MutableList<String?>? {
    val extraFiles: MutableList<String?> = ArrayList()
    val basePath = "/$name."
    for (ext in arrayOf("ts", "html")) {
      if (ReSharperTestUtil.fetchVirtualFile(testDataPath, "$basePath$ext.gold", testRootDisposable, false) == null
          && ReSharperTestUtil.fetchVirtualFile(testDataPath, basePath + ext, testRootDisposable, false) != null) {
        extraFiles.add("$name.$ext")
      }
    }
    return extraFiles
  }

  override fun getExtraFiles(): Map<String, Array<String>> {
    return Collections.singletonMap(name, ArrayUtilRt.toStringArray(doGetExtraFiles()))
  }

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
      return (AngularTestUtil.getBaseTestDataPath(klass)
              + "/CodeCompletion/"
              + klass.getAnnotation(TestDataPath::class.java).value.removePrefix("\$R#_COMPLETION_TEST_ROOT"))
    }
  }
}

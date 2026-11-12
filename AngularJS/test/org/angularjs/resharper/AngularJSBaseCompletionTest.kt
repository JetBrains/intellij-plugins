// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.resharper

import com.intellij.lang.resharper.tests.ReSharperTestUtil
import com.intellij.lang.resharper.tests.junit5.ReSharperCompletionTestCase
import com.intellij.lang.resharper.tests.junit5.ReSharperTestDataPath
import com.intellij.lang.resharper.tests.junit5.ReSharperTestNames
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PsiTestUtil
import com.intellij.util.ThrowableRunnable
import org.angularjs.AngularTestUtil
import java.io.File

abstract class AngularJSBaseCompletionTest : ReSharperCompletionTestCase() {
  @JvmField
  var myAngularVersion: String? = null

  override fun setParameters(parameters: List<Any?>) {
    super.setParameters(parameters)
    myAngularVersion = parameters[1] as String
  }

  override val goldSuffix: List<String>
    get() = listOf("$myAngularVersion.gold")

  @Throws(Exception::class)
  override fun doSingleTest(testFile: String, path: String) {
    WriteAction.runAndWait<RuntimeException?>(ThrowableRunnable {
      val angularFile = ReSharperTestUtil.fetchVirtualFile(
        AngularTestUtil.getBaseTestDataPath(javaClass), VERSIONS[myAngularVersion]!!, testRootDisposable)
      PsiTestUtil.addSourceContentToRoots(module, angularFile)
      Disposer.register(myFixture.testRootDisposable,
                        Disposable { PsiTestUtil.removeContentEntry(module, angularFile) })
    })
    super.doSingleTest(testFile, path)
  }

  companion object {
    private val VERSIONS = mapOf(
      "12" to "angular.1.2.28.js",
      "13" to "angular.1.3.15.js",
      "14" to "angular.1.4.0.js"
    )

    @Suppress("unused") // Used implicitly
    @JvmStatic
    fun findTestData(klass: Class<*>): String {
      return (AngularTestUtil.getBaseTestDataPath(klass)
              + "/CodeCompletion/"
              + klass.getAnnotation(ReSharperTestDataPath::class.java).value.removePrefix("\$R#_COMPLETION_TEST_ROOT"))
    }

    @ReSharperTestNames
    @JvmStatic
    fun testNames2(klass: Class<*>): List<Array<String>> {
      val result = mutableListOf<Array<String>>()
      val testDataRoot = File(callFindTestData(klass))
      for (file in testDataRoot.listFiles()) {
        if (File(file.getParentFile(), file.getName() + "14.gold").exists()) {
          for (version in arrayOf<String>("12", "13", "14")) {
            result.add(arrayOf<String>(file.getName(), version))
          }
        }
      }
      result.sortBy { it -> it[0] }
      return result
    }
  }
}

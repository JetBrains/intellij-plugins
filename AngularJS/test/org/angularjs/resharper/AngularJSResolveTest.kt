// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.resharper

import com.intellij.lang.javascript.resharper.junit5.JSReSharperSymbolResolveTestBase
import com.intellij.lang.resharper.tests.junit5.ReSharperTestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.angularjs.AngularTestUtil

@TestApplication
@ReSharperTestDataPath("\$R#_SYMBOL_RESOLVE_TEST_ROOT/DependencyInjection")
class AngularJSResolveTest : JSReSharperSymbolResolveTestBase() {
  companion object {
    @Suppress("unused") // Used implicitly
    @JvmStatic
    fun findTestData(klass: Class<*>): String {
      return (AngularTestUtil.getBaseTestDataPath(klass)
              + "/Resolve/"
              + klass.getAnnotation(ReSharperTestDataPath::class.java).value.removePrefix("\$R#_SYMBOL_RESOLVE_TEST_ROOT"))
    }
  }
}
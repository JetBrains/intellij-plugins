// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper

import com.intellij.lang.resharper.ReSharperIntentionTestCase
import org.angularjs.AngularTestUtil

@Suppress("ACCIDENTAL_OVERRIDE")
class Angular2BananaFixTest : ReSharperIntentionTestCase() {
  override fun getIntentionName(): String {
    return "Fix parentheses/brackets nesting"
  }

  override fun isExcluded(): Boolean {
    return true //no fix
  }

  companion object {
    @Suppress("unused")
    @JvmStatic
    fun findTestData(klass: Class<*>): String {
      return AngularTestUtil.getBaseTestDataPath(klass) + "Intentions/Angular2Html/QuickFixes"
    }
  }
}

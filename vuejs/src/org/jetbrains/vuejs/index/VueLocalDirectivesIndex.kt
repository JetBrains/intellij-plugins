// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey

/**
 * @author Irina.Chernushina on 10/20/2017.
 */
class VueLocalDirectivesIndex : VueIndexBase(KEY, JS_KEY) {
  companion object {
    val KEY: StubIndexKey<String, JSImplicitElementProvider> =
      StubIndexKey.createIndexKey<String, JSImplicitElementProvider>("vue.local.directives.index")
    val JS_KEY: String = createJSKey(KEY)
  }
}

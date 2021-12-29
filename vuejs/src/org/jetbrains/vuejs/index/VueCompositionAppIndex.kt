// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey

class VueCompositionAppIndex : VueIndexBase<JSImplicitElementProvider>(KEY) {
  companion object {
    val KEY: StubIndexKey<String, JSImplicitElementProvider> =
      StubIndexKey.createIndexKey("vue.composition.app.index")
    val JS_KEY: String = createJSKey(KEY)
  }
}

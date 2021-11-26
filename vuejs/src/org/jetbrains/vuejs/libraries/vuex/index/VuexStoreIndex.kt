// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey
import org.jetbrains.vuejs.index.VueIndexBase

class VuexStoreIndex : VueIndexBase<JSImplicitElementProvider>(KEY) {
  companion object {
    val KEY: StubIndexKey<String, JSImplicitElementProvider> = StubIndexKey.createIndexKey("vuex.store.index")
    val JS_KEY: String = createJSKey(KEY)
  }
}

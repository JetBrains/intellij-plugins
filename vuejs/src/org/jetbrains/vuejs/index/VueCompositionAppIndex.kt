// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey

class VueCompositionAppIndex : VueIndexBase<JSImplicitElementProvider>(VUE_COMPOSITION_APP_INDEX_KEY)

val VUE_COMPOSITION_APP_INDEX_KEY: StubIndexKey<String, JSImplicitElementProvider> =
  StubIndexKey.createIndexKey("vue.composition.app.index")
val VUE_COMPOSITION_APP_INDEX_JS_KEY: String = VueIndexBase.createJSKey(VUE_COMPOSITION_APP_INDEX_KEY)

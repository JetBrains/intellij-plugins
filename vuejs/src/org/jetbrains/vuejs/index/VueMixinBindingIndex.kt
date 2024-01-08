// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey

class VueMixinBindingIndex : VueIndexBase<JSImplicitElementProvider>(VUE_MIXIN_BINDING_INDEX_KEY)

val VUE_MIXIN_BINDING_INDEX_KEY: StubIndexKey<String, JSImplicitElementProvider> =
  StubIndexKey.createIndexKey("vue.mixin.binding.index")
val VUE_MIXIN_BINDING_INDEX_JS_KEY: String = VueIndexBase.createJSKey(VUE_MIXIN_BINDING_INDEX_KEY)
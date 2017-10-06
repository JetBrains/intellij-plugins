package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey

/**
 * @author Irina.Chernushina on 10/5/2017.
 */
class VueMixinBindingIndex : VueIndexBase() {
  init {
    INDICES[KEY] = JS_KEY
  }

  companion object {
    val KEY = StubIndexKey.createIndexKey<String, JSImplicitElementProvider>("vue.mixin.binding.index")
    val JS_KEY = KEY.name.split(".").map { it.subSequence(0, 1) }.joinToString("")
  }

  override fun getKey(): StubIndexKey<String, JSImplicitElementProvider> = KEY
}
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.psi.stubs.StubIndexKey

/**
 * @author Irina.Chernushina on 10/20/2017.
 */
class VueGlobalDirectivesIndex : VueIndexBase() {
  init {
    INDICES[KEY] = JS_KEY
  }

  companion object {
    val KEY = StubIndexKey.createIndexKey<String, JSImplicitElementProvider>("vue.global.directives.index")
    val JS_KEY = KEY.name.split(".").map { it.subSequence(0, 1) }.joinToString("")
  }

  override fun getKey(): StubIndexKey<String, JSImplicitElementProvider> = KEY
}
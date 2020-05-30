// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

abstract class VueIndexBase<T : PsiElement>(private val key: StubIndexKey<String, T>,
                                            jsKey: String) : StringStubIndexExtension<T>() {
  private val VERSION = 25

  init {
    // this is called on index==application component initialization
    JSImplicitElementImpl.ourUserStringsRegistry.registerUserString(jsKey)
  }

  companion object {
    fun createJSKey(key: StubIndexKey<String, *>): String =
      key.name.split(".").joinToString("") { it.subSequence(0, 1) }
  }

  override fun getKey(): StubIndexKey<String, T> = key

  override fun getVersion(): Int {
    return VERSION
  }
}

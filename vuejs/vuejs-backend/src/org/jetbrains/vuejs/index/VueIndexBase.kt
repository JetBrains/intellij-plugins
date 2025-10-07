// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

abstract class VueIndexBase<T : PsiElement>(private val key: StubIndexKey<String, T>) : StringStubIndexExtension<T>() {
  private val VERSION = 25

  companion object {
    fun createJSKey(key: StubIndexKey<*, *>): String =
      key.name.split(".").joinToString("") { it.subSequence(0, 1) }
  }

  override fun getKey(): StubIndexKey<String, T> = key

  override fun getVersion(): Int {
    return VERSION
  }
}

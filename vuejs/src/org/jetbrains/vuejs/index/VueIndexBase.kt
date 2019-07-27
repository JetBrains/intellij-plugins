// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

abstract class VueIndexBase(private val key: StubIndexKey<String, JSImplicitElementProvider>,
                            jsKey: String) : StringStubIndexExtension<JSImplicitElementProvider>() {
  private val VERSION = 25

  init {
    // this is called on index==application component initialization
    JSImplicitElementImpl.ourUserStringsRegistry.registerUserString(jsKey)
  }

  companion object {
    fun createJSKey(key: StubIndexKey<String, JSImplicitElementProvider>): String =
      key.name.split(".").joinToString("") { it.subSequence(0, 1) }
  }

  override fun getKey(): StubIndexKey<String, JSImplicitElementProvider> = key

  override fun getVersion(): Int {
    return VERSION
  }
}

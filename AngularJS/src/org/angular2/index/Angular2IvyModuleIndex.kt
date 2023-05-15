// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.stubs.StubIndexKey

class Angular2IvyModuleIndex : Angular2IndexBase<TypeScriptClass>() {

  override fun getKey(): StubIndexKey<String, TypeScriptClass> = KEY

  companion object {
    @JvmField
    val KEY = StubIndexKey.createIndexKey<String, TypeScriptClass>("angular2.ivy.module.index")
  }
}

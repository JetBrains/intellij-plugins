// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.psi.stubs.StubIndexKey
import org.angular2.entities.metadata.psi.Angular2MetadataFunction

class Angular2MetadataFunctionIndex : Angular2IndexBase<Angular2MetadataFunction>() {

  override fun getKey(): StubIndexKey<String, Angular2MetadataFunction> = KEY

  companion object {
    @JvmField
    val KEY = StubIndexKey.createIndexKey<String, Angular2MetadataFunction>("angular2.metadata.function.index")
  }
}

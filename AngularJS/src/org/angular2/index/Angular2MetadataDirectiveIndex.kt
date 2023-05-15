// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.psi.stubs.StubIndexKey
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase

class Angular2MetadataDirectiveIndex : Angular2IndexBase<Angular2MetadataDirectiveBase<*>>() {

  override fun getKey(): StubIndexKey<String, Angular2MetadataDirectiveBase<*>> = KEY

  companion object {
    @JvmField
    val KEY: StubIndexKey<String, Angular2MetadataDirectiveBase<*>> = StubIndexKey.createIndexKey("angular2.metadata.directive.index")
  }
}

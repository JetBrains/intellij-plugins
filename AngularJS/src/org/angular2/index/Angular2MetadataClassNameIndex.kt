// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.psi.stubs.StubIndexKey
import org.angular2.entities.metadata.psi.Angular2MetadataClassBase

class Angular2MetadataClassNameIndex : Angular2IndexBase<Angular2MetadataClassBase<*>>() {

  override fun getKey(): StubIndexKey<String, Angular2MetadataClassBase<*>> = KEY

  companion object {
    val KEY: StubIndexKey<String, Angular2MetadataClassBase<*>> = StubIndexKey.createIndexKey("angular2.metadata.className.index")
  }
}

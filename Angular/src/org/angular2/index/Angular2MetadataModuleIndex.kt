// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.psi.stubs.StubIndexKey
import org.angular2.entities.metadata.psi.Angular2MetadataModule

class Angular2MetadataModuleIndex : Angular2IndexBase<Angular2MetadataModule>() {

  override fun getKey(): StubIndexKey<String, Angular2MetadataModule> = Angular2MetadataModuleIndexKey

}

@JvmField
val Angular2MetadataModuleIndexKey: StubIndexKey<String, Angular2MetadataModule> =
  StubIndexKey.createIndexKey<String, Angular2MetadataModule>("angular2.metadata.module.index")
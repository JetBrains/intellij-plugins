// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.psi.stubs.StubIndexKey
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule

class Angular2MetadataNodeModuleIndex : Angular2IndexBase<Angular2MetadataNodeModule>() {

  override fun getKey(): StubIndexKey<String, Angular2MetadataNodeModule> = Angular2MetadataNodeModuleIndexKey

}

@JvmField
val Angular2MetadataNodeModuleIndexKey: StubIndexKey<String, Angular2MetadataNodeModule> =
  StubIndexKey.createIndexKey<String, Angular2MetadataNodeModule>("angular2.metadata.node.index")
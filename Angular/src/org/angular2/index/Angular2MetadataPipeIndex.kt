// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.psi.stubs.StubIndexKey
import org.angular2.entities.metadata.psi.Angular2MetadataPipe

class Angular2MetadataPipeIndex : Angular2IndexBase<Angular2MetadataPipe>() {

  override fun getKey(): StubIndexKey<String, Angular2MetadataPipe> = Angular2MetadataPipeIndexKey

}

@JvmField
val Angular2MetadataPipeIndexKey: StubIndexKey<String, Angular2MetadataPipe> =
  StubIndexKey.createIndexKey<String, Angular2MetadataPipe>("angular2.metadata.pipe.index")
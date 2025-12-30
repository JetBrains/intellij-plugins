// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index

import com.intellij.lang.javascript.modules.NodeModulesIndexableFileNamesProvider

import org.angular2.entities.metadata.Angular2MetadataFileType.METADATA_SUFFIX

class Angular2IndexableFileNamesProvider : NodeModulesIndexableFileNamesProvider() {
  override fun getIndexableExtensions(kind: DependencyKind): List<String> {
    return listOf(METADATA_SUFFIX)
  }
}

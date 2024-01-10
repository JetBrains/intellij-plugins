// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.javascript.modules.JSImportCandidateDescriptor
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor

class Angular2ImportCandidateDescriptor(descriptor: JSImportDescriptor)
  : JSImportCandidateDescriptor(descriptor.moduleDescriptor,
                                null,
                                descriptor.exportedName ?: descriptor.importedName,
                                descriptor.importExportPrefixKind,
                                descriptor.importType
), Angular2FieldImportCandidateDescriptor {
  override val fieldName: String = descriptor.importedName ?: descriptor.exportedName ?: ""
}
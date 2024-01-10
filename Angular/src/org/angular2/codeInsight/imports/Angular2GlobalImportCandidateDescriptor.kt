// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration.ImportExportPrefixKind
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.ImportExportType
import com.intellij.lang.javascript.modules.JSImportCandidateDescriptor

class Angular2GlobalImportCandidateDescriptor(override val fieldName: String, exportedName: String)
  : JSImportCandidateDescriptor("\$GLOBAL$", null, exportedName,
                                ImportExportPrefixKind.IMPORT, ImportExportType.DEFAULT),
    Angular2FieldImportCandidateDescriptor
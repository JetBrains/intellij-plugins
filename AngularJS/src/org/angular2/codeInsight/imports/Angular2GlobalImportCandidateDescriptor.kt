// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.javascript.modules.JSImportCandidateDescriptor
import com.intellij.lang.javascript.modules.imports.JSImportExportType

class Angular2GlobalImportCandidateDescriptor(unquotedModuleName: String,
                                              importedName: String?,
                                              exportedName: String?,
                                              prefixKind: ES6ImportExportDeclaration.ImportExportPrefixKind,
                                              type: JSImportExportType)
  : JSImportCandidateDescriptor(unquotedModuleName, importedName, exportedName, prefixKind, type) {

}
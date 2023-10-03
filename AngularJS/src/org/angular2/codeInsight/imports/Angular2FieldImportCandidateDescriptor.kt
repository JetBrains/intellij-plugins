// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.javascript.modules.imports.JSImportDescriptor

interface Angular2FieldImportCandidateDescriptor : JSImportDescriptor {

  val fieldName: String

}
// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.util.castSafelyTo

object VueTypedEntitiesProvider {

  fun isComponentDefinition(variable: TypeScriptVariable): Boolean {
    val qualifiedTypeName = variable.typeElement?.castSafelyTo<TypeScriptSingleType>()
      ?.qualifiedTypeName
    return variable.name != null
           && (qualifiedTypeName == "DefineComponent"
               || qualifiedTypeName == "import(\"vue\").DefineComponent"
               || qualifiedTypeName == "import('vue').DefineComponent")
  }

  fun getComponent(variable: TypeScriptVariable): VueTypedComponent? =
    variable.takeIf { isComponentDefinition(it) }?.let { VueTypedComponent(it) }

}
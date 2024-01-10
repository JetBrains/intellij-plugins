// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.ApiStatus

/**
 * Angular framework handler allows to tailor Angular plugin behaviour to properly support a particular framework.
 */
@ApiStatus.Experimental
interface Angular2FrameworkHandler {

  /**
   * Contribute additional component classes to the template context of external template file. This is required when
   * template file name is not the same as the one linked in the component's templateUrl property. Only the first component
   * in the list will be used for building the template context.
   */
  fun findAdditionalComponentClasses(context: PsiFile): List<TypeScriptClass> {
    return emptyList()
  }

  /**
   * When there are multiple modules in which component is included, the one with the first name is chosen by default. Framework handler can
   * decide which module should be chosen in a particular context. E.g. in NativeScript modules should have similar suffix in the
   * file name as the template file name.
   */
  fun selectModuleForDeclarationsScope(modules: Collection<Angular2Module>,
                                       component: Angular2Component,
                                       context: PsiFile): Angular2Module? {
    return null
  }

  /**
   * In some specific cases Angular declaration is included in many modules (like components in NativeScript), framework handler can
   * suppress incorrect inspection error in such a case for any declaration.
   */
  fun suppressModuleInspectionErrors(modules: Collection<Angular2Module>,
                                     declaration: Angular2Declaration): Boolean {
    return false
  }

  companion object {
    @JvmField
    val EP_NAME = ExtensionPointName.create<Angular2FrameworkHandler>("org.angular2.frameworkHandler")
  }
}

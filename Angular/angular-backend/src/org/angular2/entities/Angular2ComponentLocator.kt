// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.PsiFile
import org.angular2.entities.source.Angular2SourceUtil
import org.jetbrains.annotations.ApiStatus
import java.util.function.BiPredicate

@ApiStatus.ScheduledForRemoval
@Deprecated("Use 'Angular2SourceUtil' instead")
object Angular2ComponentLocator {

  @JvmStatic
  @Deprecated(message = "Use Angular2SourceUtil.findComponentClass instead",
              replaceWith = ReplaceWith("Angular2SourceUtil.findComponentClassesInFile(file, filter)",
                                        "org.angular2.entities.source.Angular2SourceUtil"))
  fun findComponentClassesInFile(file: PsiFile, filter: BiPredicate<TypeScriptClass, ES6Decorator>?): List<TypeScriptClass> =
    Angular2SourceUtil.findComponentClassesInFile(file, filter)

}

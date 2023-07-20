// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.liveTemplate

import com.intellij.lang.javascript.liveTemplates.typescript.TSExpressionContextType
import com.intellij.psi.PsiFile
import org.angular2.lang.Angular2LangUtil

class Angular2ExpressionContextType : TSExpressionContextType() {
  override fun isInContext(file: PsiFile, offset: Int) =
    Angular2LangUtil.isAngular2Context(file, offset) && super.isInContext(file, offset)
}
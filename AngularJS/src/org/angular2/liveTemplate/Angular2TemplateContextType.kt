// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.liveTemplate

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.Angular2HtmlFile

class Angular2TemplateContextType : TemplateContextType(Angular2Bundle.message("angular.live.template.context.template")) {
  override fun isInContext(file: PsiFile, offset: Int) = file is Angular2HtmlFile
}
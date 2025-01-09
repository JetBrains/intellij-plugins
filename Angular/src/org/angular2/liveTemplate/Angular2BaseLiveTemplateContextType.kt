// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.liveTemplate

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.lang.javascript.liveTemplates.JSLikeTemplateContextType
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil

class Angular2BaseLiveTemplateContextType : TemplateContextType(Angular2Bundle.message("angular.live.template.context.angular")), JSLikeTemplateContextType {
  override fun isInContext(context: TemplateActionContext): Boolean =
    context.file.findElementAt(context.startOffset)?.let { Angular2LangUtil.isAngular2Context(it) } ?: false
}

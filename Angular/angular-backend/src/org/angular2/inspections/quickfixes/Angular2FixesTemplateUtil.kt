// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.template.Template
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.ecmal4.JSClass

object Angular2FixesTemplateUtil {
  fun addClassMemberModifiers(template: Template, staticContext: Boolean, targetClass: JSClass) {
    if (DialectDetector.isTypeScript(targetClass)) {
      template.addTextSegment("protected ")
      if (staticContext) {
        template.addTextSegment("static ")
      }
    }
  }
}
// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.analysis.ErrorQuickFixProvider
import com.intellij.codeInsight.daemon.impl.analysis.EscapeCharacterIntentionFix
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiErrorElement
import org.angular2.lang.Angular2Bundle

class Angular2ErrorQuickFixProvider : ErrorQuickFixProvider {

  override fun registerErrorQuickFix(errorElement: PsiErrorElement, builder: HighlightInfo.Builder) {
    if (errorElement.errorDescription == Angular2Bundle.message("angular.parse.template.unterminated-expansion-form")
        && errorElement.textMatches("{")) {
      builder.registerFix(EscapeCharacterIntentionFix(errorElement, TextRange(0, 1), "{", "&#123;"),
                          null, null, null, null)
      builder.registerFix(EscapeCharacterIntentionFix(errorElement, TextRange(0, 1), "{", "{{ \"{\" }}"),
                          null, null, null, null)
    }
    else if (errorElement.errorDescription == Angular2Bundle.message("angular.parse.template.unexpected-block-closing-rbrace")) {
      builder.registerFix(EscapeCharacterIntentionFix(errorElement, TextRange(0, 1), "}", "&#125;"),
                          null, null, null, null)
    }
  }

}
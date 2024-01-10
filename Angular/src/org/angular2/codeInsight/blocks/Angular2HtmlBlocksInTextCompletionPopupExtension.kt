// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.completion.HtmlInTextCompletionPopupExtension
import com.intellij.psi.PsiElement
import org.angular2.lang.html.Angular2TemplateSyntax

class Angular2HtmlBlocksInTextCompletionPopupExtension : HtmlInTextCompletionPopupExtension {

  override fun isDeselectingFirstItemDisabled(element: PsiElement): Boolean =
    Angular2TemplateSyntax.of(element)?.enableBlockSyntax == true
    && element.text.startsWith("@")

}
// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.language.java

import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.ide.language.LanguageSupport

class JStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiLiteralExpressionImpl && (element.literalElementType == JavaTokenType.STRING_LITERAL)
    }

    override fun check(element: PsiElement) = when ((element as PsiLiteralExpressionImpl).literalElementType) {
        JavaTokenType.STRING_LITERAL -> GrammarChecker.default.check(element)
        else -> emptySet()
    }
}

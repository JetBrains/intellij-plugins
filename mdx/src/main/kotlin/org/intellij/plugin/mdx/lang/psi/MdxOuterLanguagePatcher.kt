package org.intellij.plugin.mdx.lang.psi

import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataElementType.OuterLanguageRangePatcher

class MdxOuterLanguagePatcher: OuterLanguageRangePatcher {
    override fun getTextForOuterLanguageInsertionRange(templateDataElementType: TemplateDataElementType, outerElementText: CharSequence): String? {
        if (templateDataElementType == MdxTemplateDataElementType) {
            return "\n;"
        }

        return null
    }
}
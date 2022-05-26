package org.intellij.plugin.mdx.format

import com.intellij.formatting.FormattingMode
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.formatter.JSBlockContext
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.formatter.JSSpacingProcessor
import com.intellij.lang.javascript.formatter.blocks.JSSpacingStrategy
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.util.ObjectUtils
import java.util.function.BiFunction

class MdxJsBlockContext(topSettings: CodeStyleSettings,
                        dialect: Language,
                        explicitSettings: JSCodeStyleSettings?,
                        formattingMode: FormattingMode) : JSBlockContext(topSettings, dialect, explicitSettings, formattingMode) {
    val myDialectSettings = ObjectUtils.coalesce(explicitSettings, topSettings.getCustomSettings(JSCodeStyleSettings.getSettingsClass(dialect)))


    override fun createSpacingStrategy(node: ASTNode): JSSpacingStrategy {
        return JSSpacingStrategy(myDialectSettings, commonSettings
        ) { child1: ASTNode, child2: ASTNode -> createMdxSpacingProcessor(node, child1, child2).calcSpacing() }
    }

    private fun createMdxSpacingProcessor(node: ASTNode, child1: ASTNode, child2: ASTNode): JSSpacingProcessor {
        return MdxJsSpacingProcessor(node, child1, child2, topSettings, dialect, myDialectSettings)
    }
}
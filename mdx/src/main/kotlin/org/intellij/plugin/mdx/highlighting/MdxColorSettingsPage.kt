package org.intellij.plugin.mdx.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import org.intellij.plugins.markdown.highlighting.MarkdownColorSettingsPage

class MdxColorSettingsPage : MarkdownColorSettingsPage() {
    override fun getHighlighter(): SyntaxHighlighter {
        return MdxSyntaxHighlighter()
    }

    override fun getDisplayName(): String {
        return "MDX"
    }

    override fun getDemoText(): String {
        return "import React from \'react\';\n\n" +
                "#hello\n\n"+
                "<Button>Press the button</Button>"
    }
}
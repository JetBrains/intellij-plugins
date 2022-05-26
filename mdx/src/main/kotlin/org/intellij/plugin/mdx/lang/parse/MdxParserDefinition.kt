package org.intellij.plugin.mdx.lang.parse

import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.IStubFileElementType
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes.Companion.MDX_FILE_NODE_TYPE
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.lang.lexer.MarkdownToplevelLexer
import org.intellij.plugins.markdown.lang.parser.MarkdownParserAdapter
import org.intellij.plugins.markdown.lang.parser.MarkdownParserDefinition
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager


class MdxParserDefinition : MarkdownParserDefinition() {
    override fun getFileNodeType(): IFileElementType {
        return MDX_FILE_NODE_TYPE
    }

    override fun createLexer(project: Project): Lexer {
        return MarkdownToplevelLexer(MdxFlavourDescriptor)
    }

    override fun createParser(project: Project): PsiParser {
        return MarkdownParserAdapter(MdxFlavourDescriptor)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        val mdxFile = MdxFile(viewProvider)
        mdxFile.putUserData(MarkdownParserManager.FLAVOUR_DESCRIPTION, MdxFlavourDescriptor)
        return mdxFile
    }

}


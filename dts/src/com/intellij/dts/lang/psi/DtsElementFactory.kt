package com.intellij.dts.lang.psi

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace

object DtsElementFactory {
    private fun createFile(project: Project, text: String): DtsFile.Include {
        return PsiFileFactory.getInstance(project).createFileFromText("a.dtsi", DtsFileType.INSTANCE, text) as DtsFile.Include
    }

    fun createNodeContent(project: Project): DtsNodeContent {
        val content = createFile(project, "p;").dtsContent as DtsNodeContent
        content.firstChild.delete()

        return content
    }

    fun createWhitespace(project: Project, text: String): PsiWhiteSpace {
        assert(text.isBlank())
        return createFile(project, text).firstChild as PsiWhiteSpace
    }
}
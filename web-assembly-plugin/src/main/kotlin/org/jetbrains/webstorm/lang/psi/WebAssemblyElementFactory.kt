package org.jetbrains.webstorm.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory

object WebAssemblyElementFactory {
    fun createImport(project: Project, name: String): ASTNode =
            createFile(project, "(import \"\" \"\" (func ${name}))")

    fun createElement(project: Project, name: String): ASTNode = createFile(project, "(func ${name})")

    private fun createFile(project: Project, text: String): ASTNode =
            PsiFileFactory.getInstance(project)
                .createFileFromText("dummy.wat", WebAssemblyFileType, text)
                .node
}

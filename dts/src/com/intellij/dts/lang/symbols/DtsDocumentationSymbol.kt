package com.intellij.dts.lang.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.SymbolDocumentationTargetProvider
import com.intellij.util.asSafely

interface DtsDocumentationSymbol : Symbol {
    fun getDocumentationTarget(project: Project): DocumentationTarget?

    class Provider : SymbolDocumentationTargetProvider {
        override fun documentationTarget(project: Project, symbol: Symbol): DocumentationTarget? {
            return symbol.asSafely<DtsDocumentationSymbol>()?.getDocumentationTarget(project)
        }
    }

    private data class FromTarget(val target: DocumentationTarget) : DtsDocumentationSymbol {
        override fun createPointer(): Pointer<out Symbol> = Pointer { this }

        override fun getDocumentationTarget(project: Project): DocumentationTarget = target
    }

    companion object {
        fun from(target: DocumentationTarget): Pointer<out Symbol> = FromTarget(target).createPointer()
    }
}
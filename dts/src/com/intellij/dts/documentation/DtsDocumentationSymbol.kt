package com.intellij.dts.documentation

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.SymbolDocumentationTargetProvider
import com.intellij.util.asSafely

data class DtsDocumentationSymbol(val target: DocumentationTarget) : Symbol {
    class Provider : SymbolDocumentationTargetProvider {
        override fun documentationTarget(project: Project, symbol: Symbol): DocumentationTarget? {
            return symbol.asSafely<DtsDocumentationSymbol>()?.target
        }
    }

    companion object {
        fun of(target: DocumentationTarget): Pointer<out Symbol> {
            return DtsDocumentationSymbol(target).createPointer()
        }
    }

    override fun createPointer(): Pointer<out Symbol> {
        val targetPtr = target.createPointer()

        return Pointer {
           targetPtr.dereference()?.let(::DtsDocumentationSymbol)
        }
    }
}
package com.intellij.dts.ide

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class DtsStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        if (psiFile !is DtsFile) return null

        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel = Model(psiFile, editor)
        }
    }
}

private class Model(file: DtsFile, editor: Editor?) : StructureViewModelBase(
    file,
    editor,
    FileElement(file)
), StructureViewModel.ElementInfoProvider {
    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement?): Boolean = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement?): Boolean = false

    override fun getSuitableClasses(): Array<Class<*>> = arrayOf(DtsStatement::class.java)
}

private abstract class ContainerElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    protected abstract val dtsContainer: DtsContainer?

    override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> {
        val container = dtsContainer ?: return mutableListOf()

        return container.dtsStatements.mapNotNull {
            when (it) {
                is DtsStatement.CompilerDirective -> null
                is DtsStatement.Node -> NodeElement(it)
                is DtsStatement.Property -> PropertyElement(it)
            }
        }.toMutableList()
    }
}

private class FileElement(element: DtsFile) : ContainerElement<DtsFile>(element) {
    override val dtsContainer: DtsContainer?
        get() = element

    override fun getPresentableText(): String? = element?.name
}

private class NodeElement(element: DtsStatement.Node) : ContainerElement<DtsStatement.Node>(element) {
    override val dtsContainer: DtsContainer?
        get() = element?.dtsContent

    override fun getPresentableText(): String? = element?.dtsName
}

private class PropertyElement(element: DtsStatement.Property) : PsiTreeElementBase<DtsStatement.Property>(element) {
    override fun getPresentableText(): String? = element?.dtsName

    override fun getLocationString(): String? {
        val values = element?.dtsValues
        if (values.isNullOrEmpty()) return null

        val builder = StringBuilder()
        for (i in values.indices) {
            // truncate after more than 50 characters
            if (builder.length > 50) {
                builder.append("...")
                break
            }

            // replace consecutive whitespace with one space
            val text = values[i].text.replace("\\s+".toRegex(), " ")
            builder.append(text)

            if (i < values.size - 1) {
                builder.append(", ")
            }
        }

        return builder.toString()
    }

    override fun getChildrenBase(): MutableCollection<StructureViewTreeElement> = mutableListOf()
}
package com.intellij.dts.lang.psi

import com.intellij.dts.util.DtsPath
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference

fun DtsNode.getDtsPresentableText(): @NlsSafe String {
    return when (this) {
        is DtsNode.Ref -> dtsHandle.text
        is DtsNode.Root -> "/"
        is DtsNode.Sub -> dtsName
    }
}

fun DtsStatement.getDtsPresentableText(): @NlsSafe String {
    return when (this) {
        is DtsStatement.CompilerDirective -> dtsDirective.text
        is DtsStatement.Node -> (this as DtsNode).getDtsPresentableText()
        is DtsStatement.Property -> dtsName
    }
}

fun DtsNode.getDtsAnnotationTarget(): PsiElement {
    return when (this) {
        is DtsNode.Ref -> dtsHandle
        is DtsNode.Root -> dtsSlash
        is DtsNode.Sub -> dtsNameElement
    }
}

fun DtsStatement.getDtsAnnotationTarget(): PsiElement {
    return when (this) {
        is DtsStatement.CompilerDirective -> dtsDirective
        is DtsStatement.Node -> (this as DtsNode).getDtsAnnotationTarget()
        is DtsStatement.Property -> dtsNameElement
    }
}

fun DtsNode.Ref.getDtsReferenceTarget(): DtsNode? {
    val reference = dtsHandle.reference ?: return null

    val target = if (reference is PsiPolyVariantReference) {
        reference.multiResolve(false).firstOrNull()?.element
    } else {
        reference.resolve()
    }

    return target as? DtsNode
}

fun DtsNode.isDtsRootNode(): Boolean {
    return when (this) {
        is DtsNode.Ref -> getDtsReferenceTarget()?.isDtsRootNode() ?: false
        is DtsNode.Root -> true
        is DtsNode.Sub -> false
    }
}

fun DtsNode.getDtsPath(): DtsPath? {
    val pathSegments = mutableListOf<String>()

    var node: DtsNode? = this
    while (node != null) {
        node = when (node) {
            is DtsNode.Root -> null
            is DtsNode.Ref -> node.getDtsReferenceTarget() ?: return null
            is DtsNode.Sub -> {
                pathSegments.add(node.dtsName)
                DtsTreeUtil.parentNode(node)
            }
        }
    }

    return DtsPath(true, pathSegments.reversed())
}

/**
 * Reads the string values of the compatible property of this node. If no
 * compatible strings where found and this node is a reference, this method
 * tries to read the compatible property from the reference target.
 */
fun DtsNode.getDtsCompatibleStrings(): List<String> {
    val property = dtsProperties.firstOrNull { it.dtsName == "compatible" }

    val string = if (property != null) {
        property.dtsValues.filterIsInstance<DtsString>().map(DtsString::dtsParse)
    } else {
        emptyList()
    }

    // if compatible strings found use these if not search for reference
    if (!string.isEmpty() || this !is DtsRefNode) return string

    val target = getDtsReferenceTarget() ?: return emptyList()
    return target.getDtsCompatibleStrings()
}

package com.intellij.dts.api

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.getDtsPath
import com.intellij.util.asSafely

fun <T> DtsFile.dtsSearch(path: DtsPath, forward: Boolean = true, maxOffset: Int? = null, callback: (DtsNode) -> T?): T? {
   var result: T? = null

    val visitor = object : DtsNodeVisitor {
        override fun visit(node: DtsNode): Boolean {
            result = callback(node)
            if (result != null) throw DtsVisitorCanceledException()

            return false
        }
    }
    dtsAccept(visitor, path, forward, maxOffset)

    return result
}

fun <T> DtsNode.dtsSearch(forward: Boolean = true, maxOffset: Int? = null, callback: (DtsNode) -> T?): T? {
    val file = containingFile.asSafely<DtsFile>() ?: return null
    val path = getDtsPath() ?: return null

    return file.dtsSearch(path, forward, maxOffset, callback)
}
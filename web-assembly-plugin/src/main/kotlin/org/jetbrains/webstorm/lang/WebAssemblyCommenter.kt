package org.jetbrains.webstorm.lang

import com.intellij.lang.Commenter

class WebAssemblyCommenter : Commenter {
    override fun getCommentedBlockCommentPrefix(): String? = "(;"
    override fun getCommentedBlockCommentSuffix(): String? = ";)"
    override fun getBlockCommentPrefix(): String? = "(;"
    override fun getBlockCommentSuffix(): String? = ";)"
    override fun getLineCommentPrefix(): String? = ";;"
}
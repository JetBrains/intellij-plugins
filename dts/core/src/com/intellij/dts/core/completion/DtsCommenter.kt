package com.intellij.dts.core.completion

import com.intellij.lang.Commenter

class DtsCommenter : Commenter {
  override fun getLineCommentPrefix(): String = "//"

  override fun getBlockCommentPrefix(): String = "/*"

  override fun getBlockCommentSuffix(): String = "*/"

  override fun getCommentedBlockCommentPrefix(): String? = null

  override fun getCommentedBlockCommentSuffix(): String? = null
}
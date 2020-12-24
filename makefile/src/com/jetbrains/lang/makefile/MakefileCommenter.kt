package name.kropp.intellij.makefile

import com.intellij.lang.Commenter

class MakefileCommenter : Commenter {
  override fun getLineCommentPrefix() = "#"

  override fun getCommentedBlockCommentPrefix() = ""
  override fun getCommentedBlockCommentSuffix() = null

  override fun getBlockCommentPrefix() = null
  override fun getBlockCommentSuffix() = null
}
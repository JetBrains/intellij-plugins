package com.intellij.openRewrite

import com.intellij.ide.IconProvider
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.PsiElement
import com.intellij.util.gist.GistAstMarker
import org.jetbrains.yaml.YAMLElementTypes
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.visitTopLevelKeyPairs
import javax.swing.Icon

internal class OpenRewriteIconProvider : IconProvider() {

  private val OPENREWRITE_MARKER = GistAstMarker(YAMLFileType.YML, "OPENREWRITE_YAML", ::isOpenRewriteRecipe)

  override fun getIcon(element: PsiElement, flags: Int): Icon? {
    if (element is YAMLFile) {
      val vFile = element.virtualFile ?: return null
      if (OPENREWRITE_MARKER.accepts(element.project, vFile)) {
        return OpenRewriteIcons.OpenRewrite
      }
    }

    return null
  }

  private fun isOpenRewriteRecipe(tree: LighterAST): Boolean {
    var foundRecipe = false

    visitTopLevelKeyPairs(tree) { key, pair ->
      if (key == "type") {
        val valueText = tree.getChildren(pair)
          .lastOrNull()
          ?.takeIf { it.tokenType == YAMLElementTypes.SCALAR_PLAIN_VALUE }
          ?.let { tree.getChildren(it).firstOrNull() }
          as? LighterASTTokenNode

        if (valueText != null && REWRITE_TYPE_REGEX.matches(valueText.text)) {
          foundRecipe = true
        }
      }

      !foundRecipe
    }

    return foundRecipe
  }
}
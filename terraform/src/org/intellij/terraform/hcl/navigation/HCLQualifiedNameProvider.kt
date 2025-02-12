// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.navigation

import com.intellij.ide.actions.QualifiedNameProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.*

class HCLQualifiedNameProvider : QualifiedNameProvider {
  override fun adjustElementToCopy(element: PsiElement): PsiElement? {
    return element.takeIf { it is HCLElement }
  }

  override fun getQualifiedName(element: PsiElement): String? {
    return Companion.getQualifiedName(element)
  }

  companion object {
    fun getQualifiedName(element: PsiElement?): String? {
      if (element !is HCLElement) return null

      if (element is HCLBlock) {
        return getFQN(element)
      }
      if (element is HCLProperty) {
//      TODO: Implement
//      return getFQN(element)
      }
      return null
    }

    fun getQualifiedModelName(element: PsiElement?): String? {
      if (element !is HCLElement) return null

      if (element is HCLBlock) {
        return getModelFQN(element)
      }
      if (element is HCLProperty) {
        val parent = element.parent
        if (parent is HCLBlock) { // Probably it's fake property, usual one would be under HCLObject
          val pName = getQualifiedModelName(parent)
          if (pName != null) {
            return "$pName.${element.name}"
          }
        } else if (parent is HCLObject && parent.parent is HCLBlock) {
          val pName = getQualifiedModelName(parent.parent)
          if (pName != null) {
            return "$pName.${element.name}"
          }
        } else return element.name
        // TODO: Check cases when there no fqn of parent block
      }
      return null
    }

    fun getFQN(block: HCLBlock): String? {
      var elements = block.nameElements.asList()

      if (TfPsiPatterns.ResourceRootBlock.accepts(block)) {
        elements = elements.drop(1)
      } else if (block.parent !is HCLFile) {
        // TODO: Implement
      }
      val sb = StringBuilder()
      elements.joinTo(sb, ".") { StringUtil.unescapeStringCharacters(HCLPsiUtil.stripQuotes(it.text)) }
      val result = sb.toString()
      if (result.isEmpty()) return null
      return result
    }

    private fun getModelFQN(block: HCLBlock): String? {
      var elements = block.nameElements.asList()
      val parent = block.parent
      var prefix: String? = null

      if (TfPsiPatterns.ResourceRootBlock.accepts(block)
          || TfPsiPatterns.DataSourceRootBlock.accepts(block)) {
        elements = elements.dropLast(1)
      } else if (parent !is HCLFile) {
        if (parent is HCLObject && parent.parent is HCLBlock) {
          prefix = getQualifiedModelName(parent.parent)
        } else {
          // TODO: Implement
        }
      }
      val sb = StringBuilder()
      if (prefix != null) {
        sb.append(prefix).append('.')
      }
      elements.joinTo(sb, ".") { StringUtil.unescapeStringCharacters(HCLPsiUtil.stripQuotes(it.text)) }
      val result = sb.toString()
      if (result.isEmpty()) return null
      return result
    }

  }

  override fun qualifiedNameToElement(fqn: String, project: Project): PsiElement? {
    // TODO: Implement: Search all models for resource/provider/etc
    return null
  }

  override fun insertQualifiedName(fqn: String, element: PsiElement, editor: Editor, project: Project) {
    EditorModificationUtil.insertStringAtCaret(editor, fqn)
  }

}

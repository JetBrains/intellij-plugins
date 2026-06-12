package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.javadoc.JavaDocInfoGeneratorFactory
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.model.Pointer
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.recipe.OpenRewriteOptionPsiElement
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteRecipePsiElement
import com.intellij.openRewrite.yaml.OpenRewriteYamlRecipeOptionReferenceProvider.RecipeOptionReference
import com.intellij.openRewrite.yaml.OpenRewriteYamlRecipeReferenceProvider.RecipeReference
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

private fun appendSection(sb: StringBuilder, sectionName: String, sectionContent: String) {
  sb.append(DocumentationMarkup.SECTION_HEADER_START)
    .append(sectionName)
    .append(":")
    .append(DocumentationMarkup.SECTION_SEPARATOR)
  sb.append(sectionContent)
  sb.append(DocumentationMarkup.SECTION_END)
}

private fun appendDescription(sb: StringBuilder, displayName: String?, description: String?) {
  if (displayName == null && description == null) return

  sb.append(DocumentationMarkup.CONTENT_START)
  if (displayName != null) {
    sb.append("<em>${displayName}</em>")
    sb.append("<br><br>")
  }
  if (description != null) {
    sb.append(description)
    sb.append("<br><br>")
  }
  sb.append(DocumentationMarkup.CONTENT_END)
}

internal class OpenRewriteYamlDocumentationProvider : PsiDocumentationTargetProvider {
  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    if (element is OpenRewriteRecipePsiElement) {
      return getRecipeDocumentationTarget(element.descriptor)
    }
    else if (element is OpenRewriteOptionPsiElement) {
      return getOptionDocumentationTarget(element)
    }
    val parent = element.parent ?: return null
    if (parent is YAMLKeyValue) {
      val references = parent.references
      val option = references.find { it is RecipeOptionReference }?.resolve() as? OpenRewriteOptionPsiElement
      if (option != null) {
        return getOptionDocumentationTarget(option)
      }
      val recipe = references.find { it is RecipeReference }?.resolve() as? OpenRewriteRecipePsiElement
      if (recipe != null) {
        return getRecipeDocumentationTarget(recipe.descriptor)
      }
    }
    else if (parent is YAMLScalar) {
      val recipe = parent.references.find { it is RecipeReference }?.resolve() as? OpenRewriteRecipePsiElement
      if (recipe != null) {
        return getRecipeDocumentationTarget(recipe.descriptor)
      }
    }
    return null
  }

  private fun getRecipeDocumentationTarget(descriptor: OpenRewriteRecipeDescriptor): DocumentationTarget {
    return object : DocumentationTarget {
      override fun createPointer(): Pointer<out DocumentationTarget> = Pointer.hardPointer(this)

      override fun computePresentation(): TargetPresentation {
        @NlsSafe val name = descriptor.name
        return TargetPresentation.builder(name).icon(OpenRewriteIcons.OpenRewrite).presentation()
      }

      override fun computeDocumentation(): DocumentationResult {
        val sb = StringBuilder(DocumentationMarkup.DEFINITION_START)
        sb.append("<b>").append(descriptor.name).append("</b><br>")
        sb.append(DocumentationMarkup.DEFINITION_END)
        appendDescription(sb, descriptor.displayName, descriptor.description)

        if (descriptor.options.isNotEmpty()) {
          val tableSb = StringBuilder("<table cellpadding=\"2\">")
          for (option in descriptor.options) {
            tableSb.append("<tr>")
            tableSb.append("<td valign='top'><pre>").append(option.name).append("</pre></td>")
            tableSb.append("<td valign='top'>").append(option.displayName).append("</td>")
            tableSb.append("</tr>")
          }
          tableSb.append("</table>")
          appendSection(sb, OpenRewriteBundle.message("open.rewrite.doc.options"), tableSb.toString())
        }

        @NlsSafe val documentation = sb.toString()
        return DocumentationResult.Companion.documentation(documentation)
      }
    }
  }

  private fun getOptionDocumentationTarget(element: OpenRewriteOptionPsiElement): DocumentationTarget {
    return object : DocumentationTarget {
      override fun createPointer(): Pointer<out DocumentationTarget> = Pointer.hardPointer(this)

      override fun computePresentation(): TargetPresentation {
        @NlsSafe val name = element.descriptor.name
        return TargetPresentation.builder(name).icon(OpenRewriteIcons.OpenRewrite).presentation()
      }

      override fun computeDocumentation(): DocumentationResult {
        val descriptor = element.descriptor
        val sb = StringBuilder(DocumentationMarkup.DEFINITION_START)

        sb.append("<b>").append(descriptor.name).append("</b><br>")
        val type = descriptor.typePointer.type
        if (type != null) {
          JavaDocInfoGeneratorFactory.create(element.getProject(), null).generateType(sb, type, element)
        }
        else {
          sb.append('(').append(OpenRewriteBundle.message("open.rewrite.doc.unknown.type")).append(')')
        }
        sb.append(DocumentationMarkup.DEFINITION_END)

        appendDescription(sb, descriptor.displayName, descriptor.description)

        sb.append(DocumentationMarkup.SECTIONS_START)
        appendSection(sb, OpenRewriteBundle.message("open.rewrite.doc.required"), "<b>${descriptor.required}</b>")
        if (descriptor.example != null) {
          appendSection(sb, OpenRewriteBundle.message("open.rewrite.doc.example"), "<pre>${descriptor.example}</pre>")
        }
        if (descriptor.valid.isNotEmpty()) {
          appendSection(sb, OpenRewriteBundle.message("open.rewrite.doc.values"), "<pre>${descriptor.valid.joinToString()}</pre>")
        }
        sb.append(DocumentationMarkup.SECTIONS_END)

        @NlsSafe val documentation = sb.toString()
        return DocumentationResult.Companion.documentation(documentation)
      }
    }
  }
}
package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.OwnBypassCollector
import com.intellij.openRewrite.isRecipe
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.recipe.OpenRewriteType
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence

private fun getRecipeList(topMapping: YAMLMapping, key: String): YAMLSequence? {
  return topMapping.getKeyValueByKey(key)?.value as? YAMLSequence
}

private fun addPresentation(descriptor: OpenRewriteRecipeDescriptor, anchor: PsiElement, sink: InlayTreeSink) {
  val displayName = descriptor.displayName ?: return
  val text = displayName.substringBefore('\n')
  sink.addPresentation(InlineInlayPosition(anchor.textRange.endOffset, true), hasBackground = true) {
    text(text)
  }
}

internal class OpenRewriteRecipeInlayHintsProvider : InlayHintsProvider {
  override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector? {
    if (!isRecipe(file)) return null

    return object : OwnBypassCollector {
      override fun collectHintsForFile(file: PsiFile, sink: InlayTreeSink) {
        val documents = (file as? YAMLFile)?.documents ?: return
        for (document in documents) {
          val mapping = document.topLevelValue as? YAMLMapping ?: continue
          for (type in OpenRewriteType.entries) {
            val keys = listOf(type.listKey) + type.additionalListKeys
            for (key in keys) {
              val sequence = getRecipeList(mapping, key) ?: continue
              val descriptors = OpenRewriteRecipeService.getInstance(file.project).getDescriptors(file, type)
              for (item in sequence.items) {
                val value = item.value
                if (value is YAMLScalar) {
                  val valueText = value.textValue
                  val descriptor = descriptors.find { it.name == valueText } ?: continue
                  addPresentation(descriptor, value, sink)
                }
                else {
                  for (keysValue in item.keysValues) {
                    val keyText = keysValue.keyText
                    val descriptor = descriptors.find { it.name == keyText } ?: continue
                    val colon = keysValue.node.findChildByType(YAMLTokenTypes.COLON)?.psi ?: continue
                    addPresentation(descriptor, colon, sink)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
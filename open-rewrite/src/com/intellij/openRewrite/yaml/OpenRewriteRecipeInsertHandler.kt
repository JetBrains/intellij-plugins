package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.TemplateBuilderFactory
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

internal class OpenRewriteRecipeInsertHandler(val descriptor: OpenRewriteRecipeDescriptor,
                                              val project: Project) : InsertHandler<LookupElement> {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val psiElement = PsiUtilCore.getElementAtOffset(context.file, context.startOffset)
    val sequenceItemScalar = psiElement.parent as? YAMLScalar ?: return

    insertOptionKeyValues(sequenceItemScalar)
    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(context.editor.document)
    runOptionValueTemplate(context)
  }

  private fun insertOptionKeyValues(sequenceItemScalar: YAMLScalar) {
    val sb = StringBuilder(sequenceItemScalar.textValue).append(":")
    val requiredOptions = descriptor.options.filter { option -> option.required }.map { option -> option.name }
    val indent = YAMLUtil.getIndentToThisElement(sequenceItemScalar) + 2
    val intentString = StringUtil.repeatSymbol(' ', indent);
    for (requiredOption in requiredOptions) {
      sb.append("\n").append(intentString).append(requiredOption).append(": value")
    }

    val dummyFile = YAMLElementGenerator.getInstance(project).createDummyYamlWithText(sb.toString())
    val topLevelKeys = YAMLUtil.getTopLevelKeys(dummyFile)
    if (topLevelKeys.isEmpty()) {
      throw IllegalStateException("no top level keys: $sb")
    }
    val dummyKeyValue = topLevelKeys.iterator().next()
    sequenceItemScalar.replace(dummyKeyValue.parentMapping!!)
  }

  private fun runOptionValueTemplate(context: InsertionContext) {
    val newElement = PsiUtilCore.getElementAtOffset(context.file, context.startOffset)
    val recipeKeyValue = newElement.parent as? YAMLKeyValue ?: return
    val recipeMapping = recipeKeyValue.value as? YAMLMapping ?: return
    val builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(recipeKeyValue)
    for (keyValue in recipeMapping.keyValues) {
      val value = keyValue.value ?: continue
      builder.replaceElement(value, "")
    }
    builder.run(context.editor, false)
  }
}
package com.intellij.openRewrite.yaml

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.isRecipe
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.yaml.OpenRewriteYamlRecipeOptionValueReferenceProvider.RecipeOptionValueReference
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequenceItem
import org.jetbrains.yaml.psi.YamlPsiElementVisitor

internal class OpenRewriteYamlRecipeInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (!isRecipe(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

    return object : YamlPsiElementVisitor() {
      override fun visitKeyValue(keyValue: YAMLKeyValue) {
        super.visitKeyValue(keyValue)
        val keyElement = keyValue.key ?: return
        val type = getKeyValueType(keyValue) ?: return

        val descriptor =
          OpenRewriteRecipeService.getInstance(keyValue.project).findDescriptor(keyValue.keyText, holder.file, type)
          ?: return

        val requiredOptions = descriptor.options.filter { it.required }.map { it.name }
        if (requiredOptions.isEmpty()) return

        val existingOptions = (keyValue.value as? YAMLMapping)?.keyValues?.map { it.keyText }?.toSet() ?: emptySet()
        for (requiredOption in requiredOptions) {
          if (existingOptions.contains(requiredOption)) continue

          holder.registerProblem(keyElement,
                                 OpenRewriteBundle.message("open.rewrite.yaml.missing.required.option", requiredOption),
                                 ProblemHighlightType.ERROR,
                                 AddOptionQuickFix(requiredOption))
        }
      }

      override fun visitScalar(scalar: YAMLScalar) {
        super.visitScalar(scalar)

        val reference = scalar.references.find { it is RecipeOptionValueReference }
        if (reference != null) {
          val valueReference = reference as RecipeOptionValueReference
          val descriptor = valueReference.findRecipeOptionDescriptor() ?: return

          val type = descriptor.typePointer.type?.canonicalText
          if (type != null && notConvertedToPrimitiveType(scalar.textValue, type)) {
            holder.registerProblem(scalar,
                                   OpenRewriteBundle.message("open.rewrite.unresolved.number.value",
                                                             type,
                                                             scalar.textValue),
                                   ProblemHighlightType.ERROR)
          }

          if (descriptor.valid.isNotEmpty() && !descriptor.valid.contains(scalar.textValue)) {
            holder.registerProblem(scalar,
                                   OpenRewriteBundle.message("open.rewrite.unresolved.static.value",
                                                             descriptor.valid.joinToString("|"),
                                                             scalar.textValue),
                                   ProblemHighlightType.ERROR)
          }
          return
        }

        val parent = scalar.parent
        val type = getSequenceItemType(parent) ?: return

        val descriptor =
          OpenRewriteRecipeService.getInstance(scalar.project).findDescriptor(scalar.textValue, holder.file, type)
          ?: return

        val requiredOptions = descriptor.options.filter { it.required }.map { it.name }

        for (requiredOption in requiredOptions) {
          holder.registerProblem(scalar,
                                 OpenRewriteBundle.message("open.rewrite.yaml.missing.required.option", requiredOption),
                                 ProblemHighlightType.ERROR,
                                 AddOptionQuickFix(requiredOption))
        }
      }
    }
  }

  private fun notConvertedToPrimitiveType(text: String, type: String): Boolean {
    return when (type) {
      JvmPrimitiveTypeKind.BOOLEAN.name, JvmPrimitiveTypeKind.BOOLEAN.boxedFqn -> text != "true" && text != "false"
      JvmPrimitiveTypeKind.INT.name, JvmPrimitiveTypeKind.INT.boxedFqn -> {
        try {
          text.toInt()
          false
        }
        catch (e: NumberFormatException) {
          true
        }
      }
      JvmPrimitiveTypeKind.LONG.name, JvmPrimitiveTypeKind.LONG.boxedFqn -> {
        try {
          text.toLong()
          false
        }
        catch (e: NumberFormatException) {
          true
        }
      }
      JvmPrimitiveTypeKind.SHORT.name, JvmPrimitiveTypeKind.SHORT.boxedFqn -> {
        try {
          text.toShort()
          false
        }
        catch (e: NumberFormatException) {
          true
        }
      }
      JvmPrimitiveTypeKind.FLOAT.name, JvmPrimitiveTypeKind.FLOAT.boxedFqn -> {
        try {
          text.toFloat()
          false
        }
        catch (e: NumberFormatException) {
          true
        }
      }
      JvmPrimitiveTypeKind.DOUBLE.name, JvmPrimitiveTypeKind.DOUBLE.boxedFqn -> {
        try {
          text.toDouble()
          false
        }
        catch (e: NumberFormatException) {
          true
        }
      }
      else -> false
    }
  }

  private class AddOptionQuickFix(private val option: String) : LocalQuickFix {
    override fun getFamilyName(): String = OpenRewriteBundle.message("open.rewrite.yaml.add.required.option", option)

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val optionKeyValue = YAMLElementGenerator.getInstance(project).createYamlKeyValue(option, "")
      val psiElement = descriptor.psiElement
      val parent = psiElement.parent
      if (parent is YAMLKeyValue) {
        applyFix(parent, optionKeyValue)
      }
      else if (parent is YAMLSequenceItem && psiElement is YAMLScalar) {
        applyFix(psiElement, optionKeyValue, project)
      }
    }

    private fun applyFix(keyValue: YAMLKeyValue, optionKeyValue: YAMLKeyValue) {
      val value = keyValue.value
      if (value is YAMLMapping) {
        value.putKeyValue(optionKeyValue)
      }
      else if (value != null) {
        value.replace(optionKeyValue.parentMapping!!)
      }
      else {
        keyValue.setValue(optionKeyValue.parentMapping!!)
      }
    }

    private fun applyFix(sequenceItemScalar: YAMLScalar, optionKeyValue: YAMLKeyValue, project: Project) {
      val itemMapping = YAMLElementGenerator.getInstance(project).createYamlKeyValue(sequenceItemScalar.textValue, "dummy")
      itemMapping.getValue()!!.replace(optionKeyValue.parentMapping!!)
      sequenceItemScalar.replace(itemMapping.parentMapping!!)
    }
  }
}
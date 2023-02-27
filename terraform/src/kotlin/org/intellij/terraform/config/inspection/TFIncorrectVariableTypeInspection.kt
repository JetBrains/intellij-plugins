/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.joinCommaOr

class TFIncorrectVariableTypeInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (!TerraformPatterns.TerraformConfigFile.accepts(file)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      if (!TerraformPatterns.VariableRootBlock.accepts(block)) return

      val obj = block.`object` ?: return

      val typeProperty = obj.findProperty(TypeModel.Variable_Type.name)
      val typePropertyValue = typeProperty?.value ?: return

      if (typePropertyValue is HCLStringLiteral) {
        /*
        The legacy variable type hint form, using a quoted string, allows only the
        values "string", "list", and "map". To provide a full type expression, remove
        the surrounding quotes and give the type expression directly.
         */
        checkVariableTypeDefinitionLegacy(block, typePropertyValue)
      } else {
        checkVariableTypeDefinition(block, typePropertyValue)
      }
    }

    private fun checkVariableTypeDefinitionLegacy(block: HCLBlock, typePropertyValue: HCLStringLiteral) {
      val legacyAllowedValues = listOf("string", "list", "map")
      val expected = typePropertyValue.value
      if (expected !in legacyAllowedValues) {
        holder.registerProblem(typePropertyValue, HCLBundle.message("incorrect.variable.type.inspection.legacy.variable.error.message",
                                                                    joinCommaOr(legacyAllowedValues)))
      }

      val defaultProperty = block.`object`?.findProperty(TypeModel.Variable_Default.name) ?: return

      val value = defaultProperty.value as? HCLValue ?: return
      val actual = value.getType() ?: return

      if (actual == Types.Null) return // Allowed in Terraform 0.12

      if ((expected == "string" && actual !in Types.SimpleValueTypes)
          || (expected == "list" && !isListType(actual))
          || (expected == "map" && !isObjectType(actual))) {
        val to: String = when {
          actual in Types.SimpleValueTypes -> "string"
          isListType(actual) -> "list"
          isObjectType(actual) -> "map"
          else -> {
            holder.registerProblem(value, HCLBundle.message("incorrect.variable.type.inspection.type.mismatch.error.message",
                                                            actual.presentableText, expected))
            return
          }
        }
        holder.registerProblem(value,
                               HCLBundle.message("incorrect.variable.type.inspection.type.mismatch.simplified.error.message", expected),
                               ChangeVariableType(to))
      }
    }

    private fun checkVariableTypeDefinition(block: HCLBlock, value: HCLExpression) {
      // next statement will also check type and add warnings if needed
      val expected = TypeSpecificationValidator(holder, true).getType(value) ?: return

      val defaultValue = block.`object`?.findProperty(TypeModel.Variable_Default.name)?.value as? HCLValue ?: return
      val actual = defaultValue.getType() ?: return

      if (actual == Types.Null) return // Allowed in Terraform 0.12

      if (!actual.isConvertibleTo(expected)) {
        holder.registerProblem(defaultValue, HCLBundle.message("incorrect.variable.type.inspection.type.mismatch.error.message",
                                                               actual.presentableText, expected.presentableText))
      }
    }
  }

  private class ChangeVariableType(val toType: String) : LocalQuickFix {
    override fun getFamilyName(): String = HCLBundle.message("incorrect.variable.type.inspection.change.type.quick.fix.name", toType)

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val element = descriptor.psiElement as? HCLValue ?: return
      val property = element.parent as? HCLProperty ?: return
      val obj = property.parent as? HCLObject ?: return
      val typeProperty = obj.findProperty(TypeModel.Variable_Type.name)

      if (typeProperty == null) {
        obj.addAfter(HCLElementGenerator(project).createProperty("type", "\"$toType\""), obj.firstChild)
      }
      else {
        // Replace property value, though it may create errors in .tfvars
        typeProperty.value!!.replace(HCLElementGenerator(project).createStringLiteral(toType))
      }
    }
  }
}

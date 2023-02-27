/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
package org.intellij.terraform.hil.psi

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.*
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hil.HILElementTypes
import org.intellij.terraform.hil.codeinsight.HILCompletionContributor
import org.intellij.terraform.hil.psi.impl.getHCLHost
import java.util.*

class TypeCachedValueProvider private constructor(private val e: BaseExpression) : CachedValueProvider<Type?> {

  companion object {
    fun getType(e: BaseExpression): Type? {
      return CachedValuesManager.getCachedValue(e, TypeCachedValueProvider(e))
    }

    private val LOG = Logger.getInstance(TypeCachedValueProvider::class.java)

    private fun doGetType(e: ParenthesizedExpression<*>): Type? {
      val expression = e.expression ?: return Types.Any
      return expression.getType()
    }

    private fun doGetType(e: LiteralExpression): Type? {
      return when (e) {
        is ILLiteralExpression -> when {
          e.doubleQuotedString != null -> Types.String
          e.number != null -> Types.Number
          "true".equals(e.text, true) -> Types.Boolean
          "false".equals(e.text, true) -> Types.Boolean
          else -> null
        }
        is HCLNullLiteral -> Types.Null
        is HCLBooleanLiteral -> Types.Boolean
        is HCLNumberLiteral -> Types.Number
        is HCLStringLiteral -> Types.String
        is HCLHeredocLiteral -> Types.String
        else -> null
      }
    }

    private val UnaryNumberOps = TokenSet.create(
        HILElementTypes.OP_PLUS, HILElementTypes.OP_MINUS,
        HCLElementTypes.OP_PLUS, HCLElementTypes.OP_MINUS
    )
    private val UnaryBooleanOps = TokenSet.create(
        HILElementTypes.OP_NOT,
        HCLElementTypes.OP_NOT
    )

    private fun doGetType(e: UnaryExpression<*>): Type? {
      return when (val sign = e.operationSign) {
        in UnaryNumberOps -> Types.Number
        in UnaryBooleanOps -> Types.Boolean
        else -> {
          LOG.error("Unexpected operation sign of UnaryExpression: $sign", e.text)
          null
        }
      }
    }

    private val BinaryNumberOps = TokenSet.create(
        HILElementTypes.IL_BINARY_ADDITION_EXPRESSION, HILElementTypes.IL_BINARY_MULTIPLY_EXPRESSION,
        HCLElementTypes.BINARY_ADDITION_EXPRESSION, HCLElementTypes.BINARY_MULTIPLY_EXPRESSION
    )
    private val BinaryBooleanOps = TokenSet.create(
        HILElementTypes.IL_BINARY_RELATIONAL_EXPRESSION, HILElementTypes.IL_BINARY_EQUALITY_EXPRESSION,
        HILElementTypes.IL_BINARY_AND_EXPRESSION, HILElementTypes.IL_BINARY_OR_EXPRESSION,
        HCLElementTypes.BINARY_RELATIONAL_EXPRESSION, HCLElementTypes.BINARY_EQUALITY_EXPRESSION,
        HCLElementTypes.BINARY_AND_EXPRESSION, HCLElementTypes.BINARY_OR_EXPRESSION
    )

    private fun doGetType(e: BinaryExpression<*>): Type? {
      return when (val et = e.node.elementType) {
        in BinaryNumberOps -> Types.Number
        in BinaryBooleanOps -> Types.Boolean
        else -> {
          LOG.error("Unexpected operation sign of BinaryExpression: $et", e.text)
          null
        }
      }
    }

    private fun doGetType(e: ConditionalExpression<*>): Type? {
      val first = e.then
      val second = e.otherwise
      val l = first.getType()
      val r = second.getType()

      // There's some strange logic in HIL eval_test.go:
      // > // false expression is type-converted to match true expression
      // > // true expression is type-converted to match false expression if the true expression is string
      if (l == r) return l
      if (l == null) return r
      if (r == null) return l
      if (l == Types.Any || r == Types.Any) return Types.Any
      if (l == Types.String) return r
      return l
    }

    private fun doGetType(e: CollectionExpression<*>): Type? {
      return when (e) {
        is ParameterList<*> -> doGetType(e)
        is HCLArray, is ILArray -> {
          val innerTypes = e.elements.map { it.getType() }.toSet()
          when (innerTypes.size) {
            0 -> ListType(null)
            1 -> ListType(innerTypes.first())
            else -> ListType(getCommonSupertype(innerTypes) ?: Types.Any)
          }
        }
        is HCLObject -> {
          val result: MutableMap<String, Type?> = TreeMap()
          // TODO: Consider using TF model instead
          for (prop in e.propertyList) {
            result[prop.name] = prop.value.getType()
          }
          for (block in e.blockList) {
            result[block.name] = block.`object`.getType()
          }
          ObjectType(result)
        }
        is ILObject -> {
          val result: MutableMap<String, Type?> = TreeMap()
          // TODO: Consider using TF model instead
          for (prop in e.propertyList) {
            result[prop.name] = prop.value.getType()
          }
          ObjectType(result)
        }
        else -> {
          LOG.error("Unexpected collection expression type: ${e.javaClass.name}", e.text)
          null
        }
      }
    }

    private fun doGetType(e: ParameterList<*>): Type? {
      LOG.error("#getType should not be called for ILParameterList", (e as PsiElement).text)
      return null
    }

    private fun doGetType(e: SelectExpression<*>): Type? {
      // TODO: Implement
      // Possible cases:
      // * for variable reference
      // * count.index, each.key, each.value, self
      // * resource references (single, count, for_each)

      // For now return 'Any' to fix HILOperationTypesMismatchInspection
      val fromType = e.from.getType()

      if (isSplatSelection(e)) {
        // "splat", may convert left part to single-element array if it's not list already
        if (fromType is SetType) {
          return ListType(fromType.elements)
        }
        if (isListType(fromType)) {
          return fromType
        }
        if (fromType is PrimitiveType || isObjectType(fromType)) {
          return ListType(fromType)
        }
        if (e.isInTerraformFile()) {
          when (fromType) {
            is ResourceType, is DataSourceType, is ModuleType -> return ListType(fromType)
          }
        }
        return Types.Invalid
      }

      if (e.field is HCLNumberLiteral || (e.field as? ILLiteralExpression)?.number != null) {
        if (fromType is ObjectType) return getCommonSupertype(fromType.elements?.values ?: emptyList()) ?: Types.Any
        if (fromType is ListType) return fromType.elements ?: Types.Any
        if (fromType is SetType) return fromType.elements ?: Types.Any
        if (fromType is MapType) return fromType.elements ?: Types.Any
      }

      if (e.isInTerraformFile()) {
        if (e is IndexSelectExpression<*>) {
          // supported above or at the end as general selection from ObjectType or MapType
        } else if (HILCompletionContributor.ILSE_FROM_KNOWN_SCOPE.accepts(e)) {
          val from = e.from as Identifier // type checked by ILSE_FROM_KNOWN_SCOPE
          val name = e.field?.text ?: return Types.Invalid
          val module = e.getHCLHost()?.getTerraformModule() ?: return Types.Invalid
          when (from.name) {
            "var" -> {
              val variables = module.findVariables(name)
              if (variables.isNotEmpty()) {
                val variable = variables.first()
                if (!PsiTreeUtil.isAncestor(variable.declaration, e, false))
                  return variable.getCombinedType() ?: Types.Any
              }
              return Types.Any
            }
            "local" -> {
              return module.findLocal(name)?.second?.value?.getType() ?: Types.Any
            }
            "module" -> {
              val modules = module.findModules(name).mapNotNull { Module.getAsModuleBlock(it) }
              if (modules.isNotEmpty()) {
                val mod = modules.first()
                return mod.getType()
              }
            }
          }
        } else if (HILCompletionContributor.ILSE_NOT_FROM_KNOWN_SCOPE.accepts(e)) {
          val name = e.field?.text ?: return Types.Invalid
          if (e.from is Identifier) {
            // first level select expression, e.g. `aws_instance.x`
            val from = e.from as Identifier
            val module = e.getHCLHost()?.getTerraformModule() ?: return Types.Invalid

            val references = from.references
            if (references.isNotEmpty()) {
              // e.g. for variable reference of dynamic block identifier
              LOG.warn("Element: ${e.text} From-References: $references")
            } else {
              // TODO: Support not only resources
              // TODO: Add properties defined in block as well
              val block = module.findResources(from.name, name).firstOrNull() ?: return Types.Invalid
              val blockType = ModelHelper.getBlockType(block)
              return blockType ?: Types.Invalid
            }
          } else if (HILCompletionContributor.ILSE_FROM_DATA_SCOPE.accepts(e.from)) {
            // `data.aws_instance.x`
            val from = (e.from as SelectExpression<*>).field as? Identifier ?: return Types.Invalid
            val module = e.getHCLHost()?.getTerraformModule() ?: return Types.Invalid

            val block = module.findDataSource(from.name, name).firstOrNull() ?: return Types.Invalid
            val blockType = ModelHelper.getBlockType(block)
            return blockType ?: Types.Invalid
          }
          if (fromType is ObjectType) {
            return fromType.elements?.get(name) ?: Types.Invalid
          }
        }
      }

      val name = when (e.field) {
        is LiteralExpression -> (e.field as LiteralExpression).unquotedText
        is Identifier -> (e.field as Identifier).name
        else -> null
      }
      if (name != null) {
        if (isSplatSelection(e.from)) {
          if (fromType is ListType) {
            return ListType(selectFromMapType(fromType.elements, name))
          }
          if (fromType is SetType) {
            return SetType(selectFromMapType(fromType.elements, name))
          }
          // TODO: check other variants
          // TupleType behaves strange in splat operator, sometimes converts to List, sometimes to List<Tuple>
          // ParenthesizedExpression may kinda-break splat operator
        }
        val result = selectFromMapType(fromType, name)
        if (result != null) return result
      }

      return Types.Any
    }

    private fun selectFromMapType(fromType: Type?, name: String?): Type? {
      if (fromType is ObjectType) {
        val elements = fromType.elements ?: return Types.Any
        return elements[name] ?: Types.Invalid
      }
      if (fromType is MapType) {
        return fromType.elements ?: Types.Any
      }
      return null
    }

    private fun isSplatSelection(expr: BaseExpression): Boolean {
      if (expr !is SelectExpression<*>) return false
      val field = expr.field
      if (field is Identifier && field.text == "*") return true
      if (field is LiteralExpression && field.unquotedText == "*") return true
      return false
    }

    private fun doGetType(e: MethodCallExpression<*>): Type? {
      if (e.isInTerraformFile()) {
        return FunctionsReturnTypeHelper.getType(e)
      }
      return Types.Any
    }

    private fun doGetType(e: HCLForExpression): Type? {
      return when (e) {
        is HCLForObjectExpression -> {
          val valueType = e.value.getType() ?: Types.Any
          // Using MapType instead of ObjectType as it'simpossible to determine key names without evaluation
          if (e.isGrouping) {
            MapType(ListType(valueType))
          } else {
            MapType(valueType)
          }
        }
        is HCLForArrayExpression -> {
          ListType(e.expression.getType())
        }
        else -> {
          LOG.error("Unexpected for expression type: ${e.javaClass.name}", e.text)
          null
        }
      }
    }

    private fun doGetType(e: Identifier): Type? {
      // TODO: Implement
      // Possible cases:
      // * dynamic block iterator
      if (HCLPsiUtil.isPartOfPropertyKey(e)) {
        return null
      }
      if (TerraformPatterns.ForVariable.accepts(e)) {
        val p = e.parent as HCLForIntro
        val containerType = p.container?.getType() ?: return null
        return if ((p.var2 === null && p.var1 === e) || p.var2 === e) {
          // Array element or Object value
          when (containerType) {
            is ListType -> containerType.elements ?: Types.Any
            is SetType -> containerType.elements ?: Types.Any
            is TupleType -> getCommonSupertype(containerType.elements) ?: Types.Any
            is MapType -> containerType.elements ?: Types.Any
            is ObjectType -> getCommonSupertype(containerType.elements?.values ?: emptyList()) ?: Types.Any
            else -> null
          }
        } else {
          // Array index or Object key
          when {
            isListType(containerType) -> Types.Number
            isObjectType(containerType) -> Types.String
            else -> null
          }
        }
      }
      val references = e.references
      val forVariableReference = references.filterIsInstance(ForVariableDirectReference::class.java).firstOrNull()
      if (forVariableReference != null) {
        val resolved = forVariableReference.resolve() as? BaseExpression
        return resolved?.getType()
      }
      return Types.Identifier
    }
  }


  override fun compute(): CachedValueProvider.Result<Type?>? {
    return when (e) {
      is ParenthesizedExpression<*> -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }
      is LiteralExpression -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }
      is UnaryExpression<*> -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }
      is BinaryExpression<*> -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }
      is ConditionalExpression<*> -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }
      is SelectExpression<*> -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }
      is MethodCallExpression<*> -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }
      is ParameterList<*> -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }
      is CollectionExpression<*> -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }

      is Identifier -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }

      is HCLForExpression -> doGetType(e)?.let { CachedValueProvider.Result.create(it, e) }

      // Errors:
      else -> {
        LOG.error("Unexpected #getType call for ${e.javaClass.name}", e.text)
        return null
      }
    }
  }

}
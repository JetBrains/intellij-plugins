// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.model.*
import org.jetbrains.annotations.Nls

// Beware: validator have recursive nature, must be called only on HCLExpression which is root of type specification
open class TypeSpecificationValidator(private val holder: ProblemsHolder?,
                                      private val constraint: Boolean,
                                      private val supportArglessTypes: Boolean = false) {
  protected open fun error(element: PsiElement, @Nls description: String, range: TextRange? = null): Type? {
    holder?.registerProblem(element, range, description)
    return null
  }

  fun getType(e: HCLExpression): Type? {
    return when (e) {
      is HCLIdentifier -> checkIdentifier(e)
      is HCLMethodCallExpression -> checkMethodCallExpression(e)
      else -> error(e, HCLBundle.message("type.specification.validator.illegal.type.specification.error.message"))
    }
  }

  private fun checkIdentifier(o: HCLIdentifier): Type? {
    return when (val kw = o.id) {
      "bool" -> Types.Boolean
      "string" -> Types.String
      "number" -> Types.Number
      "any" -> {
        if (constraint) Types.Any
        else error(o, HCLBundle.message("type.specification.validator.exact.type.required.error.message", kw))
      }
      "list", "set", "map" -> {
        if (!supportArglessTypes) {
          error(o, HCLBundle.message("type.specification.validator.collection.argument.required.error.message", kw))
        }
        else when (kw) {
          "list" -> ListType(null)
          "set" -> SetType(null)
          "map" -> MapType(null)
          else -> Types.Any
        }
      }
      "object" -> {
        if (!supportArglessTypes) {
          error(o, HCLBundle.message("type.specification.validator.object.argument.required.error.message"))
        }
        else {
          ObjectType(null)
        }
      }
      "tuple" -> {
        if (!supportArglessTypes) {
          error(o, HCLBundle.message("type.specification.validator.tuple.argument.required.error.message"))
        }
        else {
          TupleType(emptyList())
        }
      }
      else -> error(o, HCLBundle.message("type.specification.validator.invalid.type.specification.error.message", kw))
    }
  }

  private fun checkMethodCallExpression(e: HCLMethodCallExpression): Type? {
    // In case of error fail-fast, do not descend
    val method = e.callee
    val methodName = method.id
    when (methodName) {
      "bool", "string", "number", "any" -> return error(e.callee,
                                                        HCLBundle.message("type.specification.validator.no.argument.expected.error.message",
                                                                          methodName))
      "list", "set", "map", "optional", "object", "tuple" -> {}
      else -> error(e.callee, HCLBundle.message("type.specification.validator.invalid.type.specification.error.message", e.callee.text))
    }

    val params = e.parameterList.elements
    val paramsSize = params.size
    if (paramsSize != 1) {
      val range = if (paramsSize > 1) {
        TextRange(params[1].textRangeInParent.startOffset, params.last().textRangeInParent.endOffset)
      } else null
      when (methodName) {
        "list", "set", "map", "optional" -> {
          return error(e.parameterList,
                       HCLBundle.message("type.specification.validator.collection.argument.required.error.message", methodName), range)
        }
        "object" -> {
          return error(e.parameterList, HCLBundle.message("type.specification.validator.object.argument.required.error.message"), range)
        }
        "tuple" -> {
          return error(e.parameterList, HCLBundle.message("type.specification.validator.tuple.argument.required.error.message"), range)
        }
      }
    }
    val firstArgument = params.first()
    when (methodName) {
      "bool", "string", "number", "any" -> return error(e.callee,
                                                        HCLBundle.message("type.specification.validator.no.argument.expected.error.message",
                                                                          methodName))
      "list" -> return ListType(getType(firstArgument))
      "set" -> return SetType(getType(firstArgument))
      "map" -> return MapType(getType(firstArgument))
      "optional" -> return OptionalType(getType(firstArgument))
      "object" -> {
        if (firstArgument !is HCLObject) {
          return error(firstArgument, HCLBundle.message("type.specification.validator.object.argument.map.required.error.message"))
        }
        firstArgument.blockList.forEach {
          error(it, HCLBundle.message("type.specification.validator.block.not.allowed.error.message"))
        }
        val map = firstArgument.propertyList.map {
          if (it.nameElement !is HCLIdentifier) {
            error(it.nameElement,
                  HCLBundle.message("type.specification.validator.object.constructor.map.keys.must.be.attribute.names.error.message"))
          }
          it.name to it.value?.let { expr -> getType(expr) }
        }.toMap()
        return ObjectType(map)
      }
      "tuple" -> {
        if (firstArgument !is HCLArray) {
          return error(firstArgument, HCLBundle.message("type.specification.validator.tuple.argument.required.error.message"))
        }
        return TupleType(firstArgument.elements.map { getType(it) })
      }
      else -> error(e.callee, HCLBundle.message("type.specification.validator.invalid.type.constructor.error.message", methodName))
    }
    return null
  }
}
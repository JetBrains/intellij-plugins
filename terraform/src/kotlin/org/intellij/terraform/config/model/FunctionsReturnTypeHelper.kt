// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.openapi.application.ApplicationManager
import org.intellij.terraform.hcl.psi.common.MethodCallExpression

object FunctionsReturnTypeHelper {
  fun getType(e: MethodCallExpression<*>): Type? {
    val method = e.method?.name
    return if (method != null && e.callee === e.method) {
      // TODO: Find another way to support functions, extract to separate class?
      val args = e.parameterList.elements
      val firstArgType = args.firstOrNull().getType()
      when (method) {
        // fix all functions with Any as return type in schema
        "coalesce" -> {
          if (args.isEmpty()) return Types.Invalid
          return getCommonSupertype(args.map { it.getType() }) ?: Types.Any
        }
        "coalescelist" -> {
          if (args.isEmpty()) return Types.Invalid
          return getCommonSupertype(args.map { it.getType() }) ?: ListType(Types.Any)
        }
        "concat" -> {
          if (args.isEmpty()) return Types.Invalid
          return getCommonSupertype(args.map { it.getType() }) ?: ListType(Types.Any)
        }
        "contains" -> return Types.Boolean
        "csvdecode" -> return ListType(MapType(Types.String))
        "element" -> {
          if (firstArgType == null) return Types.Any
          if (firstArgType is ListType) return firstArgType.elements
          if (firstArgType is TupleType) return getCommonSupertype(firstArgType.elements)
          return Types.Invalid
        }
        "flatten" -> return ListType(Types.Any) // TODO: Improve
        "index" -> return Types.Number
        // "jsondecode" -> // could be anything
        "keys" -> return ListType(Types.String)
        "lookup" -> {
          if (firstArgType == null) return Types.Any
          if (firstArgType is MapType) return firstArgType.elements
          if (firstArgType is ObjectType) return args.getOrNull(2).getType() ?: Types.Any // TODO: Improve, try finding key in object
          return Types.Invalid
        }
        // "merge" -> // quite complex to implement
        "parseint" -> Types.Number
        // "regex" -> // either String, List(String) or Map(String)
        "reverse" -> {
          if (firstArgType == null) return Types.Any
          if (firstArgType is TupleType) return TupleType(firstArgType.elements.reversed())
          if (isListType(firstArgType)) return firstArgType
          return Types.Invalid
        }
        // "setproduct" -> quite complex
        "slice" -> {
          if (firstArgType == null) return Types.Any
          if (firstArgType is ListType) return firstArgType
          if (firstArgType is TupleType) return firstArgType // TODO: Actually get elements types
          return Types.Invalid
        }
        // "templatefile" -> truly any
        "tobool" -> return Types.Boolean
        "tonumber" -> return Types.Number
        "tostring" -> return Types.String
        "tolist" -> return ListType(Types.Any)
        "toset" -> return SetType(Types.Any)
        "tomap" -> return MapType(Types.Any)
        "values" -> {
          if (firstArgType == null) return Types.Any
          if (firstArgType is MapType) return ListType(firstArgType.elements)
          return Types.Invalid
        }
        // "yamldecode" -> // could be anything
        "zipmap" -> {
          val secondArgType = args.getOrNull(1).getType()
          if (secondArgType == null) return MapType(Types.Any)
          if (secondArgType is ListType) return MapType(secondArgType.elements)
          if (secondArgType is SetType) return MapType(secondArgType.elements)
          if (secondArgType is TupleType) return MapType(getCommonSupertype(secondArgType.elements))
          return Types.Invalid
        }

        "totuple" -> {
          if (ApplicationManager.getApplication().isUnitTestMode) {
            return TupleType(args.map { it.getType() })
          }
        }
      }
      val function = TypeModelProvider.getModel(e.project).getFunction(method)
      function?.ret
    } else {
      null
    }
  }

}
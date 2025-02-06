// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

open class Argument(val type: Type, val name: String? = null)
open class VariadicArgument(type: Type, name: String? = null) : Argument(type, name)

class TfFunction(val name: String,
                 val returnType: Type,
                 vararg val arguments: Argument = emptyArray(),
                 val description: String? = null,
                 val variadic: VariadicArgument? = null,
                 val providerType: String? = null
) {
  val presentableName: String
    get() = if (providerType != null) "provider::$providerType::$name" else name

  fun getArgumentsAsText(): String = arguments.joinToString(separator = ", ", prefix = "(", postfix = ")") { "${it.name}: ${it.type}" }

  init {
    val count = arguments.count { it is VariadicArgument }
    assert (count == 0 || (count == 1 && arguments.last() is VariadicArgument)) { "Only one (last) argument could be variadic" }
  }
}
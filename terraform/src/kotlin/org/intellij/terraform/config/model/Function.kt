// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

open class Argument(val type: Type, val name: String? = null)
open class VariadicArgument(type: Type, name: String? = null) : Argument(type, name)

class Function(val name: String, val ret: Type, vararg val arguments: Argument = emptyArray(), val variadic: VariadicArgument? = null) {
  init {
    val count = arguments.count { it is VariadicArgument }
    assert (count == 0 || (count == 1 && arguments.last() is VariadicArgument)) { "Only one (last) argument could be variadic" }
  }
}
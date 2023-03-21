// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.componentDecorator

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass

const val COMPONENT_DEC = "Component"
const val OPTIONS_DEC = "Options"

private val COMPONENT_DECS = setOf(COMPONENT_DEC, OPTIONS_DEC)

fun findComponentDecorator(clazz: JSClass): ES6Decorator? =
  clazz.attributeList
    ?.decorators
    ?.find { COMPONENT_DECS.contains(it.decoratorName) }

fun isComponentDecorator(decorator: ES6Decorator): Boolean =
  COMPONENT_DECS.contains(decorator.decoratorName)
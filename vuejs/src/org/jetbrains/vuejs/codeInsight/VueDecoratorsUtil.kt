// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner

fun findDecorator(member: JSRecordType.TypeMember, names: Set<String>): ES6Decorator? =
  (member.memberSource.singleElement as? JSAttributeListOwner)
    ?.attributeList
    ?.decorators
    ?.find { names.contains(it.decoratorName) }

fun getDecoratorArgument(decorator: ES6Decorator?, index: Int): JSExpression? =
  (decorator?.expression as? JSCallExpression)
  ?.arguments
  ?.getOrNull(index)

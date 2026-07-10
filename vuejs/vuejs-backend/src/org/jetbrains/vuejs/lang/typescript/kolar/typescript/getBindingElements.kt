// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.typescript

import com.intellij.lang.javascript.psi.JSDestructuringArray
import com.intellij.lang.javascript.psi.JSDestructuringContainer
import com.intellij.lang.javascript.psi.JSDestructuringObject
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.psi.PsiElement

fun JSDestructuringContainer.getBindingElements(): Sequence<BindingElement> =
  when (this) {
    is JSDestructuringObject -> getBindingElements(this)
    is JSDestructuringArray -> getBindingElements(this)
    else -> error("Unexpected destructuring container!")
  }

private fun getBindingElements(
  o: JSDestructuringObject,
): Sequence<BindingElement> = sequence {
  for (property in o.properties) {
    val nameIdentifier = (property as? JSNamedElement)?.nameIdentifier
                         ?: continue

    yield(
      BindingElement(
        source = property,
        nameIdentifier = nameIdentifier,
        isRest = property.isRest,
        initializer = property.destructuringElement?.initializer,
      )
    )
  }
}

private fun getBindingElements(
  array: JSDestructuringArray,
): Sequence<BindingElement> = sequence {
  for (element in array.elements) {
    val nameIdentifier = (element as? JSNamedElement)?.nameIdentifier
                         ?: continue

    yield(
      BindingElement(
        source = element,
        nameIdentifier = nameIdentifier,
        isRest = false,
        initializer = element.initializer,
      )
    )
  }

  val restElement = array.restElement
  if (restElement != null) {
    val nameIdentifier = restElement.variable?.nameIdentifier
    if (nameIdentifier != null) {
      yield(
        BindingElement(
          source = restElement,
          nameIdentifier = nameIdentifier,
          isRest = true,
          initializer = null,
        )
      )
    }
  }
}

class BindingElement(
  val source: PsiElement,
  val nameIdentifier: PsiElement,
  val isRest: Boolean,
  val initializer: JSExpression?,
)

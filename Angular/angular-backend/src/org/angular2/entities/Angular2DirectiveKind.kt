// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

enum class Angular2DirectiveKind {
  REGULAR,
  STRUCTURAL,
  BOTH;

  val isStructural: Boolean
    get() = this == STRUCTURAL || this == BOTH

  val isRegular: Boolean
    get() = this == REGULAR || this == BOTH

  companion object {

    operator fun Angular2DirectiveKind.plus(other: Angular2DirectiveKind): Angular2DirectiveKind =
      get(isRegular || other.isRegular, isStructural || other.isStructural)

    @JvmName("plusNullable")
    operator fun Angular2DirectiveKind?.plus(other: Angular2DirectiveKind?): Angular2DirectiveKind? =
      if (this == null)
        other
      else if (other == null)
        this
      else
        this + other

    fun get(isRegular: Boolean, isStructural: Boolean): Angular2DirectiveKind =
      when {
        isRegular && isStructural -> BOTH
        isStructural -> STRUCTURAL
        else -> REGULAR
      }

    fun get(
      hasElementRef: Boolean,
      hasTemplateRef: Boolean,
      hasViewContainerRef: Boolean,
      isOptionalTemplateRef: Boolean,
      hasTemplateGuard: Boolean,
    ): Angular2DirectiveKind? {
      return if (hasElementRef || hasTemplateRef || hasViewContainerRef)
        get(isOptionalTemplateRef || hasElementRef || (hasViewContainerRef && !hasTemplateRef),
            hasTemplateRef || hasViewContainerRef || hasTemplateGuard)
      else
        null
    }
  }
}

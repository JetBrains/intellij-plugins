// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.openapi.util.text.StringUtil

fun String.nullize(nullizeSpaces:Boolean = false): String? {
  return StringUtil.nullize(this, nullizeSpaces)
}

fun <T> Iterator<T>.firstOrNull(): T? {
  if (!hasNext())
    return null
  return next()
}

fun joinCommaOr(list: List<String>): String = when (list.size) {
  0 -> ""
  1 -> list.first()
  else -> (list.dropLast(1).joinToString(postfix = " or " + list.last()))
}
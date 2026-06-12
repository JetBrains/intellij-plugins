// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.shared

private val globallyAllowed = setOf(
  "Infinity",
  "undefined",
  "NaN",
  "isFinite",
  "isNaN",
  "parseFloat",
  "parseInt",
  "decodeURI",
  "decodeURIComponent",
  "encodeURI",
  "encodeURIComponent",
  "Math",
  "Number",
  "Date",
  "Array",
  "Object",
  "Boolean",
  "String",
  "RegExp",
  "Map",
  "Set",
  "JSON",
  "Intl",
  "BigInt",
  "console",
  "Error",
  "Symbol",
)

fun isGloballyAllowed(
  key: String,
): Boolean =
  key in globallyAllowed

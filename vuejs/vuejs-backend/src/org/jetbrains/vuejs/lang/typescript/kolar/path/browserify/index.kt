// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.path.browserify

import kotlin.io.path.Path
import kotlin.io.path.relativeTo

fun isAbsolute(path: String): Boolean =
  Path(path).isAbsolute

fun basename(path: String): String =
  Path(path).fileName.toString()

fun dirname(path: String): String =
  Path(path).parent.toString()

fun relative(from: String, to: String): String =
  Path(from).relativeTo(Path(to)).toString()

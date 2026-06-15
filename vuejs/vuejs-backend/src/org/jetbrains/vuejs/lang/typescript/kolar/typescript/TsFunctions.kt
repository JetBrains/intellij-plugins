// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.typescript

fun <T> forEachChild(
  node: Node,
  cbNode: (Node) -> T?,
): T? =
  TODO()

fun getLeadingCommentRanges(
  text: String,
  pos: Int,
): List<CommentRange>? =
  TODO()

fun getTokenPosOfNode(
  node: Node,
  sourceFile: SourceFile? = null,
): Int =
  TODO()

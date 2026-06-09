// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.forEachChild
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScriptBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures

const val newLine: String = "\n"
const val endOfLine: String = ";\n"
val identifierRegex: Regex = Regex("^[a-zA-Z_\$][0-9a-zA-Z_\$]*\$")

fun getTypeScriptAST(
  block: IRBlock,
  text: String,
): SourceFile =
  TODO()

fun generateSfcBlockSection(
  block: IRScriptBlock,
  start: Int,
  end: Int,
  features: VueCodeInformation,
): Sequence<Code> = sequence {
  val text = block.content.substring(start, end)
  yield(DataSegment(
    text = text,
    source = block.name,
    sourceOffset = start,
    data = features,
  ))

  // #3632
  val textEnd = text.trimEnd().length
  for (diag in block.ast.parseDiagnostics) {
    val diagStart = diag.start
    val diagEnd = diag.start + diag.length
    if (diagStart >= textEnd && diagEnd <= end) {
      yield(StringSegment(";"))
      yield(DataSegment(
        text = "",
        source = block.name,
        sourceOffset = end,
        data = codeFeatures.verification
      ))
      yield(StringSegment(newLine))
      break
    }
  }
}

fun forEachNode(
  node: Node,
): Sequence<Node> = sequence {
  val children = mutableListOf<Node>()
  forEachChild(node) { child ->
    children.add(child)
  }

  yieldAll(children)
}

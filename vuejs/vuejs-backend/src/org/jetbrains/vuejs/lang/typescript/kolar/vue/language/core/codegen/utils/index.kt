// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScriptBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation

const val newLine: String = "\n"
const val endOfLine: String = ";\n"
val identifierRE: Regex = Regex("^[a-zA-Z_\$][0-9a-zA-Z_\$]*\$")

fun getTypeScriptAST(
  text: String,
): FakeSourceFile =
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
}

fun forEachNode(
  node: PsiElement,
): Sequence<PsiElement> =
  node.children
    .asSequence()
    .filter { it !is LeafElement }
    .filter { it !is PsiWhiteSpace }
    .filter { it !is PsiComment }

// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.impl.JSChangeUtil.createJSContentFromText
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.childrenSequence
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.util.containers.sequenceOfNotNull
import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScriptBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

const val newLine: String = "\n"
const val endOfLine: String = ";\n"
val identifierRE: Regex = Regex("^[a-zA-Z_\$][0-9a-zA-Z_\$]*\$")

fun isTsLang(lang: String): Boolean =
  lang == "ts" || lang == "tsx"

// `{} as T` in TS; `/** @type {T} */ ({})` in JS. The JSDoc cast keeps the
// same assertion semantics under checkJs, and requires no type-only syntax.
fun asType(type: String, lang: String): String =
  if (isTsLang(lang)) "{} as $type" else "/** @type {$type} */ ({})"

// `let name!: T;` in TS; `var name = /** @type {T} */ ({});` in JS.
fun generateTypedVar(
  kind: String,
  name: String,
  lang: String,
  type: suspend SequenceScope<Code>.() -> Unit,
): Sequence<Code> = sequence {
  if (isTsLang(lang)) {
    yield("$kind $name!: ")
    type()
    yield(endOfLine)
  }
  else {
    yield("var $name = /** @type {")
    type()
    yield("} */ ({})$endOfLine")
  }
}

// `type name = T;` in TS; `/** @typedef {T} name */;` in JS.
fun generateTypeAlias(
  name: String,
  lang: String,
  type: suspend SequenceScope<Code>.() -> Unit,
): Sequence<Code> = sequence {
  if (isTsLang(lang)) {
    yield("type $name = ")
    type()
    yield(endOfLine)
  }
  else {
    yield("/** @typedef {")
    type()
    yield("} $name */$endOfLine")
  }
}

// The phantom argument that carries the component library's `Ref` brand into
// `__VLS_unwrap` / `__VLS_withDotValue`; the helper declarations cannot
// resolve the library import themselves.
fun getRefBrandArgument(vueCompilerOptions: VueCompilerOptions, lang: String): String =
  asType("import('${vueCompilerOptions.lib}').Ref<unknown>", lang)

fun getTypeScriptAST(
  text: String,
  project: Project,
): FakeSourceFile =
  createJSContentFromText(project, text, VueJSLanguage)

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
  node.childrenSequence
    .filter { it !is LeafElement }
    .filter { it !is PsiWhiteSpace }
    .filter { it !is PsiComment }
    .plus(nameIdentifiers(node))

private fun nameIdentifiers(
  node: PsiElement,
): Sequence<PsiElement> = when {
  node is JSParameter
    -> sequenceOfNotNull(node.nameIdentifier)

  else -> emptySequence()
}

fun typeScriptFileStart(
  node: PsiElement,
): Int =
  if (forEachNode(node).none())
    node.endOffset
  else
    node.startOffset

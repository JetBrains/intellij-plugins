// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.ObjectLiteralExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.StringLiteral
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isCallExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isExportAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectLiteralExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isStringLiteral
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getNodeText
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getStartEnd

data class ScriptExportDefaultOptions(
  val isObjectLiteral: Boolean,
  val expression: TextRange<*>,
  val args: TextRange<*>,
  val argsNode: ObjectLiteralExpression,
  val components: TextRange<*>?,
  val componentsNode: ObjectLiteralExpression?,
  val directives: TextRange<*>?,
  val name: TextRange<*>?,
  val nameNode: StringLiteral?,
  val inheritAttrs: String?,
)

data class ScriptExportDefault(
  val start: Int,
  val end: Int,
  val expression: TextRange<*>,
  val isObjectLiteral: Boolean,
  val options: ScriptExportDefaultOptions?,
)

data class ScriptRanges(
  val exportDefault: ScriptExportDefault?,
  val bindings: List<TextRange<*>>,
  val components: List<TextRange<*>>,
)

fun parseScriptRanges(
  ast: SourceFile,
  vueCompilerOptions: VueCompilerOptions,
): ScriptRanges {
  var exportDefault: ScriptExportDefault? = null

  for (child in forEachNode(ast)) {
    if (isExportAssignment(child)) {
      val expression = child.expression
      var start = getStartEnd(child).start
      val comment = getClosestMultiLineCommentRange(child, emptyList())
      if (comment != null) start = comment.start
      exportDefault = ScriptExportDefault(
        start = start,
        end = child.end,
        expression = getStartEnd(expression),
        isObjectLiteral = isObjectLiteralExpression(expression),
        options = parseOptionsFromExtression(expression),
      )
    }
  }

  val (bindings, components) = parseBindingRanges(ast, vueCompilerOptions.extensions)

  return ScriptRanges(
    exportDefault = exportDefault,
    bindings = bindings,
    components = components,
  )
}

fun parseOptionsFromExtression(
  expNode: Node,
): ScriptExportDefaultOptions? {
  val exp = getUnwrappedExpression(expNode)

  val obj: ObjectLiteralExpression
  if (isObjectLiteralExpression(exp)) {
    obj = exp
  }
  else if (isCallExpression(exp) && exp.arguments.isNotEmpty()) {
    val arg0 = exp.arguments[0]
    if (isObjectLiteralExpression(arg0)) obj = arg0
    else return null
  }
  else {
    return null
  }

  var componentsOptionNode: ObjectLiteralExpression? = null
  var directivesOptionNode: ObjectLiteralExpression? = null
  var nameOptionNode: StringLiteral? = null
  var inheritAttrsOption: String? = null

  for (node in forEachNode(obj)) {
    if (isPropertyAssignment(node) && isIdentifier(node.name)) {
      val name = getNodeText(node.name)
      val init = node.initializer
      when {
        name == "components" && isObjectLiteralExpression(init) -> componentsOptionNode = init
        name == "directives" && isObjectLiteralExpression(init) -> directivesOptionNode = init
        name == "name" && isStringLiteral(init) -> nameOptionNode = init
        name == "inheritAttrs" -> inheritAttrsOption = getNodeText(init)
      }
    }
  }

  return ScriptExportDefaultOptions(
    isObjectLiteral = isObjectLiteralExpression(exp),
    expression = getStartEnd(exp),
    args = getStartEnd(obj),
    argsNode = obj,
    components = componentsOptionNode?.let { getStartEnd(it) },
    componentsNode = componentsOptionNode,
    directives = directivesOptionNode?.let { getStartEnd(it) },
    name = nameOptionNode?.let { getStartEnd(it) },
    nameNode = nameOptionNode,
    inheritAttrs = inheritAttrsOption,
  )
}

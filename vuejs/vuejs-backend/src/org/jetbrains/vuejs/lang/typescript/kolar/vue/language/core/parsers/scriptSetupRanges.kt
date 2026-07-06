// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.HasModifiers
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SyntaxKind
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.getLeadingCommentRanges
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isCallExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isCallSignatureDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isEmptyStatement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isExportDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionLike
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isImportDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isImportEqualsDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isInterfaceDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectLiteralExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isStatement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isStringLiteral
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isStringLiteralLike
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeAliasDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeLiteralNode
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isUnionTypeNode
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isVariableDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingIdentifiers
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getNodeText
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getStartEnd
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.CallExpression as TsCallExpression

private val tsCheckRE = Regex("""^//\s*@ts-(?:no)?check($|\s)""")

// CallExpressionRange { callExp, exp, arg?, typeArg? }
// Base for DefineProps, DefineEmits, DefineSlots, UseTemplateRef; also used standalone
interface CallExpressionRange {
  val callExp: TextRange<*>
  val exp: TextRange<*>
  val arg: TextRange<*>?
  val typeArg: TextRange<*>?
}

// Concrete base implementation for standalone usages (withDefaults, defineExpose, useAttrs, useCssModule, useSlots)
data class CallExpression(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
) : CallExpressionRange

data class DefineModel(
  val arg: TextRange<*>?,
  val localName: TextRange<*>?,
  val name: TextRange<*>?,
  val type: TextRange<*>?,
  val modifierType: TextRange<*>?,
  val runtimeType: TextRange<*>?,
  val defaultValue: TextRange<*>?,
  val required: Boolean?,
  val comments: TextRange<*>?,
)

// DefineProps extends CallExpressionRange
data class DefineProps(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
  val name: String?,
  val destructured: Map<String, *>?,  // Map<string, ts.Expression | undefined>
  val destructuredRest: String?,
  val statement: TextRange<*>,
) : CallExpressionRange

// DefineEmits extends CallExpressionRange
data class DefineEmits(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
  val name: String?,
  val hasUnionTypeArg: Boolean?,
  val statement: TextRange<*>,
) : CallExpressionRange

// DefineSlots extends CallExpressionRange
data class DefineSlots(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
  val name: String?,
  val statement: TextRange<*>,
) : CallExpressionRange

data class DefineOptions(
  val name: String?,
  val inheritAttrs: String?,
)

// UseTemplateRef extends CallExpressionRange
data class UseTemplateRef(
  override val callExp: TextRange<*>,
  override val exp: TextRange<*>,
  override val arg: TextRange<*>?,
  override val typeArg: TextRange<*>?,
  val name: String?,
) : CallExpressionRange

// ScriptSetupRanges — structural equivalent of ReturnType<typeof parseScriptSetupRanges>
data class ScriptSetupRanges(
  val leadingCommentEndOffset: Int,
  val importSectionEndOffset: Int,
  val bindings: List<TextRange<*>>,
  val components: List<TextRange<*>>,
  val defineModel: List<DefineModel>,
  val defineProps: DefineProps?,
  val withDefaults: CallExpression?,
  val defineEmits: DefineEmits?,
  val defineSlots: DefineSlots?,
  val defineExpose: CallExpression?,
  val defineOptions: DefineOptions?,
  val useAttrs: List<CallExpression>,
  val useCssModule: List<CallExpression>,
  val useSlots: List<CallExpression>,
  val useTemplateRef: List<UseTemplateRef>,
)

fun parseScriptSetupRanges(
  ast: SourceFile,
  vueCompilerOptions: VueCompilerOptions,
): ScriptSetupRanges {
  val text = ast.text

  val defineModelList = mutableListOf<DefineModel>()
  var defineProps: DefineProps? = null
  var withDefaults: CallExpression? = null
  var defineEmits: DefineEmits? = null
  var defineSlots: DefineSlots? = null
  var defineExpose: CallExpression? = null
  var defineOptions: DefineOptions? = null
  val useAttrs = mutableListOf<CallExpression>()
  val useCssModule = mutableListOf<CallExpression>()
  val useSlots = mutableListOf<CallExpression>()
  val useTemplateRef = mutableListOf<UseTemplateRef>()

  fun parseCallExpr(
    node: TsCallExpression,
  ): CallExpression =
    CallExpression(
      callExp = getStartEnd(node),
      exp = getStartEnd(node.expression),
      arg = node.arguments.getOrNull(0)?.let { getStartEnd(it) },
      typeArg = node.typeArguments?.getOrNull(0)?.let { getStartEnd(it) },
    )

  fun parseCallExprAssignment(
    node: TsCallExpression,
    parent: Node,
  ): Pair<String?, CallExpression> {
    val name = if (isVariableDeclaration(parent) && isIdentifier(parent.name))
      getNodeText(parent.name)
    else null
    return Pair(name, parseCallExpr(node))
  }

  fun visitNode(
    node: Node,
    parents: MutableList<Node>,
  ) {
    val parent = parents.last()
    if (isCallExpression(node) && isIdentifier(node.expression)) {
      val callText = getNodeText(node.expression)
      when {
        callText in vueCompilerOptions.macros.defineModel -> {
          var localName: TextRange<*>? = null
          var propName: Node? = null
          var options: Node? = null
          var type: TextRange<*>? = null
          var modifierType: TextRange<*>? = null
          var runtimeType: TextRange<*>? = null
          var defaultValue: TextRange<*>? = null
          var required = false

          if (isVariableDeclaration(parent) && isIdentifier(parent.name)) {
            localName = getStartEnd(parent.name)
          }
          type = node.typeArguments?.getOrNull(0)?.let { getStartEnd(it) }
          modifierType = node.typeArguments?.getOrNull(1)?.let { getStartEnd(it) }

          when {
            node.arguments.size >= 2 -> {
              propName = node.arguments[0]
              options = node.arguments[1]
            }
            node.arguments.size >= 1 -> {
              if (isStringLiteralLike(node.arguments[0])) propName = node.arguments[0]
              else options = node.arguments[0]
            }
          }

          if (options != null && isObjectLiteralExpression(options)) {
            for (prop in options.properties) {
              if (isPropertyAssignment(prop) && isIdentifier(prop.name)) {
                when (getNodeText(prop.name)) {
                  "type" -> runtimeType = getStartEnd(prop.initializer)
                  "default" -> defaultValue = getStartEnd(prop.initializer)
                  "required" -> if (prop.initializer.kind == SyntaxKind.TrueKeyword) required = true
                }
              }
            }
          }

          val pn = propName
          val name = if (pn != null && isStringLiteralLike(pn))
            getStartEnd(pn)
          else null

          defineModelList.add(DefineModel(
            localName = localName,
            name = name,
            type = type,
            modifierType = modifierType,
            runtimeType = runtimeType,
            defaultValue = defaultValue,
            required = required,
            comments = getClosestMultiLineCommentRange(node, parents),
            arg = getStartEnd(node),
          ))
        }

        callText in vueCompilerOptions.macros.defineProps -> {
          val (name, ce) = parseCallExprAssignment(node, parent)
          var destructured: MutableMap<String, Any?>? = null
          var destructuredRest: String? = null
          var resolvedName = name

          if (isVariableDeclaration(parent) && isObjectBindingPattern(parent.name)) {
            destructured = mutableMapOf()
            for (id in collectBindingIdentifiers(parent.name)) {
              val idName = getNodeText(id.id)
              if (id.isRest) destructuredRest = idName
              else destructured[idName] = id.initializer
            }
          }
          else if (isCallExpression(parent) && isIdentifier(parent.expression)
                   && getNodeText(parent.expression) in vueCompilerOptions.macros.withDefaults) {
            val grand = parents.getOrNull(parents.lastIndex - 1)
            if (grand != null && isVariableDeclaration(grand) && isIdentifier(grand.name)) {
              resolvedName = getNodeText(grand.name)
            }
          }

          defineProps = DefineProps(
            callExp = ce.callExp, exp = ce.exp, arg = ce.arg, typeArg = ce.typeArg,
            name = resolvedName,
            destructured = destructured,
            destructuredRest = destructuredRest,
            statement = getStatementRange(parents, node),
          )
        }

        callText in vueCompilerOptions.macros.withDefaults -> {
          withDefaults = CallExpression(
            callExp = getStartEnd(node),
            exp = getStartEnd(node.expression),
            arg = node.arguments.getOrNull(1)?.let { getStartEnd(it) },
            typeArg = null,
          )
        }

        callText in vueCompilerOptions.macros.defineEmits -> {
          val (name, ce) = parseCallExprAssignment(node, parent)
          var hasUnionTypeArg: Boolean? = null
          val firstTypeArg = node.typeArguments?.getOrNull(0)
          if (firstTypeArg != null && isTypeLiteralNode(firstTypeArg)) {
            for (member in firstTypeArg.members) {
              if (isCallSignatureDeclaration(member)) {
                val paramType = member.parameters.getOrNull(0)?.type
                if (paramType != null && isUnionTypeNode(paramType)) {
                  hasUnionTypeArg = true
                  break
                }
              }
            }
          }
          defineEmits = DefineEmits(
            callExp = ce.callExp, exp = ce.exp, arg = ce.arg, typeArg = ce.typeArg,
            name = name,
            hasUnionTypeArg = hasUnionTypeArg,
            statement = getStatementRange(parents, node),
          )
        }

        callText in vueCompilerOptions.macros.defineSlots -> {
          val (name, ce) = parseCallExprAssignment(node, parent)
          defineSlots = DefineSlots(
            callExp = ce.callExp, exp = ce.exp, arg = ce.arg, typeArg = ce.typeArg,
            name = name,
            statement = getStatementRange(parents, node),
          )
        }

        callText in vueCompilerOptions.macros.defineExpose -> {
          defineExpose = parseCallExpr(node)
        }

        callText in vueCompilerOptions.macros.defineOptions && node.arguments.isNotEmpty() -> {
          val arg0 = node.arguments[0]
          if (isObjectLiteralExpression(arg0)) {
            var optionsName: String? = null
            var inheritAttrs: String? = null
            for (prop in arg0.properties) {
              if (isPropertyAssignment(prop) && isIdentifier(prop.name)) {
                val propInit = prop.initializer
                when (getNodeText(prop.name)) {
                  "inheritAttrs" -> inheritAttrs = getNodeText(propInit)
                  "name" -> if (isStringLiteral(propInit)) optionsName = propInit.text
                }
              }
            }
            defineOptions = DefineOptions(name = optionsName, inheritAttrs = inheritAttrs)
          }
        }

        callText in vueCompilerOptions.composables.useAttrs -> useAttrs.add(parseCallExpr(node))
        callText in vueCompilerOptions.composables.useCssModule -> useCssModule.add(parseCallExpr(node))
        callText in vueCompilerOptions.composables.useSlots -> useSlots.add(parseCallExpr(node))
        callText in vueCompilerOptions.composables.useTemplateRef && node.typeArguments.isNullOrEmpty() -> {
          val (name, ce) = parseCallExprAssignment(node, parent)
          useTemplateRef.add(UseTemplateRef(
            callExp = ce.callExp, exp = ce.exp, arg = ce.arg, typeArg = ce.typeArg,
            name = name,
          ))
        }
      }
    }

    if (!isFunctionLike(node)) {
      for (child in forEachNode(node)) {
        parents.add(node)
        visitNode(child, parents)
        parents.removeAt(parents.lastIndex)
      }
    }
  }

  val leadingCommentRanges = getLeadingCommentRanges(text, 0)?.reversed() ?: emptyList()
  val leadingCommentEndOffset = leadingCommentRanges
                                  .find { range -> tsCheckRE.containsMatchIn(text.substring(range.pos, range.end)) }
                                  ?.end ?: 0

  var foundNonImportExportNode = false
  var importSectionEndOffset = 0
  for (node in forEachNode(ast)) {
    if (foundNonImportExportNode
        || isImportDeclaration(node)
        || isExportDeclaration(node)
        || isEmptyStatement(node)
        || isImportEqualsDeclaration(node)) {
      continue
    }
    if (node is HasModifiers
        && (isTypeAliasDeclaration(node) || isInterfaceDeclaration(node))
        && node.modifiers?.any { it.kind == SyntaxKind.ExportKeyword } == true) {
      continue
    }
    val commentRanges = getLeadingCommentRanges(text, node.pos)
    importSectionEndOffset = if (!commentRanges.isNullOrEmpty()) {
      commentRanges.minBy { it.pos }.pos
    }
    else {
      getStartEnd(node).start
    }
    foundNonImportExportNode = true
  }

  for (node in forEachNode(ast)) {
    visitNode(node, mutableListOf(ast))
  }

  val (bindings, components) = parseBindingRanges(ast, vueCompilerOptions.extensions)

  return ScriptSetupRanges(
    leadingCommentEndOffset = leadingCommentEndOffset,
    importSectionEndOffset = importSectionEndOffset,
    bindings = bindings,
    components = components,
    defineModel = defineModelList,
    defineProps = defineProps,
    withDefaults = withDefaults,
    defineEmits = defineEmits,
    defineSlots = defineSlots,
    defineExpose = defineExpose,
    defineOptions = defineOptions,
    useAttrs = useAttrs,
    useCssModule = useCssModule,
    useSlots = useSlots,
    useTemplateRef = useTemplateRef,
  )
}

private fun getStatementRange(
  parents: List<Node>,
  node: Node,
): TextRange<*> {
  var statementRange: TextRange<*>? = null
  for (i in parents.indices.reversed()) {
    val statement = parents[i]
    if (isStatement(statement)) {
      for (child in forEachNode(statement)) {
        val range = getStartEnd(child)
        statementRange = statementRange?.copy(end = range.end) ?: range
      }
      break
    }
  }

  return statementRange
         ?: getStartEnd(node)
}

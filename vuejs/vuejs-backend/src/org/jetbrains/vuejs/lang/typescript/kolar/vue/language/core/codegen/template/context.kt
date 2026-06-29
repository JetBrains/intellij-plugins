// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.typescript.kolar.KolarCodeInformation.VerificationInfo
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.CommentNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.NodeTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.InlayHintInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext.CurrentInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext.DynamicSlot
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext.ExpectError
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext.Generic
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext.Slot
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenContext.TemplateRef
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.startBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

private val commentDirectiveRegex: Regex =
  Regex("""^<!--\s*@vue-(?<name>[-\w]+)\b(?<content>[\s\S]*)-->${'$'}""")

interface TemplateCodegenContext {
  val generatedTypes: MutableSet<String>
  val currentInfo: CurrentInfo
  fun resolveCodeFeatures(features: VueCodeInformation): VueCodeInformation
  var inVFor: Boolean
  val slots: MutableList<Slot>
  val dynamicSlots: MutableList<DynamicSlot>
  val dollarVars: MutableSet<String>
  val componentAccessMap: MutableMap<String, MutableMap<String, MutableSet<Int>>>
  val blockConditions: MutableList<String>
  val inlayHints: MutableList<InlayHintInfo>
  val inheritedAttrVars: MutableSet<String>
  val templateRefs: MutableMap<String, MutableList<TemplateRef>>
  val singleRootElTypes: MutableSet<String>
  val singleRootNodes: MutableSet<ElementNode?>
  val scopes: MutableList<MutableSet<String>>
  val components: MutableList<() -> String>
  fun addTemplateRef(name: String, typeExp: String, offset: Int)
  fun recordComponentAccess(source: String, name: String, offset: Int? = null)
  fun declare(varNames: List<String>)
  fun declare(varName: String) {
    declare(listOf(varName))
  }

  fun startScope(): () -> Sequence<Code>
  fun getInternalVariable(): String
  fun getHoistVariable(originalVar: String): String
  fun generateHoistVariables(): Sequence<Code>
  fun generateConditionGuards(): Sequence<Code>
  fun enter(node: Node): Boolean
  fun exit(): Sequence<Code>

  data class Slot(
    val name: String,
    val offset: Int?,
    val tagRange: Pair<Int, Int>,
    val nodeLoc: SourceLocation,
    val propsVar: String,
  )

  data class DynamicSlot(
    val expVar: String,
    val propsVar: String,
  )

  data class TemplateRef(
    val typeExp: String,
    val offset: Int,
  )

  class ExpectError(
    val node: CommentNode,
    var token: Int = 0,
  )

  data class Generic(
    val content: String,
    val offset: Int,
  )

  data class CurrentInfo(
    val ignoreError: Boolean = false,
    val expectError: ExpectError? = null,
    val generic: Generic? = null,
  )
}

fun createTemplateCodegenContext(): TemplateCodegenContext = object : TemplateCodegenContext {
  private var variableId = 0
  private val hoistVars = mutableMapOf<String, String>()
  private val stack = mutableListOf<CurrentInfo>()
  private val commentBuffer = mutableListOf<CommentNode>()

  override val generatedTypes = mutableSetOf<String>()
  override val currentInfo get() = stack.last()
  override var inVFor = false
  override val slots = mutableListOf<Slot>()
  override val dynamicSlots = mutableListOf<DynamicSlot>()
  override val dollarVars = mutableSetOf<String>()
  override val componentAccessMap = mutableMapOf<String, MutableMap<String, MutableSet<Int>>>()
  override val blockConditions = mutableListOf<String>()
  override val inlayHints = mutableListOf<InlayHintInfo>()
  override val inheritedAttrVars = mutableSetOf<String>()
  override val templateRefs = mutableMapOf<String, MutableList<TemplateRef>>()
  override val singleRootElTypes = mutableSetOf<String>()
  override val singleRootNodes = mutableSetOf<ElementNode?>()
  override val scopes = mutableListOf<MutableSet<String>>()
  override val components = mutableListOf<() -> String>()

  override fun addTemplateRef(
    name: String,
    typeExp: String,
    offset: Int,
  ) {
    templateRefs
      .getOrPut(name) { mutableListOf() }
      .add(TemplateRef(typeExp, offset))
  }

  override fun recordComponentAccess(
    source: String,
    name: String,
    offset: Int?,
  ) {
    val map = componentAccessMap.getOrPut(name) { mutableMapOf() }
    val arr = map.getOrPut(source) { mutableSetOf() }
    if (offset != null) {
      arr.add(offset)
    }
  }

  override fun declare(
    varNames: List<String>,
  ) {
    val scope = scopes.last()
    for (varName in varNames) {
      scope.add(varName)
    }
  }

  override fun startScope(): () -> Sequence<Code> {
    val scope = mutableSetOf<String>()
    scopes.add(scope)
    return {
      scopes.removeLast()
      generateAutoImport()
    }
  }

  override fun getInternalVariable(): String =
    "__VLS_${variableId++}"

  override fun getHoistVariable(
    originalVar: String,
  ): String =
    hoistVars.getOrPut(originalVar) { "__VLS_${variableId++}" }

  override fun generateHoistVariables(): Sequence<Code> = sequence {
    // trick to avoid TS 4081 (#5186)
    if (hoistVars.isNotEmpty()) {
      yield("// @ts-ignore$newLine")
      yield("var ")
      for ((originalVar, hoistVar) in hoistVars) {
        yield("$hoistVar = $originalVar, ")
      }
      yield(endOfLine)
    }
  }

  override fun generateConditionGuards(): Sequence<Code> = sequence {
    for (condition in blockConditions) {
      yield("if (!$condition) throw 0$endOfLine")
    }
  }

  override fun enter(node: Node): Boolean {
    if (node.type == NodeTypes.COMMENT) {
      commentBuffer.add(node as CommentNode)
      return false
    }

    var ignoreError = false
    var expectError: ExpectError? = null
    var generic: Generic? = null

    val comments = commentBuffer.toList()
    commentBuffer.clear()

    for (comment in comments) {
      val match = commentDirectiveRegex.find(comment.loc.source) ?: continue
      val name = match.groups["name"]?.value ?: continue
      val content = match.groups["content"]?.value
      when (name) {
        "skip" -> return false
        "ignore" -> ignoreError = true
        "expect-error" -> expectError = ExpectError(node = comment)
        "generic" -> {
          val text = content?.trim() ?: ""
          if (text.startsWith("{") && text.endsWith("}")) {
            generic = Generic(
              content = text.substring(1, text.length - 1),
              offset = comment.loc.start.offset + comment.loc.source.indexOf('{') + 1,
            )
          }
        }
      }
    }

    stack.add(
      CurrentInfo(
        ignoreError = ignoreError,
        expectError = expectError,
        generic = generic,
      ),
    )
    return true
  }

  override fun exit(): Sequence<Code> = sequence {
    val data = stack.removeLast()
    commentBuffer.clear()
    val expectError = data.expectError
    if (expectError != null) {
      val token = yield(startBoundary(
        source = "template",
        startOffset = expectError.node.loc.start.offset,
        features = VueCodeInformation(
          verification = VerificationInfo.WithFilter(
            // If no errors/warnings/diagnostics were reported within the region of code covered
            // by the @vue-expect-error directive, then we should allow any `unused @ts-expect-error`
            // diagnostics to be reported upward.
            shouldReport = { _, _ -> expectError.token == 0 },
          ),
        ),
      ))
      yield("// @ts-expect-error")
      yield(endBoundary(token, expectError.node.loc.end.offset))
      yield("$newLine$endOfLine")
    }
  }

  override fun resolveCodeFeatures(
    features: VueCodeInformation,
  ): VueCodeInformation {
    if (features.verification != null && stack.isNotEmpty()) {
      val data = stack.last()
      if (data.ignoreError) {
        // We are currently in a region of code covered by a @vue-ignore directive, so don't
        // even bother performing any type-checking: set verification to false.
        return features.copy(verification = null)
      }
      val expectError = data.expectError
      if (expectError != null) {
        // We are currently in a region of code covered by a @vue-expect-error directive. We need to
        // keep track of the number of errors encountered within this region so that we can know whether
        // we will need to propagate an "unused ts-expect-error" diagnostic back to the original
        // .vue file or not.
        return features.copy(
          verification = VerificationInfo.WithFilter(
            shouldReport = { source, code ->
              val featureVerification = features.verification
              if (
                featureVerification !is VerificationInfo.WithFilter
                || featureVerification.shouldReport(source, code)
              ) {
                expectError.token++
              }
              false
            },
          ),
        )
      }
    }
    return features
  }

  private fun generateAutoImport(): Sequence<Code> = sequence {
    val all = componentAccessMap.entries.toList()
    if (!all.any { (_, map) -> map.isNotEmpty() }) return@sequence
    yield("// @ts-ignore$newLine") // #2304
    yield("[")
    for ((varName, map) in all) {
      for ((source, offsets) in map) {
        for (offset in offsets) {
          yield(DataSegment(varName, source, offset, codeFeatures.importCompletionOnly))
          yield(",")
        }
        offsets.clear()
      }
    }
    yield("]$endOfLine")
  }
}

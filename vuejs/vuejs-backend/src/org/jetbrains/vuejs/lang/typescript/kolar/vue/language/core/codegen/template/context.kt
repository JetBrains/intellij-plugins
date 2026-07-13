// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.typescript.kolar.KolarCodeInformation.VerificationInfo
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.CommentNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.InlayHintInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

private val commentDirectiveRE: Regex =
  Regex("""^<!--\s*@vue-(?<name>[-\w]+)\b(?<content>[\s\S]*)-->${'$'}""")

class TemplateCodegenContext {
  private var variableId = 0
  private val hoistVars = mutableMapOf<String, String>()
  private val stack = mutableListOf<CurrentInfo>()
  private val commentBuffer = mutableListOf<CommentNode>()

  val generatedTypes: MutableSet<String> = mutableSetOf()
  fun getCommentInfo(): CurrentInfo = stack.last()
  var inVFor: Boolean = false
  val slots: MutableList<Slot> = mutableListOf<Slot>()
  val dynamicSlots: MutableList<DynamicSlot> = mutableListOf<DynamicSlot>()
  val dollarVars: MutableSet<String> = mutableSetOf()
  val contextAccesses: MutableMap<String, MutableMap<Source, MutableSet<Int>>> = mutableMapOf()
  val conditions: MutableList<String> = mutableListOf()
  val inlayHints: MutableList<InlayHintInfo> = mutableListOf()
  val inheritedAttrVars: MutableSet<String> = mutableSetOf()
  val templateRefs: MutableMap<String, MutableList<TemplateRef>> = mutableMapOf<String, MutableList<TemplateRef>>()
  val singleRootElTypes: MutableSet<String> = mutableSetOf()
  val singleRootNodes: MutableSet<ElementNode?> = mutableSetOf()
  val scopes: MutableList<Scope> = mutableListOf<Scope>()
  val components: MutableList<() -> String> = mutableListOf()

  fun addTemplateRef(
    name: String,
    typeExp: String,
    offset: Int,
  ) {
    templateRefs
      .getOrPut(name) { mutableListOf() }
      .add(TemplateRef(typeExp, offset))
  }

  fun accessVariable(
    source: Source,
    name: String,
    offset: Int? = null,
  ) {
    val map = contextAccesses.getOrPut(name) { mutableMapOf() }
    val arr = map.getOrPut(source) { mutableSetOf() }
    if (offset != null) {
      arr.add(offset)
    }
  }

  inner class Scope {
    private val vars: MutableSet<String> = mutableSetOf()

    fun declare(variable: String) {
      vars.add(variable)
    }

    fun declare(variables: List<String>) {
      vars.addAll(variables)
    }

    fun end(): Sequence<Code> {
      scopes.removeLast()
      return generateAutoImport()
    }

    operator fun contains(element: String): Boolean =
      element in vars
  }

  fun scope(): Scope {
    val s = Scope()
    scopes.add(s)
    return s
  }

  fun getInternalVariable(): String =
    "__VLS_${variableId++}"

  fun getHoistVariable(
    originalVar: String,
  ): String =
    hoistVars.getOrPut(originalVar) { "__VLS_${variableId++}" }

  fun generateHoistVariables(): Sequence<Code> = sequence {
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

  fun generateConditionGuards(): Sequence<Code> = sequence {
    for (condition in conditions) {
      yield("if (!$condition) throw 0$endOfLine")
    }
  }

  fun enter(node: Node): Boolean {
    if (node is CommentNode) {
      commentBuffer.add(node)
      return false
    }

    var ignoreError = false
    var expectError: ExpectError? = null
    var generic: Generic? = null

    val comments = commentBuffer.toList()
    commentBuffer.clear()

    for (comment in comments) {
      val match = commentDirectiveRE.find(comment.loc.source) ?: continue
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
              offset = comment.loc.startOffset + comment.loc.source.indexOf('{') + 1,
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

  fun exit(): Sequence<Code> = sequence {
    val data = stack.removeLast()
    commentBuffer.clear()
    val expectError = data.expectError
    if (expectError != null) {
      val boundary = yield(Boundary.start(
        source = Source("template"),
        startOffset = expectError.node.loc.startOffset,
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
      yield(boundary.end(expectError.node.loc.endOffset))
      yield("$newLine$endOfLine")
    }
  }

  fun resolveCodeFeatures(
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
    val all = contextAccesses.entries.toList()
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

  data class Slot(
    val name: String,
    val offset: Int?,
    val tagRange: Pair<Int, Int>,
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

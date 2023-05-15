package org.jetbrains.webstorm.lang

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext

class WebAssemblyKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters,
                                context: ProcessingContext,
                                result: CompletionResultSet) {
        when (TokenType.ERROR_ELEMENT) {
            parameters.position.parent.elementType -> {
                parameters.position.parent as PsiErrorElement
            }
            parameters.position.prevSibling.elementType -> {
                parameters.position.prevSibling as PsiErrorElement
            }
            parameters.position.prevSibling.prevSibling.elementType -> {
                if (parameters.position.prevSibling.elementType == TokenType.WHITE_SPACE) {
                    parameters.position.prevSibling.prevSibling as PsiErrorElement
                } else {
                    return
                }
            }
            else -> {
                return
            }
        }.errorDescription
                .split("\\s".toRegex())
                .map {
                    when {
                        it.matches("WebAssemblyToken.*KEY,?$".toRegex()) -> {
                            result.addElement(
                                    LookupElementBuilder.create(it
                                            .removeSuffix(",")
                                            .removeSurrounding("WebAssemblyToken.", "KEY")
                                            .toLowerCase()
                                    )
                            )
                        }

                        it == "WebAssemblyToken.LPAR" -> {
                            result.addElement(LookupElementBuilder.create("()"))
                        }

                        it.matches("<.*>,?$".toRegex()) -> {
                            when (it.removeSuffix(",")) {
                                "<valtype>" -> {
                                    result.addAllElements(numtype.map { x -> LookupElementBuilder.create(x) })
                                    result.addAllElements(reftype.map { x -> LookupElementBuilder.create(x) })
                                }

                                "<globaltype>" -> {
                                    result.addAllElements(numtype.map { x -> LookupElementBuilder.create(x) })
                                    result.addAllElements(reftype.map { x -> LookupElementBuilder.create(x) })
                                    result.addElement(LookupElementBuilder.create("(mut )"))
                                }

                                "<plaininstr>" -> {
                                    result.addAllElements(plaininstr.map { x -> LookupElementBuilder.create(x) })
                                }
                            }
                        }

                        it.matches("WebAssemblyToken.*,?$".toRegex()) -> {
                            when (it.removeSuffix(",")) {
                                "WebAssemblyToken.REFTYPE" -> {
                                    result.addAllElements(reftype.map { x -> LookupElementBuilder.create(x) })
                                }
                            }
                        }
                    }
                }
    }

    private val numtype: List<String> = listOf(
            "i32",
            "i64",
            "f32",
            "f64")

    private val reftype: List<String> = listOf(
            "funcref",
            "externref")

    private val plaininstr: List<String> = listOf(
            "unreachable",
            "nop",
            "return",
            "br",
            "br_if",
            "return",
            "call",
            "br_table",
            "call_indirect",

            "ref.is_null",
            "ref.null",
            "ref.func",

            "drop",
            "select",

            "local.get",
            "local.set",
            "local.tee",
            "global.get",
            "global.set",

            "table.get",
            "table.set",
            "table.size",
            "table.grow",
            "table.fill",
            "table.copy",
            "table.init",
            "elem.drop",

            "i32.load",
            "i64.load",
            "f32.load",
            "f64.load",
            "i32.load8_s",
            "i32.load8_u",
            "i32.load16_s",
            "i32.load16_u",
            "i64.load8_s",
            "i64.load8_u",
            "i64.load16_s",
            "i64.load16_u",
            "i64.load32_s",
            "i64.load32_u",
            "i32.store",
            "i64.store",
            "f32.store",
            "f64.store",
            "i32.store8",
            "i32.store16",
            "i64.store8",
            "i64.store16",
            "i64.store32",
            "memory.size",
            "memory.grow",
            "memory.fill",
            "memory.copy",
            "memory.init",
            "data.drop",

            "i32.const",
            "i64.const",
            "f32.const",
            "f64.const",

            "i32.clz",
            "i32.ctz",
            "i32.popcnt",
            "i32.add",
            "i32.sub",
            "i32.mul",
            "i32.div_s",
            "i32.div_u",
            "i32.rem_s",
            "i32.rem_u",
            "i32.and",
            "i32.or",
            "i32.xor",
            "i32.shl",
            "i32.shr_s",
            "i32.shr_u",
            "i32.rotl",
            "i32.rotr",

            "i64.clz",
            "i64.ctz",
            "i64.popcnt",
            "i64.add",
            "i64.sub",
            "i64.mul",
            "i64.div_s",
            "i64.div_u",
            "i64.rem_s",
            "i64.rem_u",
            "i64.and",
            "i64.or",
            "i64.xor",
            "i64.shl",
            "i64.shr_s",
            "i64.shr_u",
            "i64.rotl",
            "i64.rotr",

            "f32.abs",
            "f32.neg",
            "f32.ceil",
            "f32.floor",
            "f32.trunc",
            "f32.nearest",
            "f32.sqrt",
            "f32.add",
            "f32.sub",
            "f32.mul",
            "f32.div",
            "f32.min",
            "f32.max",
            "f32.copysign",

            "f64.abs",
            "f64.neg",
            "f64.ceil",
            "f64.floor",
            "f64.trunc",
            "f64.nearest",
            "f64.sqrt",
            "f64.add",
            "f64.sub",
            "f64.mul",
            "f642.div",
            "f64.min",
            "f64.max",
            "f64.copysign",

            "i32.eqz",
            "i32.eq",
            "i32.ne",
            "i32.lt_s",
            "i32.lt_u",
            "i32.gt_s",
            "i32.gt_u",
            "i32.le_s",
            "i32.le_u",
            "i32.ge_s",
            "i32.ge_u",

            "i64.eqz",
            "i64.eq",
            "i64.ne",
            "i64.lt_s",
            "i64.lt_u",
            "i64.gt_s",
            "i64.gt_u",
            "i64.le_s",
            "i64.le_u",
            "i64.ge_s",
            "i64.ge_u",

            "f32.eq",
            "f32.ne",
            "f32.lt",
            "f32.gt",
            "f32.le",
            "f32.ge",

            "f64.eq",
            "f64.ne",
            "f64.lt",
            "f64.gt",
            "f64.le",
            "f64.ge",

            "i32.wrap_i64",
            "i32.trunc_f32_s",
            "i32.trunc_f32_u",
            "i32.trunc_f64_s",
            "i32.trunc_f64_u",
            "i32.trunc_sat32_s",
            "i32.trunc_sat32_u",
            "i32.trunc_sat64_s",
            "i32.trunc_sat64_u",
            "i64.extend_i32_s",
            "i64.extend_i32_u",
            "i64.trunc_f32_s",
            "i64.trunc_f32_u",
            "i64.trunc_f64_s",
            "i64.trunc_f64_u",
            "i64.trunc_sat32_s",
            "i64.trunc_sat32_u",
            "i64.trunc_sat64_s",
            "i64.trunc_sat64_u",
            "f32.convert_i32_s",
            "f32.convert_i32_u",
            "f32.convert_i64_s",
            "f32.convert_i64_u",
            "f32.demote_f64",
            "f64.convert_i32_s",
            "f64.convert_i32_u",
            "f64.convert_i64_s",
            "f64.convert_i64_u",
            "f64.promote_f32",
            "i32.reinterpret_f32",
            "i64.reinterpret_f64",
            "f32.reinterpret_i32",
            "f64.reinterpret_i64",

            "i32.extend8_s",
            "i32.extend16_s",
            "i64.extend8_s",
            "i64.extend16_s",
            "i64.extend32_s")
}
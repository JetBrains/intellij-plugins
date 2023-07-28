package com.intellij.dts.parsing

import com.intellij.lang.impl.PsiBuilderDiagnostics
import com.intellij.lang.impl.PsiBuilderDiagnosticsImpl

private class BuilderDiagnostics : PsiBuilderDiagnostics {
    val rollbacks = mutableMapOf<String, MutableMap<Int, Int>>()
    var emptyRollbacks = 0

    val rollbackCount: Int
        get() = rollbacks.values.sumOf { it.values.sum() }

    override fun registerPass(charLength: Int, tokensLength: Int) {}

    override fun registerRollback(tokens: Int) {
        if (tokens == 0) {
            emptyRollbacks++
            return
        }

        val method = Thread.currentThread().stackTrace.dropWhile {
            it.className != "com.intellij.dts.lang.parser.DtsParser"
        }.first().methodName

        val map = rollbacks.getOrPut(method) { mutableMapOf() }
        val count = map.getOrDefault(tokens, 0)

        map[tokens] = count + 1
    }

    fun assertRollbacks(message: String, maxRollbacks: Int) {
        val rollbackCount = this.rollbackCount

        assert(rollbackCount <= maxRollbacks) {
            println("maximum rollbacks exceeded: $maxRollbacks - $message")

            println(" total rollbacks: ${rollbackCount + emptyRollbacks}")
            println(" empty rollbacks: $emptyRollbacks")
            println("actual rollbacks: $rollbackCount")

            println("most frequent rollbacks:")
            for ((method, map) in rollbacks.entries.sortedByDescending { it.value.values.sum() }.take(20)) {
                val rollbacks = map.values.sum()

                val avg = map.entries.fold(0.0) { acc, (tokens, count) ->
                    acc + tokens.toDouble() * (count.toDouble()  / rollbacks.toDouble())
                }

                val max = map.keys.max()
                val min = map.keys.min()

                println("%6d - avg. %2.2f - max: %2d min: %2d: %s".format(rollbacks, avg, max, min, method))
            }
        }
    }
}

class DtsRollbackParsingTest : DtsParsingTestBase("", "dtsi") {
    private fun createNesting(depth: Int, text: String): String {
        val builder = StringBuilder()

        for (i in 0 until depth) {
            builder.append("node$i {\n")
        }

        builder.append("$text\n")

        for (i in 0 until depth) {
            builder.append("};\n")
        }

        return builder.toString()
    }

    private fun parseFileWithDiagnostics(text: String): BuilderDiagnostics {
        val diagnostics = BuilderDiagnostics()
        PsiBuilderDiagnosticsImpl.runWithDiagnostics(diagnostics) {
            val root = createFile("file.dtsi", text)
            ensureParsed(root)
        }

        return diagnostics
    }

    private fun doRollbackTest(maxRollbacks: Int, text: String) {
        parseFileWithDiagnostics(text).assertRollbacks("input: $text", maxRollbacks)
    }

    private fun doRollbackWithRecoveryTest(maxRollbacks: Int, text: String) {
        val include = "/include/ \"file\""

        doRollbackTest(maxRollbacks, text)
        doRollbackTest(maxRollbacks, "$include\n$text")
        doRollbackTest(maxRollbacks + 2, "$text\n$include")

        val nestedRollbacks = parseFileWithDiagnostics(createNesting(20, text)).rollbackCount
        doRollbackTest(nestedRollbacks, createNesting(20, "$include\n$text"))
        doRollbackTest(nestedRollbacks + 2, createNesting(20, "$text\n$include"))
    }

    fun testProperty() = doRollbackTest(2, "prop = <>;")

    fun testSubNode() = doRollbackTest(3, "node {};")

    fun testRootNode() = doRollbackTest(4, "/ {};")

    fun testPropertyRecovery() = doRollbackWithRecoveryTest(1, "prop = <>")

    fun testSubNodeRecovery() = doRollbackWithRecoveryTest(2, "node {}")
}
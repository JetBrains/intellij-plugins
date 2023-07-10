package com.intellij.dts.parsing

import com.intellij.lang.impl.PsiBuilderDiagnostics
import com.intellij.lang.impl.PsiBuilderDiagnosticsImpl

private class BuilderDiagnostics : PsiBuilderDiagnostics {
    val rollbacks = mutableMapOf<String, Int>()

    val rollbackCount: Int
        get() = rollbacks.values.sum()

    override fun registerPass(charLength: Int, tokensLength: Int) {}

    override fun registerRollback(tokens: Int) {
        val method = Thread.currentThread().stackTrace.dropWhile {
            it.className != "com.intellij.dts.lang.parser.DtsParser"
        }.first().methodName

        val value = rollbacks.getOrDefault(method, 0)
        rollbacks[method] = value + 1
    }

    fun assertRollbacks(message: String, maxRollbacks: Int) {
        assert(rollbackCount <= maxRollbacks) {
            println("maximum rollbacks: $maxRollbacks, actual rollbacks: $rollbackCount")
            println(message)

            println("most frequent rollbacks:")
            for ((method, count) in rollbacks.entries.sortedByDescending { it.value }.take(20)) {
                println("%4d: %s".format(count, method))
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

    fun testRootNode() = doRollbackTest(18, "/ {};")

    fun testSubNode() = doRollbackTest(19, "node {};")

    fun testProperty() = doRollbackTest(36, "prop = <>;")

    fun testPropertyRecovery() = doRollbackWithRecoveryTest(36, "prop = <>")

    fun testSubNodeRecovery() = doRollbackWithRecoveryTest(18, "node {}")

    fun testLinearRollbackGrowth() {
        val rollbacks = parseFileWithDiagnostics(createNesting(1, "")).rollbackCount

        for (i in 2 until 20) {
            val multiple = parseFileWithDiagnostics(createNesting(i, "")).rollbackCount / rollbacks.toDouble()
            assertTrue(multiple < i)
        }
    }
}
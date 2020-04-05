import com.intellij.testFramework.fixtures.*

class MakefileCompletionTest : BasePlatformTestCase() {
  fun testSimple() = doTest("b", "c", "d", testFilename)
  fun testTargets() = doTest("a", testFilename)
  fun testAny() = doTest("include", "private", "define", "ifeq", "vpath", "ifndef", "ifneq", "override", "export", "undefine", "ifdef")
  fun testFunctions() = doTest(*functions)
  fun testVariables() = doTest("a", "b", "c", *functions)
  fun testCurly() = doTest("a", "b", "c")

  fun doTest(vararg variants: String) = myFixture.testCompletionVariants("$basePath/$testFilename", *variants)

  private val testFilename get() = "${getTestName(true)}.mk"
  override fun getTestDataPath() = "testData"
  override fun getBasePath() = "completion"

  private val functions = arrayOf("addsuffix", "realpath", "firstword", "origin", "dir", "error", "suffix", "wildcard", "findstring", "foreach", "file", "strip", "wordlist", "addprefix", "and", "guile", "abspath", "warning", "join", "if", "notdir", "value", "info", "or", "patsubst", "filter-out", "words", "subst", "sort", "call", "filter", "flavor", "basename", "eval", "lastword", "shell", "word")
}
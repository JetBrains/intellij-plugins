package org.intellij.plugin.mdx

import com.intellij.openapi.util.io.FileUtil.loadFile
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.impl.DebugUtil
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Ad-hoc PSI dumper for the `mdx-oracle-diff` agent skill - this is NOT a regression test. It is
 * intentionally excluded from [MdxTestSuite] and self-skips unless the input property is set, so a
 * normal test run just reports it as skipped.
 *
 * Given an input `.mdx` and an output directory (passed through `tests.cmd` as
 * `-Dpass.mdx.diff.in=<abs .mdx>` and `-Dpass.mdx.diff.out=<dir>`), it parses the file with the
 * real MDX languages on the shared test application and writes one `<Name>.<LangId>.txt` PSI dump
 * per view-provider root (`<Name>.MDX.txt`, `<Name>.MdxJS.txt`). The dump uses the same
 * whitespace + ranges format as the [MdxParsingTest] golden snapshots, so the skill can diff an
 * arbitrary file's live parse against the mdast oracle exactly like a committed fixture.
 */
class MdxOracleDiffDumpTest : MdxTestBase() {
  @Test
  fun dumpPsiRoots() {
    val inPath = System.getProperty("mdx.diff.in")
    Assumptions.assumeTrue(inPath != null, "mdx.diff.in not set; skipping ad-hoc PSI dump")
    val outDir = System.getProperty("mdx.diff.out") ?: error("mdx.diff.out must be set together with mdx.diff.in")

    val inFile = File(inPath!!)
    val name = inFile.name.removeSuffix(".mdx")
    val text = loadFile(inFile, CharsetToolkit.UTF8, true).trim()
    myFixture.configureByText("$name.mdx", text)

    val outDirFile = File(outDir).also { it.mkdirs() }
    for (root in myFixture.file.viewProvider.allFiles) {
      // Match the MdxParsingTest golden format exactly (trimmed dump, no trailing newline).
      val dump = DebugUtil.psiToString(root, true, true).trim()
      File(outDirFile, "$name.${root.language.id}.txt").writeText(dump)
    }
  }
}

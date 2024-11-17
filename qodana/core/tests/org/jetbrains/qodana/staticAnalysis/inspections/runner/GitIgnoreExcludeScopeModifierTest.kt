package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.project.modules
import com.intellij.psi.search.scope.packageSet.FilePatternPackageSet
import git4idea.repo.GitRepositoryManager
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.junit.Test
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class GitIgnoreExcludeScopeModifierTest : QodanaRunnerWithVcsTestCase() {

  // All tests in this class share the same test data.
  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  @Test
  fun runQodanaWithoutGitIgnore() {
    runAnalysis()

    assertSarifSummary(
      "test-module/dir.java:7:5: Result of 'Dir.unusedResult()' is ignored",
      "test-module/dir/A.java:7:5: Result of 'A.unusedMethod()' is ignored",
      "test-module/dir/A.java:8:5: Result of 'A.unusedMethod()' is ignored",
      "test-module/dir/N.java:7:5: Result of 'N.unusedMethod()' is ignored",
      "test-module/dir/N.java:8:5: Result of 'N.unusedMethod()' is ignored",
    )
  }

  @Test
  fun runQodanaWithGitIgnore() {
    createGitIgnoreFile("/test-module/dir\n")

    val dir = project.modules.first().guessModuleDir()!!.toNioPath().resolve("dir")
    assertTrue(fileIsIgnored(dir))

    ensureIgnoredFilesLoaded()

    runAnalysis()

    assertSarifSummary(
      /* .gitignore '/test-module/dir' matches '/test-module/dir/..' but not '/test-module/dir.java' */
      "test-module/dir.java:7:5: Result of 'Dir.unusedResult()' is ignored"
    )
  }

  @Test
  fun runQodanaWithGitIgnorePart() {
    createGitIgnoreFile("/test-module/dir/A.java\n")

    val srcDir = project.modules.single().guessModuleDir()!!.toNioPath().resolve("dir")
    assertTrue(fileIsIgnored(srcDir.resolve("A.java")))
    assertFalse(fileIsIgnored(srcDir.resolve("N.java")))

    ensureIgnoredFilesLoaded()

    runAnalysis()

    assertSarifSummary(
      /* No problems in A.java, since it is listed in .gitignore. */
      "test-module/dir.java:7:5: Result of 'Dir.unusedResult()' is ignored",
      "test-module/dir/N.java:7:5: Result of 'N.unusedMethod()' is ignored",
      "test-module/dir/N.java:8:5: Result of 'N.unusedMethod()' is ignored",
    )
  }

  /**
   * The pattern syntax in `.gitignore` differs from the pattern syntax typically used in [FilePatternPackageSet];
   * see [the documentation](https://www.jetbrains.com/help/idea/scope-language-syntax-reference.html) and PackageSetTest.
   *
   * Ensure that at least the commonly used patterns are interpreted in the same way as in `.gitignore`.
   * It also shouldn't be possible to cause an exception by having a strange pattern in `.gitignore` that cannot be translated to the
   * PackageSet pattern syntax.
   */
  @Test
  fun `wildcards in gitignore`() {
    createGitIgnoreFile("/test-module/dir/[A-M].java\n")

    val srcDir = project.modules.single().guessModuleDir()!!.toNioPath().resolve("dir")

    assertTrue(fileIsIgnored(srcDir.resolve("A.java")))
    assertFalse(fileIsIgnored(srcDir.resolve("N.java")))

    ensureIgnoredFilesLoaded()

    runAnalysis()

    assertSarifSummary(
      /* .gitignore '/[A-M].java' matches 'A.java' but not 'N.java' */
      "test-module/dir.java:7:5: Result of 'Dir.unusedResult()' is ignored",
      "test-module/dir/N.java:7:5: Result of 'N.unusedMethod()' is ignored",
      "test-module/dir/N.java:8:5: Result of 'N.unusedMethod()' is ignored",
    )
  }

  @Test
  fun runQodanaWithGitIgnoreYaml() {
    createGitIgnoreFile("/test-module/dir\n")

    runYamlTest()
    assertSarifSummary(
      /* .gitignore '/test-module/dir' matches '/test-module/dir/..' but not '/test-module/dir.java' */
      "test-module/dir.java:7:5: Result of 'Dir.unusedResult()' is ignored"
    )
  }

  @Test
  fun runQodanaWithGitIgnorePartYaml() {
    createGitIgnoreFile("/test-module/dir/A.java\n")

    runYamlTest()

    assertSarifSummary(
      /* No problems in A.java, since it is listed in .gitignore. */
      "test-module/dir.java:7:5: Result of 'Dir.unusedResult()' is ignored",
      "test-module/dir/N.java:7:5: Result of 'N.unusedMethod()' is ignored",
      "test-module/dir/N.java:8:5: Result of 'N.unusedMethod()' is ignored",
    )
  }

  @Test
  fun `wildcards in gitignore yaml`() {
    createGitIgnoreFile("/test-module/dir/[A-M].java\n")

    runYamlTest()
    assertSarifSummary(
      /* .gitignore '/[A-M].java' matches 'A.java' but not 'N.java' */
      "test-module/dir.java:7:5: Result of 'Dir.unusedResult()' is ignored",
      "test-module/dir/N.java:7:5: Result of 'N.unusedMethod()' is ignored",
      "test-module/dir/N.java:8:5: Result of 'N.unusedMethod()' is ignored",
    )
  }

  private fun runYamlTest() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(path = getTestDataPath("inspection-profile.yaml").absolutePathString())
      )
    }

    ensureIgnoredFilesLoaded()

    runAnalysis()
  }

  private fun ensureIgnoredFilesLoaded() {
    val untrackedFilesHolder = GitRepositoryManager.getInstance(project).repositories.single().untrackedFilesHolder
    untrackedFilesHolder.invalidate()
    untrackedFilesHolder.createWaiter().waitFor()
  }
}

package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat

class ProblemCountersTest : BasePlatformTestCase() {
  private val file1 = "A.java"
  private val file2 = "B.java"
  private val file3 = "B.java"

  private val module1 = "Module1"
  private val module2 = "Module2"
  private val module3 = "Module3"

  fun `test counters don't exceed threshold`() {
    val counters = counters(maxPerFile = 2, maxPerModule = 5, maxPerProject = 11)

    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file2, module1)).isFalse

    assertThat(counters.addProblem(file1, module2)).isFalse
    assertThat(counters.addProblem(file2, module2)).isFalse
  }

  fun `test counters exceed on file1 module1 and file2 module1`() {
    val counters = counters(maxPerFile = 2, maxPerModule = 5, maxPerProject = 11)

    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isTrue // file1 threshold reached

    assertThat(counters.addProblem(file2, module1)).isFalse
    assertThat(counters.addProblem(file2, module1)).isTrue // file2 threshold reached

    assertThat(counters.addProblem(file3, module1)).isTrue
    assertThat(counters.addProblem(file3, module1)).isTrue // file3 and module1 thresholds reached
  }

  fun `test counters exceed on file1 file2 module1 and file3 module1 also exceeds`() {
    val counters = counters(maxPerFile = 2, maxPerModule = 4, maxPerProject = 11)

    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isTrue // file1 threshold reached

    assertThat(counters.addProblem(file2, module1)).isFalse
    assertThat(counters.addProblem(file2, module1)).isTrue // file2 and module2 thresholds reached

    assertThat(counters.addProblem(file3, module1)).isTrue // module1 threshold is already reached
  }

  fun `test counters exceed on file1 module1 but on file2 module1 doesnt exceed`() {
    val counters = counters(maxPerFile = 2, maxPerModule = 5, maxPerProject = 11)

    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isTrue // file1 threshold reached
    assertThat(counters.addProblem(file1, module1)).isTrue // extra add, not counted
    assertThat(counters.addProblem(file1, module1)).isTrue // extra add, not counted
    assertThat(counters.addProblem(file1, module1)).isTrue // extra add, not counted, module1 threshold shouldn't be reached here
    assertThat(counters.addProblem(file1, module1)).isTrue // extra add, not counted

    assertThat(counters.addProblem(file2, module1)).isFalse
    assertThat(counters.addProblem(file2, module1)).isTrue // file2 threshold is reached
  }

  fun `test counters exceed on file1 module1 on file2 module1 exceed too`() {
    val counters = counters(maxPerFile = 10, maxPerModule = 5, maxPerProject = 11)

    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isTrue // module1 threshold reached
    assertThat(counters.addProblem(file1, module1)).isTrue // extra add, not counted

    assertThat(counters.addProblem(file2, module1)).isTrue
  }

  fun `test counters exceed on module1 on module2 doesnt exceed`() {
    val counters = counters(maxPerFile = 100, maxPerModule = 5, maxPerProject = 11)

    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file2, module1)).isFalse
    assertThat(counters.addProblem(file2, module1)).isTrue // module1 threshold reached
    assertThat(counters.addProblem(file2, module1)).isTrue // extra add, not counted

    assertThat(counters.addProblem(file1, module2)).isFalse
    assertThat(counters.addProblem(file1, module2)).isFalse
    assertThat(counters.addProblem(file2, module2)).isFalse
    assertThat(counters.addProblem(file2, module2)).isFalse
    assertThat(counters.addProblem(file2, module2)).isTrue // module2 threshold reached
  }

  fun `test counters exceed module1 and module2 on module3 exceed because of project`() {
    val counters = counters(maxPerFile = 100, maxPerModule = 2, maxPerProject = 5)

    assertThat(counters.addProblem(file1, module1)).isFalse
    assertThat(counters.addProblem(file2, module1)).isTrue // module1 threshold reached
    assertThat(counters.addProblem(file3, module1)).isTrue // extra add, not counted
    assertThat(counters.addProblem(file3, module1)).isTrue // extra add, not counted
    assertThat(counters.addProblem(file3, module1)).isTrue // extra add, not counted, project threshold not reached here

    assertThat(counters.addProblem(file1, module2)).isFalse
    assertThat(counters.addProblem(file1, module2)).isTrue // module2 threshold reached
    assertThat(counters.addProblem(file3, module2)).isTrue // extra add, not counted

    assertThat(counters.addProblem(file1, module3)).isTrue // project threshold reached
  }

  @Suppress("SameParameterValue")
  private fun counters(maxPerFile: Int, maxPerModule: Int, maxPerProject: Int): ProblemCounters {
    return ProblemCounters(project, QodanaToolRegistrar.getInstance(project).createTools().first(), maxPerFile, maxPerModule, maxPerProject)
  }
}
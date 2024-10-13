package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.testFramework.utils.io.deleteRecursively
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Files

class QodanaToolResultDatabaseTest {
  @Test
  fun `reading must not throw if DB was never written to`() {
    val path = Files.createTempDirectory(javaClass.simpleName)
    try {
      QodanaToolResultDatabase.create(path).close()
      QodanaToolResultDatabase.open(path)
        .use {
          val selection = it.select("sanity").executeQuery().toList()
          assertThat(selection).isEmpty()
        }
    }
    finally {
      path.deleteRecursively()
    }
  }
}

package tanvd.grazi.grammar

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.CharsetToolkit
import org.junit.Test
import tanvd.grazi.GraziTestBase
import java.io.File
import kotlin.system.measureTimeMillis

class GrammarEnginePerformanceTest : GraziTestBase(true) {
  @Test
  fun `test performance with 10 sonnets`() {
    val text = FileUtil.loadFile(File(testDataPath, "grammar/sonnet_10.txt"), CharsetToolkit.UTF8_CHARSET)
    var fixes: List<Typo> = emptyList()
    val totalTime = measureTimeMillis {
      fixes = GrammarEngine.getTypos(text).toList()
    }
    fixes.forEach { it.verify(text) }
    assert(fixes.size > 30)
    assert(totalTime < 10_000)
  }

  @Test
  fun `test performance with 50 sonnets`() {
    val text = FileUtil.loadFile(File(testDataPath, "grammar/sonnet_50.txt"), CharsetToolkit.UTF8_CHARSET)
    var fixes: List<Typo> = emptyList()
    val totalTime = measureTimeMillis {
      fixes = GrammarEngine.getTypos(text).toList()
    }
    fixes.forEach { it.verify(text) }
    assert(fixes.size > 200)
    assert(totalTime < 20_000)
  }
}
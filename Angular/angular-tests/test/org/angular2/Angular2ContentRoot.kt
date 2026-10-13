package org.angular2

import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.util.Disposer
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import java.io.FileNotFoundException

/**
 * Replaces the content roots of the test module with the directory at [path] in the temp dir.
 * Files outside of this directory stay in the temp dir, but are not in the content of the project.
 * The original content roots are restored when the test ends.
 */
class Angular2ContentRoot(
  private val path: String,
) : PolySymbolsTestConfigurator {

  override fun configure(fixture: CodeInsightTestFixture) {
    val contentRoot = fixture.tempDirFixture.getFile(path) ?: throw FileNotFoundException(path)
    val module = fixture.module
    val originalSourceFolders = mutableMapOf<String, List<Pair<String, Boolean>>>()
    ModuleRootModificationUtil.updateModel(module) { model ->
      for (entry in model.contentEntries) {
        originalSourceFolders[entry.url] = entry.sourceFolders.map { Pair(it.url, it.isTestSource) }
        model.removeContentEntry(entry)
      }
      model.addContentEntry(contentRoot).addSourceFolder(contentRoot, false)
    }
    Disposer.register(fixture.testRootDisposable) {
      if (module.isDisposed) return@register
      ModuleRootModificationUtil.updateModel(module) { model ->
        model.contentEntries.forEach { model.removeContentEntry(it) }
        for ((url, sourceFolders) in originalSourceFolders) {
          val entry = model.addContentEntry(url)
          for ((sourceUrl, isTestSource) in sourceFolders) {
            entry.addSourceFolder(sourceUrl, isTestSource)
          }
        }
      }
    }
  }

}

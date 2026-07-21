package org.intellij.plugin.mdx.js

import com.intellij.lang.ecmascript6.ES6HandlersFactory
import com.intellij.lang.javascript.modules.imports.ES6ImportExecutorFactory
import com.intellij.lang.javascript.modules.imports.JSAddImportExecutor
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportExecutorFactory
import com.intellij.psi.PsiElement

/**
 * Supplies [MdxAddImportExecutor] instead of the platform one, so new imports land after a leading front
 * matter header. Extends [ES6HandlersFactory] rather than the bare JSHandlersFactory to keep MdxJS's ES6
 * completion-keyword behavior (`from`/`as` after `import`, export keywords, ...).
 */
internal class MdxJSHandlersFactory : ES6HandlersFactory() {
  override fun createImportFactories(place: PsiElement): List<JSImportExecutorFactory> {
    return super.createImportFactories(place).map { factory ->
      if (factory is ES6ImportExecutorFactory) {
        object : JSImportExecutorFactory {
          override fun createExecutor(place: PsiElement): JSAddImportExecutor = MdxAddImportExecutor(place)

          override fun isAvailable(candidate: JSImportCandidate, place: PsiElement): Boolean =
            factory.isAvailable(candidate, place)
        }
      }
      else {
        factory
      }
    }
  }
}

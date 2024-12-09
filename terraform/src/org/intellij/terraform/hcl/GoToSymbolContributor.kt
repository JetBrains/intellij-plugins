// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.DumbModeAccessType
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import org.intellij.terraform.hcl.psi.*

class GoToSymbolContributor : ChooseByNameContributorEx, DumbAware {

  override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
    val fileIndex = FileBasedIndex.getInstance()
    DumbModeAccessType.RAW_INDEX_DATA_ACCEPTABLE.ignoreDumbMode {
      fileIndex.processAllKeys(NAME, processor, scope, filter)
    }
  }

  override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
    val scope = parameters.searchScope
    val collector = SymbolCollector(name, parameters.project, scope, processor)
    val fileIndex = FileBasedIndex.getInstance()
    DumbModeAccessType.RELIABLE_DATA_ONLY.ignoreDumbMode {
      fileIndex.processValues(NAME, name, null, collector, scope, parameters.idFilter)
    }
  }
}

private class SymbolCollector(private val name: String, project: Project, private val scope: GlobalSearchScope, val processor: Processor<in NavigationItem>) : FileBasedIndex.ValueProcessor<Void?> {
  private val psiManager = PsiManager.getInstance(project)

  override fun process(file: VirtualFile, kind: Void?): Boolean {
    if (!scope.contains(file)) return true

    val psiFile = psiManager.findFile(file)
    if (psiFile is HCLFile) {

      psiFile.acceptChildren(object : HCLElementVisitor(),PsiRecursiveVisitor {
        private var stop = false

        override fun visitElement(element: HCLElement) {
          ProgressIndicatorProvider.checkCanceled()
          if (!stop) {
            element.acceptChildren(this)
          }
        }

        override fun visitProperty(o: HCLProperty) {
          if (name == o.name) {
            if (!processor.process(o)) {
              stop = true
              return
            }
          }
          ProgressIndicatorProvider.checkCanceled()
          o.value?.accept(this)
        }

        override fun visitBlock(o: HCLBlock) {
          if (name == o.fullName || o.nameElements.any { name == it.name }) {
            if (!processor.process(o)) {
              stop = true
              return
            }
          }
          ProgressIndicatorProvider.checkCanceled()
          o.`object`?.acceptChildren(this)
        }
      })
    }
    return true
  }
}
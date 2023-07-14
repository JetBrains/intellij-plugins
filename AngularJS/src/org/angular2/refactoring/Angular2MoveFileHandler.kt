// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.refactoring

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.typescript.refactoring.ES6MoveFileHandler
import com.intellij.psi.PsiFile
import com.intellij.usageView.UsageInfo
import com.intellij.util.IncorrectOperationException
import com.intellij.util.containers.ContainerUtil
import org.angular2.lang.Angular2LangUtil
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider.Angular2TemplateReferenceData
import java.util.*

class Angular2MoveFileHandler : ES6MoveFileHandler() {
  override fun canProcessElement(element: PsiFile): Boolean {
    return (element is JSFile
            && DialectDetector.isTypeScript(element)
            && Angular2LangUtil.isAngular2Context(element))
  }

  override fun doFindUsages(psiFile: PsiFile): List<UsageInfo> {
    // In addition to hack from ES6MoveFileHandler for preventing broken file reference,
    // we need to workaround broken contract of `prepareMovedFile` when moving directories.
    val map = Angular2SoftFileReferenceSet.encodeTemplateReferenceData(psiFile)
    return if (map.isEmpty()) super.doFindUsages(psiFile)
    else ContainerUtil.append(
      super.doFindUsages(psiFile), MyRestoreReferencesUsage(psiFile, map))
  }

  @Throws(IncorrectOperationException::class)
  override fun updateMovedFile(file: PsiFile) {
    super.updateMovedFile(file)
    Angular2SoftFileReferenceSet.decodeTemplateReferenceData(file)
  }

  private class MyRestoreReferencesUsage(element: PsiFile, refs: Map<String, Angular2TemplateReferenceData>)
    : RestoreReferencesUsage<Map<String, Angular2TemplateReferenceData>>(element, refs) {
    override fun restore(file: PsiFile) {
      Angular2SoftFileReferenceSet.decodeTemplateReferenceData(file, myRefs)
    }

  }
}
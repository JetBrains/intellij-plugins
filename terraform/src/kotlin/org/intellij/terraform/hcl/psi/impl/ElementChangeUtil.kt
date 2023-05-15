// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.source.SourceTreeToPsiMap
import com.intellij.psi.impl.source.tree.Factory
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException

object ElementChangeUtil {

  @Throws(IncorrectOperationException::class)
  internal fun doNameReplacement(elementDecl: PsiNameIdentifierOwner, identifier: PsiElement, name: String, elementType: IElementType) {
    if (!elementDecl.isWritable) {
      throw IncorrectOperationException("Cannot rename ${elementDecl.javaClass.name}: element non-writable")
    } else if (!isInProjectContent(elementDecl.project, elementDecl.containingFile.virtualFile)) {
      throw IncorrectOperationException("Cannot rename ${elementDecl.javaClass.name}: element not in project content")
    } else {
      val manipulator = ElementManipulators.getManipulator(identifier)
      if (manipulator != null) {
        manipulator.handleContentChange(identifier, name)
        return
      }
      val replacement = SourceTreeToPsiMap.treeElementToPsi(Factory.createSingleLeafElement(elementType, name, null, elementDecl.manager))
      if (replacement != null) {
        identifier.replace(replacement)
      }
    }
  }

  private fun isInProjectContent(project: Project, vfile: VirtualFile?): Boolean {
    return vfile == null || ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(vfile) != null
  }
}

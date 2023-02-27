/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

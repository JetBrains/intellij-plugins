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
package org.intellij.terraform.hcl.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.hcl.psi.*

class HCLStructureViewFactory : PsiStructureViewFactory {
  override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
    return object : TreeBasedStructureViewBuilder() {
      override fun createStructureViewModel(editor: Editor?): StructureViewModel {
        return HCLStructureViewModel(psiFile as HCLFile, editor)
      }
    }
  }
}

class HCLStructureViewModel(file: HCLFile, editor: Editor?) : StructureViewModelBase(file, editor, HCLStructureViewElement(file)), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
  init {
    withSuitableClasses(HCLFile::class.java, HCLProperty::class.java, HCLObject::class.java, HCLArray::class.java, HCLBlock::class.java)
  }

  override fun isAlwaysLeaf(element: StructureViewTreeElement?): Boolean {
    return false
  }

  override fun isAlwaysShowsPlus(element: StructureViewTreeElement?): Boolean {
    return false
  }

  override fun isAutoExpand(element: StructureViewTreeElement): Boolean {
    return element.value is PsiFile || ApplicationManager.getApplication().isUnitTestMode
  }

  override fun isSmartExpand(): Boolean {
    return false
  }
}

class HCLStructureViewElement(val element: HCLElement) : StructureViewTreeElement {
  init {
    assert(PsiTreeUtil.instanceOf(element, HCLFile::class.java, HCLProperty::class.java, HCLObject::class.java, HCLArray::class.java, HCLBlock::class.java))
  }

  override fun getValue(): Any {
    return element
  }

  override fun canNavigate(): Boolean {
    return element.canNavigate()
  }

  override fun canNavigateToSource(): Boolean {
    return element.canNavigateToSource()
  }

  override fun navigate(requestFocus: Boolean) {
    element.navigate(requestFocus)
  }

  override fun getPresentation(): ItemPresentation {
    return element.presentation!!
  }

  override fun getChildren(): Array<out TreeElement> {
    val value: HCLElement
    if (element is HCLProperty) {
      value = element.value as? HCLValue ?: return emptyArray()
    } else if (element is HCLBlock) {
      value = element.`object` ?: return emptyArray()
    } else {
      value = element
    }

    val list: List<HCLStructureViewElement> = value.children.mapNotNull {
      when (it) {
        is HCLObject -> HCLStructureViewElement(it)
        is HCLArray -> HCLStructureViewElement(it)
        is HCLProperty -> HCLStructureViewElement(it)
        is HCLBlock -> HCLStructureViewElement(it)
        else -> null
      }
    }

    return list.toTypedArray()
  }

}

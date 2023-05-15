// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

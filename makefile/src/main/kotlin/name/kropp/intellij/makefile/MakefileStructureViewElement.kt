package name.kropp.intellij.makefile

import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileRule

class MakefileStructureViewElement(private val element: PsiElement) : StructureViewTreeElement {
  override fun getPresentation() = (element as? NavigationItem)?.presentation ?: object : ItemPresentation {
    override fun getIcon(p0: Boolean) = AllIcons.Toolwindows.ToolWindowRun
    override fun getPresentableText() = (element as? MakefileRule)?.targetLine?.firstChild?.text ?: ""
    override fun getLocationString() = ""
  }

  override fun getChildren(): Array<out TreeElement> {
    if (element is MakefileFile) {
      val targets = PsiTreeUtil.getChildrenOfType(element, MakefileRule::class.java)
      return targets?.map(::MakefileStructureViewElement)?.toTypedArray() ?: emptyArray()
    } else {
      return emptyArray()
    }
  }

  override fun canNavigate() = (element as? NavigationItem)?.canNavigate() ?: false
  override fun canNavigateToSource() = (element as? NavigationItem)?.canNavigateToSource() ?: false

  override fun navigate(requestFocus: Boolean) {
    (element as? NavigationItem)?.navigate(requestFocus)
  }

  override fun getValue() = element
}
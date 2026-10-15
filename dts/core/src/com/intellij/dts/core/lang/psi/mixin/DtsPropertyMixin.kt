package com.intellij.dts.core.lang.psi.mixin

import com.intellij.dts.core.icons.DtsCoreIcons
import com.intellij.dts.core.lang.DtsAffiliation
import com.intellij.dts.core.lang.psi.DtsProperty
import com.intellij.dts.core.lang.psi.DtsPropertyContent
import com.intellij.dts.core.lang.psi.DtsStatementKind
import com.intellij.dts.core.lang.psi.DtsTypes
import com.intellij.dts.core.lang.psi.DtsValue
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.Icon

abstract class DtsPropertyMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsProperty {
  override val dtsAffiliation: DtsAffiliation
    get() = DtsAffiliation.NODE

  override val dtsStatementKind: DtsStatementKind
    get() = DtsStatementKind.PROPERTY

  override val dtsIsComplete: Boolean
    get() = !PsiTreeUtil.hasErrorElements(this)

  override val dtsNameElement: PsiElement
    get() = findNotNullChildByType<PsiElement>(DtsTypes.NAME)

  override val dtsName: String
    get() = dtsNameElement.text

  override val dtsValues: List<DtsValue>
    get() {
      val content = findChildByClass(DtsPropertyContent::class.java) ?: return emptyList()
      return PsiTreeUtil.getChildrenOfTypeAsList(content, DtsValue::class.java)
    }

  override fun getTextOffset(): Int = dtsNameElement.textOffset

  override fun getIcon(flags: Int): Icon = DtsCoreIcons.Property
}
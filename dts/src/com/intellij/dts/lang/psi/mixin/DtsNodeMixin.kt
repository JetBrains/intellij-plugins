package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.DtsIcons
import com.intellij.dts.lang.DtsAffiliation
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsNodeContent
import com.intellij.dts.lang.psi.DtsPHandle
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsPsiFactory
import com.intellij.dts.lang.psi.DtsRefNode
import com.intellij.dts.lang.psi.DtsRootNode
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.DtsStatementKind
import com.intellij.dts.lang.psi.DtsSubNode
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.psi.getDtsPresentableText
import com.intellij.dts.lang.psi.impl.DtsStubBasedElement
import com.intellij.dts.lang.stubs.impl.DtsRefNodeStub
import com.intellij.dts.lang.stubs.impl.DtsRootNodeStub
import com.intellij.dts.lang.stubs.impl.DtsSubNodeStub
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.Icon

abstract class DtsNodeMixin<T : StubBase<*>> : DtsStubBasedElement<T>, DtsStatement.Node {
  constructor(node: ASTNode) : super(node)

  constructor(stub: T, elementType: IStubElementType<*, *>) : super(stub, elementType)

  override val dtsStatementKind: DtsStatementKind
    get() = DtsStatementKind.NODE

  override val dtsIsComplete: Boolean
    get() = !PsiTreeUtil.hasErrorElements(this) && findChildByType<PsiElement>(DtsTypes.RBRACE) != null

  override val dtsContent: DtsNodeContent?
    get() = findChildByClass(DtsNodeContent::class.java)

  override val dtsIsEmpty: Boolean
    get() {
      val content = dtsContent ?: return true
      return content.dtsEntries.isEmpty()
    }

  override val dtsProperties: List<DtsProperty>
    get() = dtsContent?.dtsProperties ?: emptyList()

  override val dtsSubNodes: List<DtsSubNode>
    get() = dtsContent?.dtsNodes?.filterIsInstance<DtsSubNode>() ?: emptyList()

  override fun getIcon(flags: Int): Icon = DtsIcons.Node

  override fun getPresentation(): ItemPresentation {
    return object : ItemPresentation {
      override fun getPresentableText(): String = getDtsPresentableText()

      override fun getIcon(unused: Boolean): Icon = DtsIcons.Node

      override fun getLocationString(): String? = containingFile.originalFile.virtualFile?.presentableName
    }
  }

  private fun addContentWithProperty(property: DtsEntry): DtsEntry {
    val content = DtsPsiFactory.createNodeContent(project)
    content.add(property)

    // find not null child is safe because lbrace is a pined character
    val brace = findNotNullChildByType<PsiElement>(DtsTypes.LBRACE)

    val actualContent = addAfter(content, brace) as DtsNodeContent

    return actualContent.dtsEntries.single()
  }

  override fun addDtsProperty(property: DtsEntry): DtsEntry {
    val content = dtsContent

    if (content == null) {
      return addContentWithProperty(property)
    }

    val lastProperty = content.dtsEntries.lastOrNull { it.dtsStatement.dtsStatementKind.isProperty() }
    return content.addAfter(property, lastProperty) as DtsEntry
  }
}

abstract class DtsSubNodeMixin : DtsNodeMixin<DtsSubNodeStub>, DtsSubNode {
  constructor(node: ASTNode) : super(node)

  constructor(stub: DtsSubNodeStub, elementType: IStubElementType<*, *>) : super(stub, elementType)

  override val dtsAffiliation: DtsAffiliation
    get() = DtsAffiliation.NODE

  override val dtsName: String
    get() = dtsNameElement.text

  override val dtsNameElement: PsiElement
    get() = findNotNullChildByType(DtsTypes.NAME)

  override val dtsLabels: List<String>
    get() = findChildrenByType<PsiElement>(DtsTypes.LABEL).map { it.text.trimEnd(':') }

  override fun getTextOffset(): Int = dtsNameElement.textOffset
}

abstract class DtsRefNodeMixin : DtsNodeMixin<DtsRefNodeStub>, DtsRefNode {
  constructor(node: ASTNode) : super(node)

  constructor(stub: DtsRefNodeStub, elementType: IStubElementType<*, *>) : super(stub, elementType)

  override val dtsAffiliation: DtsAffiliation
    get() = DtsAffiliation.ROOT

  override val dtsHandle: DtsPHandle
    get() = findNotNullChildByType(DtsTypes.P_HANDLE)

  override val dtsLabels: List<String>
    get() = findChildrenByType<PsiElement>(DtsTypes.LABEL).map { it.text.trimEnd(':') }

  override fun getTextOffset(): Int = dtsHandle.textOffset
}

abstract class DtsRootNodeMixin : DtsNodeMixin<DtsRootNodeStub>, DtsRootNode {
  constructor(node: ASTNode) : super(node)

  constructor(stub: DtsRootNodeStub, elementType: IStubElementType<*, *>) : super(stub, elementType)

  override val dtsAffiliation: DtsAffiliation
    get() = DtsAffiliation.ROOT

  override val dtsSlash: PsiElement
    get() = findNotNullChildByType(DtsTypes.SLASH)
}
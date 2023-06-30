package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.DtsAffiliation
import com.intellij.dts.lang.psi.*
import com.intellij.dts.lang.psi.impl.DtsStubBasedElement
import com.intellij.dts.lang.stubs.DtsRootNodeStub
import com.intellij.dts.lang.stubs.DtsSubNodeStub
import com.intellij.dts.util.DtsUtil
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.util.elementType
import javax.swing.Icon

abstract class DtsNodeMixin<T : StubBase<*>> : DtsStubBasedElement<T>, DtsStatement.Node {
    constructor(node: ASTNode) : super(node)

    constructor(stub: T, elementType: IStubElementType<*, *>) : super(stub, elementType)

    override val dtsStatementKind: DtsStatementKind
        get() = DtsStatementKind.NODE

    override val dtsName: String
        get() = dtsNameElement.text

    override val dtsAnnotationTarget: PsiElement
        get() = dtsNameElement

    override val dtsContent: DtsNodeContent?
        get() = findChildByClass(DtsNodeContent::class.java)

    override val dtsLabels: List<String>
        get() = findChildrenByType<PsiElement>(DtsTypes.LABEL).map { it.text.trimEnd(':') }

    override fun getTextOffset(): Int = dtsNameElement.textOffset

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String = dtsName

            override fun getIcon(unused: Boolean): Icon? = null

            override fun getLocationString(): String? = containingFile.originalFile.virtualFile?.presentableName
        }
    }
}

abstract class DtsSubNodeMixin : DtsNodeMixin<DtsSubNodeStub>, DtsSubNode {
    constructor(node: ASTNode) : super(node)

    constructor(stub: DtsSubNodeStub, elementType: IStubElementType<*, *>) : super(stub, elementType)

    override val dtsAffiliation: DtsAffiliation
        get() = DtsAffiliation.NODE

    override val dtsNameElement: PsiElement
        get() = findNotNullChildByType(DtsTypes.NAME)
}

abstract class DtsRootNodeMixin : DtsNodeMixin<DtsRootNodeStub>, DtsRootNode {
    constructor(node: ASTNode) : super(node)

    constructor(stub: DtsRootNodeStub, elementType: IStubElementType<*, *>) : super(stub, elementType)

    override val dtsAffiliation: DtsAffiliation
        get() = DtsAffiliation.ROOT

    override val dtsNameElement: PsiElement
        get() = DtsUtil.children(this).first { it.elementType == DtsTypes.SLASH || it.elementType == DtsTypes.P_HANDLE }
}
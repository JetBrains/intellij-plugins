package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.xml.XmlTag

/**
 * @author Irina.Chernushina on 12/11/2017.
 */
class VueTagNameReference(nameElement: ASTNode, startTagFlag: Boolean) : TagNameReference(nameElement, startTagFlag), PsiPolyVariantReference {
  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val parentTag = nameElement?.treeParent as? XmlTag
    val descriptor = VueTagProvider().getDescriptor(parentTag) as? VueElementDescriptor
    if (descriptor != null) {
      return descriptor.variants.map { PsiElementResolveResult(it) }.toTypedArray()
    }
    return emptyArray()
  }
}
// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor

class VueTagNameReference(nameElement: ASTNode, startTagFlag: Boolean) : TagNameReference(nameElement, startTagFlag),
                                                                         PsiPolyVariantReference {
  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    return ((nameElement?.treeParent as? XmlTag)?.descriptor as? VueElementDescriptor)
             ?.getPsiSources()
             ?.map { PsiElementResolveResult(it) as ResolveResult }
             ?.toTypedArray()
           ?: emptyArray()
  }
}

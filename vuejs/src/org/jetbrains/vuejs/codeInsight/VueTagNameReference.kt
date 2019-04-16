// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.xml.XmlTag

class VueTagNameReference(nameElement: ASTNode, startTagFlag: Boolean) : TagNameReference(nameElement, startTagFlag),
                                                                         PsiPolyVariantReference {
  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val parentTag = nameElement?.treeParent as? XmlTag
    val descriptor = parentTag?.descriptor as? VueElementDescriptor
    if (descriptor != null) {
      return descriptor.variants.map { PsiElementResolveResult(it) }.toTypedArray()
    }
    return emptyArray()
  }
}

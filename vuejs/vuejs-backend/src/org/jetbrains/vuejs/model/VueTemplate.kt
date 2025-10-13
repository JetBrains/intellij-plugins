// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.xml.XmlTag

interface VueTemplate<T : PsiElement> {

  val source: T

  @StubSafe
  fun safeVisitTags(visitor: (XmlTag) -> Unit) {
    source.acceptChildren(object : XmlRecursiveElementWalkingVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        visitor.invoke(tag)
        super.visitXmlTag(tag)
      }
    })
  }

}

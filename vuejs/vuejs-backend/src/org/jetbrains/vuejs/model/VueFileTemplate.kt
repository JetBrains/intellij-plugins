// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.impl.source.xml.stub.XmlTagStub
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag

class VueFileTemplate(override val source: XmlFile) : VueTemplate<XmlFile> {

  override fun safeVisitTags(visitor: (XmlTag) -> Unit) {
    if (source is PsiFileImpl) {
      source.stubTree?.let { stubTree ->
        stubTree.plainList
          .asSequence()
          .filterIsInstance<XmlTagStub<*>>()
          .forEach { visitor(it.psi) }
        return
      }
    }
    super.safeVisitTags(visitor)
  }

  override fun equals(other: Any?): Boolean {
    return (other as? VueFileTemplate)?.source == source
  }

  override fun hashCode(): Int {
    return source.hashCode()
  }

}

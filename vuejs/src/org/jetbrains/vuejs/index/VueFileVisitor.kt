// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.index

import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile

open class VueFileVisitor : XmlElementVisitor() {
  override fun visitXmlDocument(document: XmlDocument): Unit = recursion(document)

  override fun visitXmlFile(file: XmlFile): Unit = recursion(file)

  protected fun recursion(element: PsiElement) {
    element.children.forEach { it.accept(this) }
  }
}

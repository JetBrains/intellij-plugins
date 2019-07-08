// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:Suppress("DEPRECATION")

package org.jetbrains.vuejs.codeInsight

import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.codeInsight.attributes._VueAttributeDescriptor

@Deprecated("Class moved, kept here for compatibility with NativeScript")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class VueAttributeDescriptor(name: String,
                                  element: PsiElement? = null,
                                  isNonProp: Boolean = false) :
  _VueAttributeDescriptor(name, element, isNonProp)

@Deprecated("Class moved, kept here for compatibility with NativeScript")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class VueFileVisitor : XmlElementVisitor() {
  override fun visitXmlDocument(document: XmlDocument?): Unit = recursion(document)

  override fun visitXmlFile(file: XmlFile?): Unit = recursion(file)

  protected fun recursion(element: PsiElement?) {
    element?.children?.forEach { it.accept(this) }
  }
}

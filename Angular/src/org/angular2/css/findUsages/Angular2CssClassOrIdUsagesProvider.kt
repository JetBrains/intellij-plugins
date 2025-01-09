// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.findUsages

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.Angular2LangUtil

class Angular2CssClassOrIdUsagesProvider : CssClassOrIdReferenceBasedUsagesProvider() {

  override fun acceptElement(candidate: PsiElement): Boolean {
    return (candidate is XmlAttribute || candidate is JSLiteralExpression || candidate is JSProperty)
           && Angular2LangUtil.isAngular2Context(candidate)
  }
}

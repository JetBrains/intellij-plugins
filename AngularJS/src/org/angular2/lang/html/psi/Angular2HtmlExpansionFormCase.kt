// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi

import com.intellij.psi.PsiElement

interface Angular2HtmlExpansionFormCase : PsiElement {
  val value: String?
}
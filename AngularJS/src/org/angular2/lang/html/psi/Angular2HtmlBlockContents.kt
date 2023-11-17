// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi

import com.intellij.psi.PsiElement

interface Angular2HtmlBlockContents : PsiElement {

  val block: Angular2HtmlBlock

}
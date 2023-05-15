// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.stub.impl

import com.intellij.psi.stubs.PsiFileStubImpl
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.html.VueFileElementType

class VueFileStubImpl : PsiFileStubImpl<VueFile> {
  val langMode: LangMode

  constructor(file: VueFile) : super(file) {
    this.langMode = file.langMode
  }
  constructor(langMode: LangMode) : super(null) {
    this.langMode = langMode
  }

  override fun getType(): VueFileElementType = VueFileElementType.INSTANCE
}
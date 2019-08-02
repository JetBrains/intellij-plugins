// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueFileElementType : IStubFileElementType<PsiFileStub<HtmlFileImpl>>(VueLanguage.INSTANCE) {
  companion object {
    @JvmStatic
    val INSTANCE: VueFileElementType = VueFileElementType()
  }

  override fun getStubVersion(): Int {
    return JSFileElementType.getVersion()
  }
}

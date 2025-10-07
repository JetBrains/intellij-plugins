// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubIndexKey

class VueIdIndex : VueIndexBase<PsiElement>(VUE_ID_INDEX_KEY)

val VUE_ID_INDEX_KEY: StubIndexKey<String, PsiElement> =
  StubIndexKey.createIndexKey("vue.id.index")
val VUE_ID_INDEX_JS_KEY: String = VueIndexBase.createJSKey(VUE_ID_INDEX_KEY)
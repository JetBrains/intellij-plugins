// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.web.WebFramework

class VueFramework: WebFramework() {


  companion object {
    val INSTANCE = get("vue")!!
  }
}
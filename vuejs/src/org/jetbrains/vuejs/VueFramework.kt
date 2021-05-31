// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.lang.html.WebFrameworkHtmlDialect
import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage
import javax.swing.Icon

class VueFramework: WebFramework() {

  override val displayName: String = "Vue"
  override val icon: Icon = VuejsIcons.Vue
  override val standaloneFileType: WebFrameworkHtmlFileType = VueFileType.INSTANCE
  override val htmlFileType: WebFrameworkHtmlFileType = VueFileType.INSTANCE

  companion object {
    val instance get() = get("vue")
  }
}
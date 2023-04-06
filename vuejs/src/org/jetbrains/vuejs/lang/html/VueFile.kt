// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.html.HtmlCompatibleFile
import com.intellij.psi.xml.XmlFile
import org.jetbrains.vuejs.lang.LangMode

interface VueFile : HtmlCompatibleFile, XmlFile {

  val langMode: LangMode

}

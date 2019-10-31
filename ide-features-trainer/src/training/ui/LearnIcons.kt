/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.ui

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon


object LearnIcons {

  private fun load(path: String): Icon
    = IconLoader.getIcon(path, LearnIcons::class.java)

  val checkMarkGray = load("/img/checkmark.png")
  val chevronIcon = load("/img/chevron.png")
  val chevronToolWindowIcon = load("/img/chevron_toolwin.png")

}

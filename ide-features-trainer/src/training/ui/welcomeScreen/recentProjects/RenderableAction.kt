// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.actionSystem.AnAction
import javax.swing.Icon

interface RenderableAction {
  val action: AnAction
  val name: String
  val description: String?
  val icon: Icon?
  val emptyIcon: Icon
  var isValid: Boolean
}
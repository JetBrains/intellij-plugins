/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.components

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.ApplicationComponent
import training.actions.StartLearnAction

class LearnApplicationComponent : ApplicationComponent {

  override fun getComponentName(): String = "IDE Features Trainer application level component"

  override fun disposeComponent() {
  }

  override fun initComponent() {
    if (!StartLearnAction.isEnabled()) removeWelcomeFrameAction()
  }

  private fun removeWelcomeFrameAction() {
    ActionManager.getInstance().unregisterAction(StartLearnAction.ACTION_ID)
  }

}